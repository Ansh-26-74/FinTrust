package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.DTO.LoginRequest;
import com.Ansh.FinTrust.Entities.User;
import org.springframework.http.ResponseEntity;

public interface UserService {

    ResponseEntity<?> registerUser(User user);
    ResponseEntity<?> login(LoginRequest request);
}
