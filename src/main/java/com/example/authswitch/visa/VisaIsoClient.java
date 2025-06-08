package com.example.authswitch.visa;

import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.packager.ISO87APackager;
import org.springframework.stereotype.Component;

@Component
public class VisaIsoClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 5000;

    public ISOMsg send(ISOMsg request) throws Exception {
        ISOChannel channel = new ASCIIChannel(HOST, PORT, new ISO87APackager());
        channel.connect();
        channel.send(request);
        ISOMsg response = channel.receive();
        channel.disconnect();
        return response;
    }
}
