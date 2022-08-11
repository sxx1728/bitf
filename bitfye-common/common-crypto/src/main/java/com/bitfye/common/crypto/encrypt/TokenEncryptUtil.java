package com.bitfye.common.crypto.encrypt;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenEncryptUtil {
    static final String CIPHER_NAME = "AES/ECB/PKCS5Padding";
    static final byte[] KEY = generateKey();
    static final Pattern ORIGIN_TOKEN = Pattern.compile("^default_([a-f0-9]{32})$");

    public TokenEncryptUtil() {
    }

    static byte[] generateKey() {
        long SEED = 4506576770988285202L;
        long multiplier = 25214903917L;
        long addend = 11L;
        long mask = 281474976710655L;
        long seed = 4506576770988285202L;
        byte[] buffer = new byte[16];

        for(int i = 0; i < 16; ++i) {
            seed = seed * 25214903917L + 11L & 281474976710655L;
            buffer[i] = (byte)((int)seed);
        }

        return buffer;
    }

    public static String encryptToken(String plain) throws GeneralSecurityException {
        Object var1 = null;

        byte[] enc;
        try {
            enc = encrypt(plain.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalArgumentException var3) {
            throw new GeneralSecurityException();
        }

        return Base64.getUrlEncoder().withoutPadding().encodeToString(enc);
    }

    public static String decryptToken(String encrypted) throws GeneralSecurityException {
        Matcher m = ORIGIN_TOKEN.matcher(encrypted);
        if (m.matches()) {
            return m.group(1);
        } else {
            Object var2 = null;

            try {
                byte[] enc = Base64.getUrlDecoder().decode(encrypted);
                byte[] dec = decrypt(enc);
                return new String(dec, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException var4) {
                throw new GeneralSecurityException();
            }
        }
    }

    static byte[] encrypt(byte[] input) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(KEY, "AES");
        cipher.init(1, keySpec);
        return cipher.doFinal(input);
    }

    static byte[] decrypt(byte[] input) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(KEY, "AES");
        cipher.init(2, keySpec);
        return cipher.doFinal(input);
    }
}

