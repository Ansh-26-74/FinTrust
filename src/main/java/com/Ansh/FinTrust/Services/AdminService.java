package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.DTO.LoginRequest;
import com.Ansh.FinTrust.Entities.Admin;
import org.springframework.http.ResponseEntity;

public interface AdminService {

    ResponseEntity<?> registerAdmin(Admin admin);
    ResponseEntity<?> login(LoginRequest request);

}
