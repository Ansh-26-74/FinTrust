package com.Ansh.FinTrust.Controllers;

import com.Ansh.FinTrust.DTO.FileInfo;
import com.Ansh.FinTrust.DTO.SessionPin;
import com.Ansh.FinTrust.Entities.User;
import com.Ansh.FinTrust.Services.AdminService;
import com.Ansh.FinTrust.Services.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final FileStorageService fileStorageService;

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

}
