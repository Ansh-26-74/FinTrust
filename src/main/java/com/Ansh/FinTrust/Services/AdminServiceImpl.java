package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.DTO.LoginRequest;
import com.Ansh.FinTrust.Entities.Admin;
import com.Ansh.FinTrust.Entities.User;
import com.Ansh.FinTrust.Helper.PhoneNumberValidation;
import com.Ansh.FinTrust.Repositories.AdminRepo;
import com.Ansh.FinTrust.Repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepo adminRepo;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtServiceImpl jwtServiceImpl;
    private final SessionPinService sessionPinService;
    private final EmailService emailService;
    private final PhoneNumberValidation phoneNumberValidation;

    @Override
    public ResponseEntity<?> registerAdmin(Admin admin) {
        try {
            Optional<Admin> existing = adminRepo.findByUsername(admin.getUsername());
            if (existing.isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.ALREADY_REPORTED)
                        .body("Admin already exists");
            }

            if (!admin.getKey().equals("267414362462")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_ACCEPTABLE)
                        .body("Wrong Admin key entered");
            }

            if (!phoneNumberValidation.isValidPhoneNumber(admin.getPhoneNumber()) || !phoneNumberValidation.isValidCountryCode(admin.getCountryCode())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Invalid phone number or country code");
            }

            admin.setPassword(passwordEncoder.encode(admin.getPassword()));

            String fullNumber = admin.getCountryCode() + admin.getPhoneNumber();
            admin.setPhoneNumber(fullNumber);

            if ("admin".equalsIgnoreCase(admin.getRole())) {
                admin.setRole("ROLE_ADMIN");
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Wrong role entered");
            }

            Admin savedAdmin = adminRepo.save(admin);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedAdmin);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error registering admin: " + e.getMessage());
        }
    }


    @Override
    public ResponseEntity<?> login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            Admin admin = adminRepo.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Admin Not Found"));

            String token = jwtServiceImpl.generateAdminToken(admin);

            String pin = sessionPinService.generateAndStorePin(admin.getUsername());
            emailService.sendEmail(admin.getEmail(), "Your FinTrust Session PIN", "Your session PIN is: " + pin);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", admin.getUsername());
            response.put("role", admin.getRole());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        }

    }

    public List<User> getAllUsers(String sessionPin) throws Exception {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!sessionPinService.validatePin(username, sessionPin)) {
            throw new Exception("Invalid or expired session PIN");
        }

        return userRepo.findAll();
    }

    public void lockUser(String username) throws Exception {
        try {
            if (username == null) {
                throw new Exception("Invalid or expired lock token.");
            }

            Optional<User> optionalUser = userRepo.findByUsername(username);
            if (optionalUser.isEmpty()) {
                throw new Exception("User not found.");
            }

            User user = optionalUser.get();

            user.setLocked(true);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 15);
            user.setLockedUntil(calendar.getTime());

            userRepo.save(user);

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
