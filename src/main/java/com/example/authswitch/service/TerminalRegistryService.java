package com.example.authswitch.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.authswitch.crypto.dukpt.DukptService;
import com.example.authswitch.dto.TerminalCredentials;
import com.example.authswitch.dto.TerminalRegisterRequest;



@Service
public class TerminalRegistryService {
	
	

@Autowired
private DukptService dukptService;


	
	
    private final Map<String, TerminalCredentials> registered = new ConcurrentHashMap<>();

    private static final String EXPECTED_CODE = "1123456789012345";

    public boolean validateAndRegister(TerminalRegisterRequest request) {
    	
        String decrypted = dukptService.decryptPanBlock("8ac293faa1315b4d8ac293faa1315b4d8ac293faa1315b4d", request.getEncryptedCode());
        return EXPECTED_CODE.equals(decrypted);
    }

    public TerminalCredentials generateClientCredentials(String terminalId) {
        String clientId = UUID.randomUUID().toString();
        String clientSecret = UUID.randomUUID().toString();
        TerminalCredentials creds = new TerminalCredentials(clientId, clientSecret);
        registered.put(clientId, creds);
        return creds;
    }

    public Optional<TerminalCredentials> getCredentials(String terminalId) {
        return Optional.ofNullable(registered.get(terminalId));
    }

    public boolean validateClient(String clientId, String clientSecret) {
        return registered.containsKey(clientId) &&
        		registered.get(clientId).getClientSecret().equals(clientSecret);
    }
}
