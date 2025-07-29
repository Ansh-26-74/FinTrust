package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.Entities.Admin;
import com.Ansh.FinTrust.Entities.User;
import com.Ansh.FinTrust.Repositories.AdminRepo;
import com.Ansh.FinTrust.Repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepo userRepo;
    private final AdminRepo adminRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Trying to load user: " + username);

        Optional<User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("User found: " + user.getUsername());
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    List.of(new SimpleGrantedAuthority(user.getRole())
                    )
            );
        }

        Optional<Admin> adminOpt = adminRepo.findByUsername(username);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            return new org.springframework.security.core.userdetails.User(
                    admin.getUsername(),
                    admin.getPassword(),
                    List.of(new SimpleGrantedAuthority(admin.getRole()))
            );
        }

        throw new UsernameNotFoundException("User/Admin not found");
    }
}

