package com.bitfye.common.crypto.util;

import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Objects;

public class RSAKeyPair {
    public final PrivateKey privateKey;
    public final PublicKey publicKey;

    public RSAKeyPair(Resource privateKey, Resource publicKey) {
        this.privateKey = CipherUtils.readRSAPrivateKey(privateKey);
        this.publicKey = CipherUtils.readRSAPublicKey(publicKey);
    }

    public RSAKeyPair(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = (PrivateKey) Objects.requireNonNull(privateKey);
        this.publicKey = (PublicKey)Objects.requireNonNull(publicKey);
    }

    public String decrypt(String data) {
        try {
            int pos = data.indexOf(10);
            String encKey = data.substring(0, pos);
            byte[] aesKey = CipherUtils.decryptRSA(this.privateKey, CipherUtils.decodeBase64(encKey));
            if (aesKey.length != 32) {
                throw new IllegalArgumentException("invalid key size: " + aesKey.length);
            } else {
                pos = data.indexOf(10, encKey.length() + 1);
                String encIv = data.substring(encKey.length() + 1, pos);
                byte[] aesIv = CipherUtils.decryptRSA(this.privateKey, CipherUtils.decodeBase64(encIv));
                if (aesIv.length != 16) {
                    throw new IllegalArgumentException("invalid iv size: " + aesIv.length);
                } else {
                    byte[] message = CipherUtils.decodeBase64(data.substring(pos + 1));
                    byte[] decrypted = CipherUtils.decryptAES(aesKey, aesIv, message);
                    return new String(decrypted);
                }
            }
        } catch (Exception var9) {
            throw new IllegalArgumentException(var9);
        }
    }

    public String encrypt(String data) {
        try {
            byte[] aseKey = CipherUtils.randomBytes(32);
            byte[] aesIv = CipherUtils.randomBytes(16);
            byte[] message = data.getBytes(StandardCharsets.UTF_8);
            message = CipherUtils.encryptAES(aseKey, aesIv, message);
            aseKey = CipherUtils.encryptRSA(this.publicKey, aseKey);
            aesIv = CipherUtils.encryptRSA(this.publicKey, aesIv);
            String encKey = CipherUtils.encodeBase64(aseKey);
            String encIv = CipherUtils.encodeBase64(aesIv);
            String encMessage = CipherUtils.encodeBase64(message);
            StringBuilder sb = new StringBuilder(encMessage.length() + encKey.length() * 2 + 3);
            sb.append(encKey).append('\n').append(encIv).append('\n').append(encMessage).append('\n');
            return sb.toString();
        } catch (Exception var9) {
            throw new RuntimeException(var9);
        }
    }
}

