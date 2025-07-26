package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.Entities.Admin;
import com.Ansh.FinTrust.Entities.User;

import java.util.List;

public interface JwtService {

    String generateUserToken(User user);
    String generateAdminToken(Admin admin);
    List<String> getAuthoritiesFromToken(String token);
    String getUsernameFromToken(String token);

}
