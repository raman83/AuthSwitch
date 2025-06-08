package com.example.authswitch.service;

import com.example.authswitch.dto.AuthRequest;
import com.example.authswitch.dto.AuthResponse;
import com.example.authswitch.visa.VisaSimulatorClient;
import com.example.authswitch.crypto.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final VisaSimulatorClient visaClient;
    private final EncryptionService encryptionService;

    public AuthResponse authorize(AuthRequest request) {
        // Decrypt data if encrypted
        if (request.getEntryMethod().equals("swipe")) {
            String decryptedData = encryptionService.decryptTrackData(
                request.getEncryptedTrackData(), request.getKsn());
            System.out.println("Decrypted Track Data: " + decryptedData);
        }

        // TODO: MAC validation

        // Send to Visa simulator
        return visaClient.sendToVisa(request);
    }
}