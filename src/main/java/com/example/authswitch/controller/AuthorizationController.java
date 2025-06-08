// REST controller for /authorize endpoint
package com.example.authswitch.controller;

import com.example.authswitch.dto.AuthRequest;
import com.example.authswitch.dto.AuthResponse;
import com.example.authswitch.service.AuthorizationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/transaction")
public class AuthorizationController {

   
   
    @Autowired
    private AuthorizationService authorizationService;
    


    @PostMapping("/authorize")
    public ResponseEntity<?> authorize(@RequestBody AuthRequest request) {
        String ksn = request.getKsn();
        String encryptedPan = request.getEncryptedTrackData();

        if (ksn == null || encryptedPan == null) {
            return ResponseEntity.badRequest().body("Missing ksn or encryptedPan");
        }

        AuthResponse authResponse = new AuthResponse();

        if (ksn == null || encryptedPan == null) {
        	authResponse.setApproved(false);
        	authResponse.setMessage("Missing ksn or encryptedPan");
        	authResponse.setResponseCode("91");
            return ResponseEntity.badRequest().body(authResponse);
        }

        try {
            // Derive the session DEK (Data Encryption Key) using DUKPT from the KSN
        	authResponse= authorizationService.authorize(request);

            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            e.printStackTrace(); // Log full stack trace for internal debugging
            authResponse.setApproved(false);
            authResponse.setMessage("Decryption failed. Check logs for details.");
            authResponse.setResponseCode("96");
            return ResponseEntity.status(500).body(authResponse);
        }
    }
}
