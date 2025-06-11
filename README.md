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



🔐 1. Terminal Registration Flow
Terminals (e.g. Postman or actual devices) initiate secure registration:

POST /register

Sends a terminal ID and an encrypted registration code (using a session key)

Backend:

Decrypts the code

Verifies it against a hardcoded/shared secret

If valid, generates and returns:

client_id

client_secret

These are used to obtain OAuth2 tokens

🔑 2. OAuth2 Token Issuance
Once registered, the terminal uses:

POST /oauth/token

With client_id and client_secret using client_credentials grant

Response includes:

access_token (JWT)

refresh_token

Token expiry metadata

A JWT-based token system was implemented using jjwt (via jjwt-api, jjwt-impl, jjwt-jackson).

🔁 3. Token Refresh Support
POST /oauth/token/refresh

Uses the refresh token to issue a new access token

Helps terminals avoid token expiration during critical payment flows

🧾 4. ISO 8583 Handling with jPOS
The app has an ISO 8583 listener (Option B) integrated directly into the Spring Boot app via jPOS.

Terminal sends a payment payload mapped to ISO 8583 format.

Steps:

Payload → ISO 8583 message

Message sent to Visa simulator

Response mapped back → JSON → returned to terminal

🔐 5. PAN Decryption (DUKPT)
Implemented a DukptService to decrypt PAN data encrypted by the terminal

Uses session key from BDK + KSN

Includes MAC validation and DUKPT key derivation logic

🧩 Dependencies Used
jjwt for JWT token creation/validation

jakarta.annotation-api for @PostConstruct

spring-boot-starter-security for OAuth2

jPOS for ISO 8583 messaging

(Optional) spring-boot-starter-data-redis if you want token/refresh token persistence

🛠️ Services and Components
TerminalRegistrationController

ClientCredentialService

JwtTokenService

DukptService

Iso8583MessageMapper

VisaSimulatorClient

RefreshTokenStore (in-memory or persistent)

🔄 Typical Terminal Flow
POST /register → gets client_id / client_secret

POST /oauth/token → gets access & refresh tokens

Sends payments → access token in Authorization header

If token expired → uses /oauth/token/refresh

All communication encrypted and token-protected




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
