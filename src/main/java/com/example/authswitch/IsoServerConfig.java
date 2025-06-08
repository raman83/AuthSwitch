package com.example.authswitch;

import java.io.InputStream;

import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.*;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.iso.packager.ISO87APackager;
import org.jpos.util.ThreadPool;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
@Configuration
public class IsoServerConfig {

    @PostConstruct
    public void startIsoServer() {
        new Thread(() -> {
            try {
                // Load ISO 8583 packager from XML
               // InputStream is = getClass().getResourceAsStream("/iso87ascii.xml");
               // GenericPackager packager = new GenericPackager(is);

                // Create a thread pool for incoming connections
                ThreadPool pool = new ThreadPool(10, 100, "jPOS-ISO-Thread");

                // Create a ServerChannel prototype (ASCIIChannel)
                

ISOBasePackager packager = new ISO87APackager();
ServerChannel serverChannel = new ASCIIChannel(packager);

                // Now pass port, channel, and thread pool
                ISOServer isoServer = new ISOServer(5000, serverChannel, pool);

                // Add handler
                isoServer.addISORequestListener(new IsoMessageHandler());

                // Start it in a new thread
                new Thread(isoServer).start();

                System.out.println("✅ ISO Server started on port 5000");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
