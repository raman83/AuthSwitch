package com.example.authswitch.visa;

import com.example.authswitch.dto.AuthRequest;
import com.example.authswitch.dto.AuthResponse;
import org.springframework.stereotype.Component;

@Component
public class VisaSimulatorClient {

    public AuthResponse sendToVisa(AuthRequest request) {
        AuthResponse response = new AuthResponse();
        if (request.getAmount().doubleValue() < 100.00) {
            response.setApproved(true);
            response.setResponseCode("00");
            response.setMessage("Approved");
            response.setAuthCode("123456");
        } else {
            response.setApproved(false);
            response.setResponseCode("05");
            response.setMessage("Declined");
        }
        return response;
    }
}