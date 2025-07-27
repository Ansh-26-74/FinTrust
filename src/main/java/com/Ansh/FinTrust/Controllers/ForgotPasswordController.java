package com.Ansh.FinTrust.Controllers;

import com.Ansh.FinTrust.DTO.ForgotPasswordRequest;
import com.Ansh.FinTrust.DTO.ResetPasswordRequest;
import com.Ansh.FinTrust.Services.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            forgotPasswordService.processForgotPassword(request.getEmail());
            return ResponseEntity.ok("If your email exists, a reset link has been sent.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing request.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            forgotPasswordService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok("Password has been successfully reset");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
