package com.example.authswitch.dto;

import lombok.Data;

@Data 
public class AuthResponse {
    private boolean approved;
    private String responseCode;
    private String message;
    private String authCode;
}