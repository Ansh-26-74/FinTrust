package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.Entities.Admin;
import com.Ansh.FinTrust.Entities.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Service
public class JwtServiceImpl implements JwtService{

    private static final long EXPIRATION = 1000*60*60*10;   //10 Hours
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateUserToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("authorities", List.of(user.getRole()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }


    public String generateAdminToken(Admin admin) {
        return Jwts.builder()
                .setSubject(admin.getUsername())
                .claim("authorities", java.util.List.of(admin.getRole()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }

    public List<String> getAuthoritiesFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("authorities", List.class);
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}
