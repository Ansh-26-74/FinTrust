package com.Ansh.FinTrust.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "admins")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin implements CommonUser{

    @Id
    private String id;

    private String username;
    private String password;
    private String email;
    private String countryCode;
    private String phoneNumber;

    private String role;

}
