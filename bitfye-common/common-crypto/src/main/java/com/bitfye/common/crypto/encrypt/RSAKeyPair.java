package com.bitfye.common.crypto.encrypt;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAKeyPair {
    final PrivateKey privateKey;
    final PublicKey publicKey;

    public RSAKeyPair(String privateKey, String publicKey) {
        this((Reader)(new StringReader(privateKey)), (Reader)(new StringReader(publicKey)));
    }

    public RSAKeyPair(Reader privateKey, Reader publicKey) {
        this.privateKey = readPrivateKey(privateKey);
        this.publicKey = readPublicKey(publicKey);
    }

    public RSAKeyPair(String publicKey) {
        this((Reader)(new StringReader(publicKey)));
    }

    public RSAKeyPair(Reader publicKey) {
        this.privateKey = null;
        this.publicKey = readPublicKey(publicKey);
    }

    static PrivateKey readPrivateKey(Reader reader) {
        try {
            PemReader preader = new PemReader(reader);
            Throwable var2 = null;

            PrivateKey var7;
            try {
                PemObject po = preader.readPemObject();
                byte[] encoded = po.getContent();
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                var7 = kf.generatePrivate(keySpec);
            } catch (Throwable var17) {
                var2 = var17;
                throw var17;
            } finally {
                if (preader != null) {
                    if (var2 != null) {
                        try {
                            preader.close();
                        } catch (Throwable var16) {
                            var2.addSuppressed(var16);
                        }
                    } else {
                        preader.close();
                    }
                }

            }

            return var7;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException var19) {
            throw new RuntimeException(var19);
        }
    }

    static PrivateKey readPrivateKey(String text) {
        return readPrivateKey((Reader)(new StringReader(text)));
    }

    static PublicKey readPublicKey(Reader reader) {
        try {
            PemReader preader = new PemReader(reader);
            Throwable var2 = null;

            PublicKey var7;
            try {
                PemObject po = preader.readPemObject();
                byte[] encoded = po.getContent();
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                var7 = kf.generatePublic(keySpec);
            } catch (Throwable var17) {
                var2 = var17;
                throw var17;
            } finally {
                if (preader != null) {
                    if (var2 != null) {
                        try {
                            preader.close();
                        } catch (Throwable var16) {
                            var2.addSuppressed(var16);
                        }
                    } else {
                        preader.close();
                    }
                }

            }

            return var7;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException var19) {
            throw new RuntimeException(var19);
        }
    }

    static PublicKey readPublicKey(String text) {
        return readPublicKey((Reader)(new StringReader(text)));
    }

    public byte[] encryptByPrivateKey(byte[] message) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(1, this.privateKey);
        return cipher.doFinal(message);
    }

    public byte[] decryptByPrivateKey(byte[] input) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(2, this.privateKey);
        return cipher.doFinal(input);
    }

    public byte[] decryptByPublicKey(byte[] input) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(2, this.publicKey);
        return cipher.doFinal(input);
    }

    public byte[] encryptByPublicKey(byte[] message) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(1, this.publicKey);
        return cipher.doFinal(message);
    }

    public byte[] sign(byte[] message) throws GeneralSecurityException {
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(this.privateKey);
        signature.update(message);
        return signature.sign();
    }

    public boolean verify(byte[] message, byte[] sign) throws GeneralSecurityException {
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initVerify(this.publicKey);
        signature.update(message);
        return signature.verify(sign);
    }

    public String getEncodedPublicKey() {
        StringBuilder sb = new StringBuilder(4096);
        sb.append("-----BEGIN PUBLIC KEY-----\n");

        for(String s = Base64.getEncoder().encodeToString(this.publicKey.getEncoded()); s.length() > 76; s = s.substring(76)) {
            sb.append(s.substring(0, 76)).append('\n');
        }

        sb.append("-----END PUBLIC KEY-----");
        return sb.toString();
    }

    static {
        Security.addProvider(new BouncyCastleProvider());
    }
}

