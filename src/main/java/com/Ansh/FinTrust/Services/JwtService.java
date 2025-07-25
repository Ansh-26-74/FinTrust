package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.Entities.User;

public interface JwtService {

    public String generateToken(User user);
    public String getUsernameFromToken(String token);

}
