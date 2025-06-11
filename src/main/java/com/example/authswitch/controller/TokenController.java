package com.example.authswitch.controller;


import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.authswitch.dto.RefreshTokenRequest;
import com.example.authswitch.dto.TerminalCredentials;
import com.example.authswitch.dto.TokenResponse;
import com.example.authswitch.service.TerminalRegistryService;
import com.example.authswitch.token.JwtTokenService;
import com.example.authswitch.token.RefreshTokenStore;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class TokenController {

    @Autowired

    private  TerminalRegistryService terminalRegistryService;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenStore refreshTokenStore;
    
  


    @PostMapping("/token")
    public ResponseEntity<TokenResponse> generateAccessToken(@RequestBody TerminalCredentials request) {
        boolean valid = terminalRegistryService.validateClient(request.getClientId(), request.getClientSecret());

        if (!valid) {
            return ResponseEntity.status(401).build();
        }

        TokenResponse tokenResponse = jwtTokenService.generateToken(request.getClientId());
        refreshTokenStore.storeRefreshToken(request.getClientId(), tokenResponse.getRefreshToken());
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshAccessToken(@RequestBody RefreshTokenRequest refresh) {
        String storedRefreshToken = refreshTokenStore.getRefreshToken(refresh.getClientId());
        if (!refresh.getRefreshToken().equals(storedRefreshToken)) {
            return ResponseEntity.status(401).build();
        }

        TokenResponse newTokenResponse = jwtTokenService.generateToken(refresh.getClientId());
        refreshTokenStore.storeRefreshToken(refresh.getClientId(), newTokenResponse.getRefreshToken());
        return ResponseEntity.ok(newTokenResponse);
    }
} 
