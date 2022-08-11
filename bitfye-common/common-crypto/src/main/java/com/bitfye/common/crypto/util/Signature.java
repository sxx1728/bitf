package com.bitfye.common.crypto.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Signature {
    static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
    static final ZoneId ZONE_GMT = ZoneId.of("Z");
    static final String SIGN_METHOD = "HmacSHA256";
    static final String SIGN_VERSION = "2";
    private final String appKey;
    private final SecretKeySpec appSecretKey;
    private static final Logger log = LoggerFactory.getLogger(Signature.class);

    public Signature(String appKey, String secretKey) {
        this.appKey = (String) Objects.requireNonNull(appKey);
        this.appSecretKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public Map<String, String> create(String method, String host, String encodedPath, Map<String, ?> params) {
        StringBuilder sb = new StringBuilder(1024);
        sb.append(method.toUpperCase()).append('\n').append(host.toLowerCase()).append('\n').append(encodedPath).append('\n');
        Map<String, String> result = new HashMap(5);
        result.put("AWSAccessKeyId", this.appKey);
        result.put("SignatureVersion", "2");
        result.put("SignatureMethod", "HmacSHA256");
        result.put("Timestamp", gmtNow());
        SortedMap<String, Object> sortedMap = new TreeMap(params);
        sortedMap.putAll(result);
        sortedMap.remove("Signature");
        Iterator var8 = sortedMap.entrySet().iterator();

        while(var8.hasNext()) {
            Map.Entry<String, ?> entry = (Map.Entry)var8.next();
            String key = (String)entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                sb.append(key).append('=').append(urlEncode(value.toString())).append('&');
            }
        }

        sb.setLength(sb.length() - 1);

        Mac hmacSha256;
        try {
            hmacSha256 = Mac.getInstance("HmacSHA256");
            hmacSha256.init(this.appSecretKey);
        } catch (NoSuchAlgorithmException var12) {
            throw new RuntimeException(var12);
        } catch (InvalidKeyException var13) {
            throw new RuntimeException(var13);
        }

        String payload = sb.toString();
        byte[] hash = hmacSha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        String actualSign = Base64.getEncoder().encodeToString(hash);
        result.put("Signature", actualSign);
        log.debug("signature={}, params={}", actualSign, params);
        return result;
    }

    private static String gmtNow() {
        return Instant.now().atZone(ZONE_GMT).format(DT_FORMAT);
    }

    private static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException var2) {
            throw new IllegalArgumentException("UTF-8 encoding not supported!");
        }
    }
}

