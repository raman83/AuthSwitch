Spring Boot POS Authorization Switch
This project is a Java Spring Boot-based POS Authorization Switch, capable of receiving encrypted card transaction requests, decrypting them using DUKPT, constructing ISO 8583 messages, and interacting with a Visa Simulator for authorization. It emulates a basic payment switch for testing and educational purposes.


Features
🔐 Dukpt Decryption of Encrypted PAN / Track Data

🧾 ISO 8583 Message Construction using jPOS

🌐 Visa Simulator Integration for end-to-end transaction authorization

🚀 Spring Boot REST API for accepting encrypted payment requests

🧪 Logging of ISO 8583 Request/Response with MTI, Field values, and approval status


 Sample Request Payload
 {
  "cardNumber": "4111111111111111",
  "expiryDate": "2504",
  "cvv": "123",
  "amount": 50.00,
  "currency": "CAD",
  "entryMethod": "swipe",
  "ksn": "FFFF9876543210E00001",
  "encryptedTrackData": "e780110e7979e241",
  "mac": "1234567890ABCDEF"
}





Transaction Flow
Client sends an encrypted payment request (includes encrypted PAN/Track2 + KSN).

Spring Boot controller accepts the request and:

Decrypts the PAN using DUKPT and derived Data Encryption Key (DEK).

Constructs an ISO 8583 message using jPOS.

Sends the message to the Visa Simulator.

The Visa Simulator responds with an authorization response (Field 39).

The system maps the ISO response to a simplified response object:



{
  "approved": true,
  "responseCode": "00",
  "message": "Approved",
  "authCode": "123456"
}
