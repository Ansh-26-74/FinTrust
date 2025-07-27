package com.Ansh.FinTrust.Controllers;

import com.Ansh.FinTrust.DTO.FileInfo;
import com.Ansh.FinTrust.DTO.SessionPin;
import com.Ansh.FinTrust.Services.FileStorageService;
import com.Ansh.FinTrust.Services.SessionPinService;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/vault")
@RequiredArgsConstructor
public class VaultController {

    private final FileStorageService fileStorageService;
    private final SessionPinService sessionPinService;

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
    public ResponseEntity<?> downloadFile(@PathVariable String filename, @RequestParam("pin") String pin, Principal principal) {
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

    @PostMapping("/view/{filename}")
    public ResponseEntity<InputStreamResource> viewFile(
            @PathVariable String filename,
            @RequestBody SessionPin sessionPin,
            Principal principal) throws Exception {

        String username = principal.getName();
        String pin = sessionPin.getSessionPin();

        GridFSFile file = fileStorageService.viewFileByFilename(filename, username, pin);

        String contentType = file.getMetadata().getString("_contentType");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .body(fileStorageService.downloadFile(filename, username, pin));
    }

    @GetMapping("files")
    public ResponseEntity<?> listMyFiles() {
        List<FileInfo> files = fileStorageService.listMyFiles();
        return ResponseEntity.ok(files);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Principal principal) {

        sessionPinService.deletePin(principal.getName());

        return ResponseEntity.ok("Logout successful. Session PIN invalidated.");
    }

}
