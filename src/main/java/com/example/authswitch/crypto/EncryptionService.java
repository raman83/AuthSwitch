package com.example.authswitch.crypto;

import org.springframework.stereotype.Component;
import com.example.authswitch.crypto.dukpt.DukptService;
import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor

public class EncryptionService {

	  private final DukptService dukptService;

	    public String decryptTrackData(String encryptedData, String ksn) {
	        // Stubbed: in real implementation we’ll decrypt with key derived from KSN
	        String derivedKey = dukptService.deriveKey(ksn);
	        System.out.println("[Stub] Using derived key: " + derivedKey);
	        return "4111111111111111=25042010000000000000"; // Stub
	    }

	    public boolean validateMAC(String data, String mac, String ksn) {
	        // TODO: Validate MAC using HSM or MAC key
	        return true; // Stub
	    }
}
