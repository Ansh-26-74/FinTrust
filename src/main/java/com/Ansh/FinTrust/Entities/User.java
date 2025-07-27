package com.Ansh.FinTrust.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

    private String role;


    @Override
    public String getUsername() { return this.username; }

    @Override
    public String getEmail() { return this.email; }

    @Override
    public String getRole() { return this.role; }
}
