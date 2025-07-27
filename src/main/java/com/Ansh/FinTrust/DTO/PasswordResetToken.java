package com.Ansh.FinTrust.DTO;

import lombok.Data;

@Data
public class PasswordResetToken {

    private String email;
    private String token;

    public PasswordResetToken() {

    }

    public PasswordResetToken(String token, String email) {
        this.token = token;
        this.email = email;
    }

}
