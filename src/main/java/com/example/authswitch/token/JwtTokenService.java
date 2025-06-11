package com.example.authswitch.token;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.example.authswitch.dto.TokenResponse;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtTokenService {

 private Key key;
//Token validity in milliseconds (e.g., 15 minutes)
 private static final long ACCESS_TOKEN_VALIDITY = 15 * 60 * 1000;
 // Refresh token validity (e.g., 7 days)
 private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000;
 @PostConstruct
 public void init() {
     this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
 }

 public TokenResponse generateToken(String clientId) {
     long now = System.currentTimeMillis();

     // Build access token
     String accessToken = Jwts.builder()
             .setSubject(clientId)
             .setId(UUID.randomUUID().toString())
             .setIssuedAt(new Date(now))
             .setExpiration(new Date(now + ACCESS_TOKEN_VALIDITY))
             .signWith(key)
             .compact();

     // Build refresh token
     String refreshToken = Jwts.builder()
             .setSubject(clientId)
             .setId(UUID.randomUUID().toString())
             .setIssuedAt(new Date(now))
             .setExpiration(new Date(now + REFRESH_TOKEN_VALIDITY))
             .signWith(key)
             .compact();

     return new TokenResponse(accessToken, refreshToken, ACCESS_TOKEN_VALIDITY / 1000);
 }

 public String generateRefreshToken() {
     return UUID.randomUUID().toString();
 }

 public String getClientIdFromToken(String token) {
     return Jwts.parserBuilder()
             .setSigningKey(key)
             .build()
             .parseClaimsJws(token)
             .getBody()
             .getSubject();
 }

 public boolean isTokenValid(String token) {
     try {
         Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
         return true;
     } catch (Exception e) {
         return false;
     }
 }

 public long getAccessTokenValidityMillis() {
     return ACCESS_TOKEN_VALIDITY;
 }

 public long getRefreshTokenValidityMillis() {
     return REFRESH_TOKEN_VALIDITY;
 }
} 

//Next: RefreshTokenStore and TokenController
