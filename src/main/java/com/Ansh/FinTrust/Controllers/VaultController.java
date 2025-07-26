package com.Ansh.FinTrust.Controllers;

import com.Ansh.FinTrust.DTO.AccessApprovalRequest;
import com.Ansh.FinTrust.DTO.AdminRequest;
import com.Ansh.FinTrust.DTO.FileInfo;
import com.Ansh.FinTrust.Entities.AdminAccessRequest;
import com.Ansh.FinTrust.Services.AdminRequestService;
import com.Ansh.FinTrust.Services.FileStorageService;
import com.Ansh.FinTrust.Services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/vault")
@RequiredArgsConstructor
public class VaultController {

    private final FileStorageService fileStorageService;
    private final AdminRequestService adminRequestService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file")MultipartFile file, Principal principal, @RequestParam("pin") String pin) {
        if (!pin.matches("\\d{4,6}")) {
            throw new IllegalArgumentException("PIN must be 4 to 6 digits.");
        }
        try {
            String fileId = fileStorageService.uploadFile(file, pin, principal.getName());
            return ResponseEntity.ok("File uploaded with ID: " + fileId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("upload failed" + e.getMessage());
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<?> downloadFile(@PathVariable String filename, @RequestParam String pin, Principal principal) {
        try {
            InputStreamResource resource = fileStorageService.downloadFile(filename, principal.getName(), pin);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Download failed: " + e.getMessage());
        }
    }

    @GetMapping("/user/files")
    public ResponseEntity<?> listUserFiles(HttpServletRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();;
        List<FileInfo> files = fileStorageService.listFiles(username);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/admin/files")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listAllFiles() {
        List<FileInfo> files = fileStorageService.listAllFiles();
        return ResponseEntity.ok(files);
    }

    @PatchMapping("/user/approve")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> approveAccess(@RequestBody AccessApprovalRequest approvalRequest, Principal principal) {
        try {
            adminRequestService.respondToAccessRequests(
                    approvalRequest.getRequestId(),
                    approvalRequest.getAction(),
                    principal.getName()
            );
            return ResponseEntity.ok("Request " + approvalRequest.getAction().toLowerCase() + "ed successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/pending-requests")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getPendingRequests(Principal principal) {
        List<AdminRequest> requests = adminRequestService.getPendingRequestsForUser(principal.getName());
        return ResponseEntity.ok(requests);
    }


}
