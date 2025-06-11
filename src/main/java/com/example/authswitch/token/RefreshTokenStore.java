package com.example.authswitch.token;


import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class RefreshTokenStore {

    private final ConcurrentHashMap<String, String> refreshTokens = new ConcurrentHashMap<>();

    public void storeRefreshToken(String clientId, String refreshToken) {
        refreshTokens.put(clientId, refreshToken);
    }

    public String getRefreshToken(String clientId) {
        return refreshTokens.get(clientId);
    }

    public void removeRefreshToken(String clientId) {
        refreshTokens.remove(clientId);
    }
}
