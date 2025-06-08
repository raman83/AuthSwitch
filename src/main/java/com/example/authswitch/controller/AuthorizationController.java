// REST controller for /authorize endpoint
package com.example.authswitch.controller;

import com.example.authswitch.crypto.dukpt.DukptService;
import com.example.authswitch.dto.AuthResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/transaction")
public class AuthorizationController {

    /**
     * DukptService handles DUKPT (Derived Unique Key Per Transaction) logic:
     * - Deriving IPEK (Initial PIN Encryption Key) using the Base Derivation Key (BDK) and Key Serial Number (KSN).
     * - Using XOR masking with a fixed C0C0C0... mask to derive the second half of the IPEK.
     * - Padding transaction counter or PAN blocks with zeros using padRight to fit the required block size (8 bytes for 3DES).
     * - Performing Triple DES encryption (DESede/ECB/NoPadding), which requires exact block alignment with no padding.
     * This service enables secure derivation and decryption of transaction data as per PCI standards.
     */
    @Autowired
    private DukptService dukptService;

    @PostMapping("/authorize")
    public ResponseEntity<?> authorize(@RequestBody Map<String, String> payload) {
        String ksn = payload.get("ksn");
        String encryptedPan = payload.get("encryptedPan");

        if (ksn == null || encryptedPan == null) {
            return ResponseEntity.badRequest().body("Missing ksn or encryptedPan");
        }

        AuthResponse response = new AuthResponse();

        if (ksn == null || encryptedPan == null) {
            response.setApproved(false);
            response.setMessage("Missing ksn or encryptedPan");
            response.setResponseCode("91");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Derive the session DEK (Data Encryption Key) using DUKPT from the KSN
            String dek = dukptService.deriveKey(ksn);
            System.out.println("[DEBUG] Derived DEK: " + dek);

            // Decrypt the PAN block using the derived session key
          String panBlock = dukptService.decryptPanBlock(dek, encryptedPan);
           // String panBlock = dukptService.tripleDesEncrypt(dek, "1123456789012345");
            System.out.println("[DEBUG] Decrypted PAN Block: " + panBlock);

            response.setApproved(true);
            response.setAuthCode("123456");
            response.setMessage("Transaction Approved");
            response.setResponseCode("00");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // Log full stack trace for internal debugging
            response.setApproved(false);
            response.setMessage("Decryption failed. Check logs for details.");
            response.setResponseCode("96");
            return ResponseEntity.status(500).body(response);
        }
    }
}
