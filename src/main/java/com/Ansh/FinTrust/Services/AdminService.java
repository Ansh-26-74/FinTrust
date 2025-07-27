package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.DTO.LoginRequest;
import com.Ansh.FinTrust.Entities.Admin;
import com.Ansh.FinTrust.Entities.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AdminService {

    ResponseEntity<?> registerAdmin(Admin admin);
    ResponseEntity<?> login(LoginRequest request);
    List<User> getAllUsers(String sessionPin) throws Exception;

}
