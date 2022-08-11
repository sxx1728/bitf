package com.bitfye.common.crypto.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.core.io.Resource;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class CipherUtils {
    static final String CIPHER_AES = "AES/CBC/PKCS5Padding";
    static final SecureRandom secureRandom = new SecureRandom();

    public CipherUtils() {
    }

    static byte[] randomBytes(int size) {
        byte[] buffer = new byte[size];
        secureRandom.nextBytes(buffer);
        return buffer;
    }

    public static byte[] decodeBase64(String value) {
        return Base64.getDecoder().decode(value.getBytes(StandardCharsets.UTF_8));
    }

    public static String encodeBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] encryptAES(byte[] key, byte[] iv, byte[] input) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivps = new IvParameterSpec(iv);
        cipher.init(1, keySpec, ivps);
        return cipher.doFinal(input);
    }

    public static byte[] decryptAES(byte[] key, byte[] iv, byte[] input) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivps = new IvParameterSpec(iv);
        cipher.init(2, keySpec, ivps);
        return cipher.doFinal(input);
    }

    public static byte[] encryptRSA(PublicKey key, byte[] message) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(1, key);
        return cipher.doFinal(message);
    }

    public static byte[] encryptRSA(PrivateKey key, byte[] message) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(1, key);
        return cipher.doFinal(message);
    }

    public static byte[] decryptRSA(PublicKey key, byte[] input) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(2, key);
        return cipher.doFinal(input);
    }

    public static byte[] decryptRSA(PrivateKey key, byte[] input) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(2, key);
        return cipher.doFinal(input);
    }

    public byte[] signRSA(PrivateKey key, byte[] message) throws GeneralSecurityException {
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(key);
        signature.update(message);
        return signature.sign();
    }

    public boolean verifyRSA(PublicKey key, byte[] message, byte[] sign) throws GeneralSecurityException {
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initVerify(key);
        signature.update(message);
        return signature.verify(sign);
    }

    public static PrivateKey readRSAPrivateKey(Reader reader) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] encoded = (new PemReader(reader)).readPemObject().getContent();
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    public static PublicKey readRSAPublicKey(InputStream in) throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        X509Certificate cer = (X509Certificate)factory.generateCertificate(in);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(cer.getPublicKey().getEncoded());
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    public static PrivateKey readRSAPrivateKey(Resource resource) {
        try {
            Reader in = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            Throwable var2 = null;

            PrivateKey var3;
            try {
                var3 = readRSAPrivateKey((Reader)in);
            } catch (Throwable var13) {
                var2 = var13;
                throw var13;
            } finally {
                if (in != null) {
                    if (var2 != null) {
                        try {
                            in.close();
                        } catch (Throwable var12) {
                            var2.addSuppressed(var12);
                        }
                    } else {
                        in.close();
                    }
                }

            }

            return var3;
        } catch (Throwable var15) {
            throw new RuntimeException(var15);
        }
    }

    public static PrivateKey readRSAPrivateKey(String content) {
        try {
            Reader in = new StringReader(content);
            Throwable var2 = null;

            PrivateKey var3;
            try {
                var3 = readRSAPrivateKey((Reader)in);
            } catch (Throwable var13) {
                var2 = var13;
                throw var13;
            } finally {
                if (in != null) {
                    if (var2 != null) {
                        try {
                            in.close();
                        } catch (Throwable var12) {
                            var2.addSuppressed(var12);
                        }
                    } else {
                        in.close();
                    }
                }

            }

            return var3;
        } catch (Throwable var15) {
            throw new RuntimeException(var15);
        }
    }

    public static PublicKey readRSAPublicKey(Resource resource) {
        try {
            InputStream in = resource.getInputStream();
            Throwable var2 = null;

            PublicKey var3;
            try {
                var3 = readRSAPublicKey(in);
            } catch (Throwable var13) {
                var2 = var13;
                throw var13;
            } finally {
                if (in != null) {
                    if (var2 != null) {
                        try {
                            in.close();
                        } catch (Throwable var12) {
                            var2.addSuppressed(var12);
                        }
                    } else {
                        in.close();
                    }
                }

            }

            return var3;
        } catch (Throwable var15) {
            throw new RuntimeException(var15);
        }
    }

    public static PublicKey readRSAPublicKey(String content) {
        try {
            InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            Throwable var2 = null;

            PublicKey var3;
            try {
                var3 = readRSAPublicKey((InputStream)in);
            } catch (Throwable var13) {
                var2 = var13;
                throw var13;
            } finally {
                if (in != null) {
                    if (var2 != null) {
                        try {
                            in.close();
                        } catch (Throwable var12) {
                            var2.addSuppressed(var12);
                        }
                    } else {
                        in.close();
                    }
                }

            }

            return var3;
        } catch (Throwable var15) {
            throw new RuntimeException(var15);
        }
    }

    public static String toEncodedString(PublicKey key) {
        int len = 76;
        String s = encodeBase64(key.getEncoded());
        StringBuilder sb = new StringBuilder((s.length() / len + 3) * (len + 1));
        sb.append("-----BEGIN PUBLIC KEY-----\n");

        int end;
        for(end = len; end < s.length(); end += len) {
            sb.append(s.substring(end - len, end)).append('\n');
        }

        sb.append(s.substring(end - len, s.length())).append('\n');
        sb.append("-----END PUBLIC KEY-----");
        return sb.toString();
    }

    static {
        Security.addProvider(new BouncyCastleProvider());
    }
}

