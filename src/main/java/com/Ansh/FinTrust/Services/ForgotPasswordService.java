package com.Ansh.FinTrust.Services;

public interface ForgotPasswordService {

    void processForgotPassword(String email) throws Exception;

    void resetPassword(String token, String newPassword) throws Exception;

}
