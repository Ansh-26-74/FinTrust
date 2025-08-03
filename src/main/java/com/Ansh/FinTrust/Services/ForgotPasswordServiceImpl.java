package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.DTO.PasswordResetToken;
import com.Ansh.FinTrust.Entities.Admin;
import com.Ansh.FinTrust.Entities.User;
import com.Ansh.FinTrust.Repositories.AdminRepo;
import com.Ansh.FinTrust.Repositories.UserRepo;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private AdminRepo adminRepo;
    @Autowired
    private EmailService emailService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private final RedisTemplate<String, Object> redisTemplate;

    public ForgotPasswordServiceImpl(@Qualifier("redisTemplate2") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public void processForgotPassword(String email) throws Exception {
        Optional<User> userOpt = userRepo.findByEmail(email);
        Optional<Admin> adminOpt = adminRepo.findByEmail(email);

        if (userOpt.isEmpty() && adminOpt.isEmpty()) {
            throw new Exception("If the email exists, a reset link will be sent");
        }

        String userType = userOpt.isPresent() ? "user" : "admin";

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken(token, email);
        redisTemplate.opsForValue().set(token, resetToken, 15, TimeUnit.MINUTES);

        String resetLink = "http://localhost:4200/reset-password?token=" + token;
        String subject = "FinTrust Password Reset Request";
        String body = "Click the link to reset your password: " + resetLink;

        emailService.sendEmail(email, subject, body);
    }

    public void resetPassword(String token, String newPassword) throws Exception {

        PasswordResetToken resetToken = (PasswordResetToken) redisTemplate.opsForValue().get(token);

        if (resetToken == null) {
            throw new Exception("Invalid or expired token");
        }

        String email = resetToken.getEmail();

        Optional<User> userOpt = userRepo.findByEmail(email);
        Optional<Admin> adminOpt = adminRepo.findByEmail(email);

        if (userOpt.isEmpty() && adminOpt.isEmpty()) {
            throw new Exception("Account not found");
        }

        String hashedPassword = passwordEncoder.encode(newPassword);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(hashedPassword);
            userRepo.save(user);
        } else {
            Admin admin = adminOpt.get();
            admin.setPassword(hashedPassword);
            adminRepo.save(admin);
        }

        redisTemplate.delete(token);

    }

}
