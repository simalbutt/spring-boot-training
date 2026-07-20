package org.example.helloworld.user;

import com.fasterxml.jackson.core.Base64Variant;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {


    private final String secret;
    public JwtService(
            @Value("${jwt.secret}") String secret
    ) {
        this.secret = secret;
    }

    public String generateToken(String username, String role){

        SecretKey key = Keys.hmacShaKeyFor(
                secret.getBytes()
        );

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + 3600000
                        )
                )
                .signWith(key)
                .compact();
    }


}