package com.Ansh.FinTrust.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements CommonUser {

    @Id
    private String id;

    private String username;
    private String password;
    private String email;
    private String countryCode;
    private String phoneNumber;

    private boolean isLocked = false;
    private Date lockedUntil;

    private String role;

}
