package com.Ansh.FinTrust.Controllers;

import com.Ansh.FinTrust.Services.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/vault")
@RequiredArgsConstructor
public class VaultController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file")MultipartFile file, Principal principal) {

        try {
            String fileId = fileStorageService.uploadFile(file, principal.getName());
            return ResponseEntity.ok("File uploaded with ID: " + fileId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("upload failed" + e.getMessage());
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<?> downloadFile(@PathVariable String filename, Principal principal) {
        try {
            InputStreamResource resource = fileStorageService.downloadFile(filename, principal.getName());

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
}
