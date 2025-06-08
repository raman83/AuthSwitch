package com.example.authswitch;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;

public class IsoMessageHandler implements ISORequestListener {

    @Override
    public boolean process(ISOSource source, ISOMsg m) {
        try {
        	System.out.println("💬 Received MTI: " + m.getMTI());
            System.out.println("🔐 PAN Field 2: " + m.getString(2));
            System.out.println("💵 Amount Field 4: " + m.getString(4));
            System.out.println("📅 Expiry Field 14: " + m.getString(14));
            System.out.println("🔢 Processing Code Field 3: " + m.getString(3));
            System.out.println("📟 Terminal ID Field 41: " + m.getString(41));
            // TODO: Add your business logic here (validate, authorize, respond)

            // Example: send a simple response message (copy MTI with +10)
            ISOMsg response = (ISOMsg) m.clone();
            response.setResponseMTI();
            response.set(39, "00"); // 00 = Approved
            source.send(response);

            return true; // processed successfully

        } catch (Exception e) {
            e.printStackTrace(); // Log the error
            return false; // Let jPOS know processing failed
        }
    }
}
