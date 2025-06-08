package com.example.authswitch.crypto.dukpt;

import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.HexFormat;

@Service
public class DukptService {


    // 16-byte (32 hex chars) Base Derivation Key (BDK)
    private static final String BDK = "0123456789ABCDEFFEDCBA9876543210";
    private static final HexFormat hex = HexFormat.of();

    /**
     * Derives a working data key (DEK) from BDK and KSN using DUKPT steps.
     * This is the same DEK the terminal would derive using its IPEK.
     * @param ksn Key Serial Number (hex string)
     * @return derived 24-byte (192 bit) session (data) key (hex)
     */
    public String deriveKey(String ksn) {
        String ipek = deriveIPEK(ksn);
        System.out.println("[DEBUG] IPEK: " + ipek);

        String sessionKey = deriveDataKey(ipek, ksn);
        System.out.println("[DEBUG] Derived session key (before padding): " + sessionKey);

        // Pad session key to 24 bytes (48 hex chars) for DESede 3-key 3DES
        String sessionKey24 = padKeyTo24Bytes(sessionKey);
        System.out.println("[DEBUG] Session key padded to 24 bytes: " + sessionKey24);
        return sessionKey24;
    }

    /**
     * Derives IPEK from BDK and the left 10 bytes of KSN (KSN base).
     * @param ksn Full KSN including transaction counter
     * @return IPEK as 32-hex char string (16 bytes)
     */
    private String deriveIPEK(String ksn) {
        String ksnLeft8Bytes = ksn.substring(0, 16); // First 8 bytes (16 hex)
        System.out.println("[DEBUG] KSN left 8 bytes: " + ksnLeft8Bytes);

        // Left half BDK (16 hex chars = 8 bytes)
        String leftKey = BDK; // full 16-byte key (32 hex chars)
        String xorMask = "C0C0C0C000000000C0C0C0C000000000";
        String rightKey = xorHex(leftKey, xorMask);

        System.out.println("[DEBUG] Left Key for IPEK: " + leftKey);
        System.out.println("[DEBUG] Right Key (XOR masked): " + rightKey);

        String ipekLeft = tripleDesEncrypt(leftKey, ksnLeft8Bytes);
        String ipekRight = tripleDesEncrypt(rightKey, ksnLeft8Bytes);

        String ipek = ipekLeft + ipekRight;
        System.out.println("[DEBUG] IPEK (left + right): " + ipek);
        return ipek;
    }

    /**
     * Derives a session key (DEK) using IPEK and KSN.
     * NOTE: Simplified demo derivation.
     * @param ipek 32 hex chars (16 bytes)
     * @param ksn full KSN string
     * @return 32 hex char key (16 bytes)
     */
    private String deriveDataKey(String ipek, String ksn) {
        // Extract transaction counter from last 6 hex digits
    	 // Example simplification: XOR padded counter with IPEK (not production-secure, demo only)
        String transactionCounter = ksn.substring(ksn.length() - 6);
        String paddedCounter = padRight(transactionCounter, 16); // 8 bytes in hex
        System.out.println("[DEBUG] Padded Transaction Counter: " + paddedCounter);

        String keyBlock = xorHex(ipek.substring(0, 16), paddedCounter); // 8 bytes XOR
        String extendedKey = keyBlock + keyBlock + keyBlock;  // 16 + 16 + 16 = 48 hex chars

        System.out.println("[DEBUG] Session Key 24-byte: " + extendedKey);
        return extendedKey;
    }

    /**
     * Encrypt plaintext using 3DES (DESede/ECB/NoPadding) with a hex key.
     * @param hexKey 32 or 48 hex chars (16 or 24 bytes)
     * @param hexData 16 hex chars (8 bytes) block to encrypt
     * @return encrypted hex string
     */
    public String tripleDesEncrypt(String hexKey, String hexData) {
        try {
            byte[] keyBytes = expandTo24Bytes(hex.parseHex(hexKey));
            byte[] dataBytes = hex.parseHex(hexData);
            SecretKeySpec key = new SecretKeySpec(keyBytes, "DESede");
            Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(dataBytes);
            return hex.formatHex(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("3DES error: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypt encrypted data block using 3DES session key.
     * @param dekHex 48 hex chars (24 bytes)
     * @param encryptedBlockHex 16 hex chars (8 bytes)
     * @return decrypted hex string
     */
    public String decryptPanBlock(String dekHex, String encryptedBlockHex) {
        try {
            byte[] keyBytes = expandTo24Bytes(hex.parseHex(dekHex));
            byte[] dataBytes = hex.parseHex(encryptedBlockHex);
            SecretKeySpec key = new SecretKeySpec(keyBytes, "DESede");
            Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(dataBytes);
            return hex.formatHex(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("3DES decrypt error: " + e.getMessage(), e);
        }
    }
    
    
    private byte[] expandTo24Bytes(byte[] keyBytes) {
        if (keyBytes.length == 16) {
            byte[] extended = new byte[24];
            System.arraycopy(keyBytes, 0, extended, 0, 16);
            System.arraycopy(keyBytes, 0, extended, 16, 8); // Repeat first 8 bytes
            return extended;
        }
        if (keyBytes.length == 24) return keyBytes;
        throw new IllegalArgumentException("Invalid key length for padding: " + keyBytes.length);
    }
    /**
     * XOR two equal-length hex strings.
     */
    private String xorHex(String a, String b) {
        byte[] ba = hex.parseHex(a);
        byte[] bb = hex.parseHex(b);
        byte[] result = new byte[ba.length];
        for (int i = 0; i < ba.length; i++) {
            result[i] = (byte) (ba[i] ^ bb[i]);
        }
        return hex.formatHex(result);
    }

    /**
     * Pad a string on the right with '0' to reach length n.
     */
    private String padRight(String s, int n) {
        while (s.length() < n) s += "0";
        return s;
    }

    /**
     * Pads a 16-byte (32 hex chars) key to 24 bytes (48 hex chars) by appending
     * the first 8 bytes (16 hex chars) again - required for 3DES 3-key mode.
     * If already 48 chars, returns as is.
     */
    private String padKeyTo24Bytes(String keyHex) {
        if (keyHex.length() == 48) return keyHex; // Already 24 bytes
        if (keyHex.length() == 32) {
            // Append first 16 hex chars to make 48 chars
            return keyHex + keyHex.substring(0, 16);
        }
        throw new IllegalArgumentException("Invalid key length for padding: " + keyHex.length());
}

}