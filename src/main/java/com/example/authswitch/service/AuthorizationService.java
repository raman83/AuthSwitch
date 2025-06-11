package com.example.authswitch.service;

import com.example.authswitch.dto.AuthRequest;
import com.example.authswitch.dto.AuthResponse;
import com.example.authswitch.visa.VisaIsoClient;
import com.example.authswitch.crypto.dukpt.DukptService;

import lombok.RequiredArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.ISO87APackager;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;


@Service
@RequiredArgsConstructor
public class AuthorizationService {

   
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
	
    
    @Autowired
    private VisaIsoClient visaIsoClient;

    public AuthResponse authorize(AuthRequest request) throws Exception {
        
    	 String dek = dukptService.deriveKey(request.getKsn());
         System.out.println("[DEBUG] Derived DEK: " + dek);

         // Decrypt the PAN block using the derived session key
        String decryptedPan = dukptService.decryptPanBlock(dek, request.getEncryptedTrackData());
        // String panBlock = dukptService.tripleDesEncrypt(dek, "1123456789012345");
         System.out.println("[DEBUG] Decrypted PAN Block: " + decryptedPan);
         
         AuthResponse authResponse = new AuthResponse();

         
      // 🧱 Build ISO Message
         ISOMsg isoMsg = new ISOMsg();
         isoMsg.setPackager(new ISO87APackager());
         isoMsg.setMTI("0200");
         isoMsg.set(2, decryptedPan);
         isoMsg.set(3, "000000");
         isoMsg.set(4, request.getAmount().toString());
         isoMsg.set(7, new SimpleDateFormat("MMddHHmmss").format(new Date()));
         isoMsg.set(11, "123456");
         isoMsg.set(14, request.getExpiryDate());
         isoMsg.set(49, "124");
         isoMsg.set(41, "TERM1234");

         
         // 🔁 Send to Visa Simulator
         ISOMsg response = visaIsoClient.send(isoMsg);

         // ✅ Map response to DTO
         String responseCode = response.getString(39);
         authResponse.setResponseCode(responseCode);
         authResponse.setAuthCode(response.hasField(38) ? response.getString(38) : null);
         authResponse.setApproved("00".equals(responseCode));
         authResponse.setMessage("00".equals(responseCode) ? "Transaction Approved" : "Transaction Declined");
		return authResponse;
    }
}