package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.DTO.LoginRequest;
import com.Ansh.FinTrust.Entities.Admin;
import com.Ansh.FinTrust.Repositories.AdminRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService{

    private final AdminRepo adminRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtServiceImpl jwtServiceImpl;

    @Override
    public ResponseEntity<?> registerAdmin(Admin admin) {
        try {
            Optional<Admin> existing = adminRepo.findByUsername(admin.getUsername());
            if (existing.isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.ALREADY_REPORTED)
                        .body("User already exists");
            }

            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
            String role = admin.getRole();
            if(role.equalsIgnoreCase("admin")) {
                admin.setRole("ROLE_ADMIN");
                Admin savedAdmin = adminRepo.save(admin);
                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(savedAdmin);
            }else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Wrong role entered");
            }

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

            Admin admin = adminRepo.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Admin Not Found"));

            String token = jwtServiceImpl.generateAdminToken(admin);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", admin.getUsername());
            response.put("role", admin.getRole());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        }

    }
}
