package com.bitfye.common.crypto.encrypt;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;


public class AesUtil {
    static final String CIPHER_NAME = "AES/CBC/PKCS5Padding";
    static final SecureRandom secureRandom = new SecureRandom();

    public AesUtil() {
    }

    public static byte[] encrypt(byte[] key, byte[] iv, byte[] input) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivps = new IvParameterSpec(iv);
        cipher.init(1, keySpec, ivps);
        return cipher.doFinal(input);
    }

    public static byte[] decrypt(byte[] key, byte[] iv, byte[] input) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivps = new IvParameterSpec(iv);
        cipher.init(2, keySpec, ivps);
        return cipher.doFinal(input);
    }

    public static byte[] randomKey() {
        return randomBytes(32);
    }

    public static byte[] randomIV() {
        return randomBytes(16);
    }

    static byte[] randomBytes(int size) {
        byte[] buffer = new byte[size];
        secureRandom.nextBytes(buffer);
        return buffer;
    }
}

