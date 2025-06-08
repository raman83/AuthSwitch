package com.example.authswitch;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.ISO87APackager;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.channel.ASCIIChannel;

public class IsoTestClient {

    public static void main(String[] args) throws Exception {
        // Connect to your Spring Boot ISO 8583 listener
        ISOChannel channel = new ASCIIChannel("127.0.0.1", 5000, new ISO87APackager());

        channel.connect();

        ISOMsg isoMsg = new ISOMsg();
        isoMsg.setPackager(new ISO87APackager());

        isoMsg.setMTI("0200");
        isoMsg.set(2, "4111111111111111"); // PAN
        isoMsg.set(3, "000000");           // Processing code
        isoMsg.set(4, "000000005000");     // Amount: 50.00
        isoMsg.set(7, "0608123456");       // Transmission date/time
        isoMsg.set(11, "123456");          // STAN
        isoMsg.set(14, "2504");            // Expiry
        isoMsg.set(22, "012");             // POS Entry Mode
        isoMsg.set(41, "TERMID01");        // Terminal ID
        isoMsg.set(42, "MERCH01");         // Merchant ID

        // Send it
        channel.send(isoMsg);
        System.out.println("✅ Sent ISO 8583 message");

        // Read response
        ISOMsg response = channel.receive();
        System.out.println("📥 Response MTI: " + response.getMTI());
        System.out.println("📥 Response code (field 39): " + response.getString(39));

        channel.disconnect();
    }
}
