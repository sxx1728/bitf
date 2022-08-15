package com.bitfye.wallet.cobo;

import okhttp3.*;
import okio.ByteString;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.TreeMap;
/**
 * @author ming.jia
 * @version 1.0
 * @description TODO
 * @date 2022/8/15 上午12:11
 **/
public class CoboTest {
    private static String API_KEY = "032730060f719d7251f6530e0027a2ed2c6e78b09dbcb7e841444d950caa5caf53\n";
    private static String API_SECRET = "a425cf8873d6b5bf35b76eba23a488b395f75b84179185949f23f233d12739fa";
    private static String HOST = "https://api.sandbox.cobo.com";
    private static String COBO_PUB = "032f45930f652d72e0c90f71869dfe9af7d713b1f67dc2f7cb51f9572778b9c876";
    private static String COBO_IP = "18.177.176.209";

    private static OkHttpClient HTTP_CLIENT = new OkHttpClient();

    private static byte[] doubleSha256(String content) {
        return Sha256Hash.hashTwice(content.getBytes());
    }

    private static byte[] hex2bytes(String s) {
        return ByteString.decodeHex(s).toByteArray();
    }

    private static String bytes2Hex(byte[] b) {
        return ByteString.of(b).hex();
    }

    private static String generateEccSignature(String content, String key) {
        ECKey eckey = ECKey.fromPrivate(hex2bytes(key));
        return bytes2Hex(eckey.sign(Sha256Hash.wrap(doubleSha256(content))).encodeToDER());
    }

    private static String composeParams(TreeMap<String, Object> params) {
        StringBuffer sb = new StringBuffer();
        params.forEach((s, o) -> {
            try {
                sb.append(s).append("=").append(URLEncoder.encode(String.valueOf(o), "UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private static boolean verifyResponse(String content, String sig, String pubkey) throws Exception {
        ECKey key = ECKey.fromPublicOnly(hex2bytes(pubkey));
        return key.verify(doubleSha256(content), hex2bytes(sig));
    }

    private static String request(String method, String path, TreeMap<String, Object> params, String apiKey, String apiSecret, String host) throws Exception {
        method = method.toUpperCase();
        String nonce = String.valueOf(System.currentTimeMillis());

        String paramString = composeParams(params);

        String content = method + "|" + path + "|" + nonce + "|" + paramString;

        String signature = generateEccSignature(content, apiSecret);

        Request.Builder builder = new Request.Builder()
                .addHeader("Biz-Api-Key", apiKey)
                .addHeader("Biz-Api-Nonce", nonce)
                .addHeader("Biz-Api-Signature", signature);
        Request request;
        if ("GET".equalsIgnoreCase(method)) {
            request = builder
                    .url(host + path + "?" + paramString)
                    .build();
        } else if ("POST".equalsIgnoreCase(method)) {
            FormBody.Builder bodyBuilder = new FormBody.Builder();
            params.forEach((s, o) -> bodyBuilder.add(s, String.valueOf(o)));
            RequestBody formBody = bodyBuilder.build();
            request = builder
                    .url(host + path)
                    .post(formBody)
                    .build();
        } else {
            throw new RuntimeException("not supported http method");
        }
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            String ts = response.header("BIZ_TIMESTAMP");
            String sig = response.header("BIZ_RESP_SIGNATURE");
            String body = response.body().string();
            boolean verifyResult = verifyResponse(body + "|" + ts, sig, COBO_PUB);
            System.out.println("verify success? " + verifyResult);
            if (!verifyResult) {
                throw new RuntimeException("verify response error");
            }
            return body;
        }
    }

    public static void main(String... args) throws Exception {
//        if (args.length == 1 && args[0].equals("key")){
//            testGenerateKeysAndSignMessage();
//        } else {
//            testApi();
//        }
        testApi();
    }


    public static void testApi() throws Exception {
        TreeMap<String, Object> params = new TreeMap<>();
        params.put("coin", "BTC");
        String res = request("GET", "/v1/custody/org_info/", params, API_KEY, API_SECRET, HOST);
        System.out.println(res);
    }

    public static void testGenerateKeysAndSignMessage() {
        ECKey key = new ECKey();
        String privHex = bytes2Hex(key.getPrivKeyBytes());
        String pubHex = bytes2Hex(key.getPubKey());
        System.out.println("API_KEY: " + pubHex + "; API_SECRET: " + privHex);
    }
}
