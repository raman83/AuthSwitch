package com.example.authswitch.dto;


	
	import lombok.Data;
	import java.math.BigDecimal;

	@Data
	public class AuthRequest {
	    private String cardNumber;
	    private String expiryDate;
	    private String cvv;
	    private BigDecimal amount;
	    private String currency;
	    private String entryMethod; // swipe, chip, contactless, manual
	    private String ksn; // For DUKPT
	    private String encryptedTrackData;
	    private String mac;
	}


