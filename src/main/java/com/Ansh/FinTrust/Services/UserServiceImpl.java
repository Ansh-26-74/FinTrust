package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.DTO.LoginRequest;
import com.Ansh.FinTrust.Entities.User;
import com.Ansh.FinTrust.Helper.PhoneNumberValidation;
import com.Ansh.FinTrust.Repositories.AdminRepo;
import com.Ansh.FinTrust.Repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepo userRepo;
    private final AdminRepo adminRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtServiceImpl jwtServiceImpl;
    private final SessionPinService sessionPinService;
    private final EmailService emailService;
    private final PhoneNumberValidation phoneNumberValidation;

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
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = userRepo.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException(("User Not Found")));

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
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        }

    }
}
