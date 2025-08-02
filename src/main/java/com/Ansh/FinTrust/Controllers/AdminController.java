package com.Ansh.FinTrust.Controllers;

import com.Ansh.FinTrust.DTO.FileInfo;
import com.Ansh.FinTrust.DTO.SessionPin;
import com.Ansh.FinTrust.Entities.SuspiciousActivity;
import com.Ansh.FinTrust.Entities.User;
import com.Ansh.FinTrust.Services.AdminService;
import com.Ansh.FinTrust.Services.FileStorageService;
import com.Ansh.FinTrust.Services.SuspiciousActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final FileStorageService fileStorageService;
    private final SuspiciousActivityService suspiciousActivityService;


    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers(@RequestBody SessionPin sessionPin) {

        try{
            List<User> allUsers = adminService.getAllUsers(sessionPin.getSessionPin());
            if (allUsers.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No users found.");
            }

            return ResponseEntity.ok(allUsers);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid or expired session PIN");
        }

    }

        @GetMapping("/suspicious-activity")
        public ResponseEntity<List<SuspiciousActivity>> getAllLogs() {
            return ResponseEntity.ok(suspiciousActivityService.getAllLogs());
        }

        @GetMapping("/suspicious-activity/{userId}")
        public ResponseEntity<List<SuspiciousActivity>> getUserLogs(@PathVariable String userId) {
            return ResponseEntity.ok(suspiciousActivityService.getLogsByUser(userId));
        }

        @GetMapping("/lock-user/{username}")
        public ResponseEntity<String> lockUserFromLink(@PathVariable String username) {
            try {
                adminService.lockUser(username);
                return ResponseEntity.ok("✅ User " + username + " has been locked for 15 minutes.");
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                        .body(e.getMessage());
            }
        }
}
