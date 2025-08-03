package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.DTO.LoginRequest;
import com.Ansh.FinTrust.DTO.SuspiciousEventType;
import com.Ansh.FinTrust.Entities.Admin;
import com.Ansh.FinTrust.Entities.User;
import com.Ansh.FinTrust.Helper.PhoneNumberValidation;
import com.Ansh.FinTrust.Repositories.AdminRepo;
import com.Ansh.FinTrust.Repositories.UserRepo;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtServiceImpl jwtServiceImpl;
    private final SessionPinService sessionPinService;
    private final EmailService emailService;
    private final PhoneNumberValidation phoneNumberValidation;
    private final SuspiciousActivityService suspiciousActivityService;
    private final Admin admin;

    private final RedisTemplate<String, Integer> redisTemplate;

    public UserServiceImpl(UserRepo userRepo, BCryptPasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager, JwtServiceImpl jwtServiceImpl,
                           SessionPinService sessionPinService, EmailService emailService,
                           PhoneNumberValidation phoneNumberValidation,
                           SuspiciousActivityService suspiciousActivityService, Admin admin,
                           @Qualifier("redisTemplate3") RedisTemplate<String, Integer> redisTemplate) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtServiceImpl = jwtServiceImpl;
        this.sessionPinService = sessionPinService;
        this.emailService = emailService;
        this.phoneNumberValidation = phoneNumberValidation;
        this.suspiciousActivityService = suspiciousActivityService;
        this.admin = admin;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public ResponseEntity<?> registerUser(User user) {
        try {
            Optional<User> existing = userRepo.findByUsername(user.getUsername());
            if (existing.isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.ALREADY_REPORTED)
                        .body("User already exists");
            }

            if (!phoneNumberValidation.isValidPhoneNumber(user.getPhoneNumber()) || !phoneNumberValidation.isValidCountryCode(user.getCountryCode())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Invalid phone number or country code");
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));

            String fullNumber = user.getCountryCode() + user.getPhoneNumber();
            user.setPhoneNumber(fullNumber);

            if ("user".equalsIgnoreCase(user.getRole())) {
                user.setRole("ROLE_USER");
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Wrong role entered");
            }

            User savedUser = userRepo.save(user);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedUser);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error registering user: " + e.getMessage());
        }
    }


    @Override
    public ResponseEntity<?> login(LoginRequest request) {
        String key = "FAILED_LOGIN:" + request.getUsername();

        User user = userRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(("User Not Found")));

        if (user.isLocked()) {
            Date now = new Date();
            if (user.getLockedUntil() != null && now.before(user.getLockedUntil())) {
                long minutesLeft = (user.getLockedUntil().getTime() - now.getTime()) / 60000;
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("⛔ User is temporarily locked. Try again in " + minutesLeft + " minute(s).");
            } else {
                user.setLocked(false);
                user.setLockedUntil(null);
                userRepo.save(user);
            }
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            redisTemplate.delete(key);

            String token = jwtServiceImpl.generateUserToken(user);

            String pin = sessionPinService.generateAndStorePin(user.getUsername());
            emailService.sendEmail(user.getEmail(), "Your FinTrust Session PIN", "Your session PIN is: " + pin);


            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", user.getUsername());
            response.put("role", user.getRole());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);
        } catch (Exception e) {
            Long attempts = redisTemplate.opsForValue().increment(key);

            if (attempts == 1) {
                redisTemplate.expire(key, Duration.ofMinutes(10));
            }

            if (attempts > 3) {
                suspiciousActivityService.logEvent(request.getUsername(),
                        SuspiciousEventType.FAILED_LOGIN,
                        "Invalid login credentials after 3+ attempts");


                String lockLink = "https://localhost:8080/api/admin/lock-user/" + request.getUsername();
                String subject = "⚠️ Suspicious Login Detected";
                String body = String.format("""
                                User with username %s failed to login 3 times.\n
                                Time: %s\n
                                
                                Click to temporarily lock the user for 15 minutes:
                                %s
                                """,
                        request.getUsername(),
                        LocalDateTime.now(),
                        lockLink
                );
                try {
                    emailService.sendEmail(admin.getEmail(), subject, body);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

        }
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid username or password");
    }


}
