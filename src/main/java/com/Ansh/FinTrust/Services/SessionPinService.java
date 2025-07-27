package com.Ansh.FinTrust.Services;

public interface SessionPinService {

    String generateAndStorePin(String username);
    boolean validatePin(String username, String pin);
    public void deletePin(String username);
}
