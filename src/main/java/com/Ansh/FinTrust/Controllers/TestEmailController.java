package com.Ansh.FinTrust.Controllers;

import com.Ansh.FinTrust.Services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestEmailController {

    private final EmailService emailService;

    @GetMapping("/email")
    public ResponseEntity<String> testEmail() {
        try {
            emailService.sendEmail("anshm2674@gmail.com", "Test Email", "<h1>This is a test</h1>");
            return ResponseEntity.ok("Email sent");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Email failed: " + e.getMessage());
        }
    }
}

