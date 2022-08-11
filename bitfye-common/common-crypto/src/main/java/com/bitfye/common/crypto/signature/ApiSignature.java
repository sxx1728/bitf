package com.bitfye.common.crypto.signature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class ApiSignature {
    final Logger log = LoggerFactory.getLogger(this.getClass());
    static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
    static final ZoneId ZONE_GMT = ZoneId.of("Z");
    static final long TIME_DIFF = 300L;
    Map<String, String> apps = new HashMap();

    public ApiSignature() {
    }

    public void setApps(Map<String, String> apps) {
        this.apps = new HashMap(apps);
    }

    public void createSignature(String appKey, String appSecretKey, String method, String host, String uri, Map<String, String> params) throws IOException {
        StringBuilder sb = new StringBuilder(1024);
        sb.append(method.toUpperCase()).append('\n').append(host.toLowerCase()).append('\n').append(uri).append('\n');
        params.remove("Signature");
        params.put("AWSAccessKeyId", appKey);
        params.put("SignatureVersion", "2");
        params.put("SignatureMethod", "HmacSHA256");
        params.put("Timestamp", this.gmtNow());
        SortedMap<String, String> map = new TreeMap(params);
        Iterator var9 = map.entrySet().iterator();

        String actualSign;
        while(var9.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry)var9.next();
            String key = (String)entry.getKey();
            actualSign = (String)entry.getValue();
            sb.append(key).append('=').append(urlEncode(actualSign)).append('&');
        }

        sb.deleteCharAt(sb.length() - 1);
        var9 = null;

        Mac hmacSha256;
        try {
            hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secKey = new SecretKeySpec(appSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(secKey);
        } catch (NoSuchAlgorithmException var15) {
            throw new BadSignatureException("No such algorithm: " + var15.getMessage());
        } catch (InvalidKeyException var16) {
            throw new BadSignatureException("Invalid key: " + var16.getMessage());
        }

        String payload = sb.toString();
        byte[] hash = hmacSha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        actualSign = Base64.getEncoder().encodeToString(hash);
        params.put("Signature", actualSign);
        this.log.debug("Dump parameters:");
        Iterator var13 = params.entrySet().iterator();

        while(var13.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry)var13.next();
            this.log.debug("  key: {}, value: {}", entry.getKey(), entry.getValue());
        }

    }

    protected StringBuilder prepare(HttpServletRequest request) {
        String serverName = request.getHeader("X-Forwarded-Host");
        if (serverName == null) {
            serverName = request.getHeader("Host");
            if (serverName == null) {
                throw new BadSignatureException("请求错误");
            }
        }

        StringBuilder sb = new StringBuilder(1024);
        sb.append(request.getMethod()).append('\n').append(serverName.toLowerCase()).append('\n').append(request.getRequestURI()).append('\n');
        return sb;
    }

    public void checkSignature(HttpServletRequest request) throws IOException {
        StringBuilder sb = this.prepare(request);
        Signature sign = this.appendFormalQueryString(sb, request);
        String appSecretKey = (String)this.apps.get(sign.accessKeyId);
        if (appSecretKey == null) {
            throw new BadSignatureException("Missing AWSAccessKeyId");
        } else if (!"2".equals(sign.signatureVersion)) {
            throw new BadSignatureException("Bad signature version for AWSAccessKeyId: " + sign.accessKeyId);
        } else if (!"HmacSHA256".equals(sign.signatureMethod)) {
            throw new BadSignatureException("Bad signature method for AWSAccessKeyId: " + sign.accessKeyId);
        } else if (sign.timestamp == null) {
            throw new BadSignatureException("Missing timestamp for AWSAccessKeyId: " + sign.accessKeyId);
        } else {
            ZonedDateTime zdt;
            try {
                zdt = LocalDateTime.parse(sign.timestamp, DT_FORMAT).atZone(ZONE_GMT);
                long epoch = zdt.toInstant().getEpochSecond();
                if (Math.abs(this.epochNow() - epoch) > 300L) {
                    throw new BadSignatureException("Bad timestamp: not current date time for AWSAccessKeyId: " + sign.accessKeyId);
                }
            } catch (DateTimeParseException var11) {
                throw new BadSignatureException("Bad format of timestamp for AWSAccessKeyId: " + sign.accessKeyId);
            }

            zdt = null;

            Mac hmacSha256;
            try {
                hmacSha256 = Mac.getInstance("HmacSHA256");
                SecretKeySpec secKey = new SecretKeySpec(appSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
                hmacSha256.init(secKey);
            } catch (NoSuchAlgorithmException var9) {
                throw new BadSignatureException("No such algorithm: " + var9.getMessage());
            } catch (InvalidKeyException var10) {
                throw new BadSignatureException("Invalid key: " + var10.getMessage());
            }

            String payload = sb.toString();
            byte[] hash = hmacSha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String actualSign = Base64.getEncoder().encodeToString(hash);
            this.log.debug("Payload: {}", payload);
            this.log.debug("expected sign: {}", sign.signature);
            this.log.debug("actual sign: {}", actualSign);
            if (!actualSign.equals(sign.signature)) {
                this.log.warn("Payload: {}, Expected sign: {}, actual sign: {}", new Object[]{Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8)), sign.signature, actualSign});
                throw new BadSignatureException("Signature NOT match for AWSAccessKeyId: " + sign.accessKeyId);
            }
        }
    }

    public void checkSignature(HttpServletRequest request, String remoteIp, ApiKey apiKey) throws IOException {
        StringBuilder sb = this.prepare(request);
        Signature sign = this.appendFormalQueryString(sb, request);
        if (apiKey != null && apiKey.secretKey != null) {
            if (!"2".equals(sign.signatureVersion)) {
                throw new BadSignatureException("错误的签名版本");
            } else if (!"HmacSHA256".equals(sign.signatureMethod)) {
                throw new BadSignatureException("错误的签名方法");
            } else if (sign.timestamp == null) {
                throw new BadSignatureException("提交时间不能为空");
            } else {
                ZonedDateTime zdt;
                try {
                    zdt = LocalDateTime.parse(sign.timestamp, DT_FORMAT).atZone(ZONE_GMT);
                    long epoch = zdt.toInstant().getEpochSecond();
                    if (Math.abs(this.epochNow() - epoch) > 300L) {
                        throw new BadSignatureException("无效的提交时间");
                    }
                } catch (DateTimeParseException var13) {
                    throw new BadSignatureException("提交时间格式错误");
                }

                if (StringUtils.isEmpty(apiKey.ip)) {
                    if (System.currentTimeMillis() / 1000L - apiKey.created > 7776000L) {
                        throw new BadSignatureException("密钥已经过期");
                    }
                } else {
                    if (remoteIp == null) {
                        throw new BadSignatureException("ip地址错误");
                    }

                    boolean isValidIp = false;
                    String[] var15 = apiKey.ip.split(",");
                    int var8 = var15.length;

                    for(int var9 = 0; var9 < var8; ++var9) {
                        String ip = var15[var9];
                        if (ip.equals(remoteIp)) {
                            isValidIp = true;
                            break;
                        }
                    }

                    if (!isValidIp) {
                        throw new BadSignatureException("ip地址错误");
                    }
                }

                zdt = null;

                Mac hmacSha256;
                try {
                    hmacSha256 = Mac.getInstance("HmacSHA256");
                    SecretKeySpec secKey = new SecretKeySpec(apiKey.secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
                    hmacSha256.init(secKey);
                } catch (NoSuchAlgorithmException var11) {
                    throw new BadSignatureException("No such algorithm: " + var11.getMessage());
                } catch (InvalidKeyException var12) {
                    throw new BadSignatureException("Invalid key: " + var12.getMessage());
                }

                String payload = sb.toString();
                byte[] hash = hmacSha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));
                String actualSign = Base64.getEncoder().encodeToString(hash);
                this.log.debug("Payload: {}", payload);
                this.log.debug("expected sign: {}", sign.signature);
                this.log.debug("actual sign: {}", actualSign);
                if (!actualSign.equals(sign.signature)) {
                    this.log.warn("Payload: {}, Expected sign: {}, actual sign: {}", new Object[]{Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8)), sign.signature, actualSign});
                    throw new BadSignatureException("校验失败");
                }
            }
        } else {
            throw new BadSignatureException("公钥错误");
        }
    }

    Signature appendFormalQueryString(StringBuilder sb, HttpServletRequest request) throws IOException {
        Signature sign = new Signature();
        SortedMap<String, String[]> params = new TreeMap(request.getParameterMap());
        Iterator var5 = params.entrySet().iterator();

        while(true) {
            while(var5.hasNext()) {
                Map.Entry<String, String[]> entry = (Map.Entry)var5.next();
                String key = (String)entry.getKey();
                String value = ((String[])entry.getValue())[0];
                byte var10 = -1;
                switch(key.hashCode()) {
                    case -1801215079:
                        if (key.equals("SignatureMethod")) {
                            var10 = 4;
                        }
                        break;
                    case -1217415016:
                        if (key.equals("Signature")) {
                            var10 = 0;
                        }
                        break;
                    case -607018720:
                        if (key.equals("SignatureVersion")) {
                            var10 = 3;
                        }
                        break;
                    case 187716025:
                        if (key.equals("AWSAccessKeyId")) {
                            var10 = 2;
                        }
                        break;
                    case 2059094262:
                        if (key.equals("Timestamp")) {
                            var10 = 1;
                        }
                }

                switch(var10) {
                    case 0:
                        sign.signature = value;
                        continue;
                    case 1:
                        sign.timestamp = value;
                        break;
                    case 2:
                        sign.accessKeyId = value;
                        break;
                    case 3:
                        sign.signatureVersion = value;
                        break;
                    case 4:
                        sign.signatureMethod = value;
                }

                sb.append(key).append('=').append(urlEncode(value)).append('&');
            }

            if (!params.isEmpty()) {
                sb.deleteCharAt(sb.length() - 1);
            }

            return sign;
        }
    }

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException var2) {
            throw new IllegalArgumentException("UTF-8 encoding not supported!");
        }
    }

    public static String urlEncode(Map<String, String> params) {
        List<String> list = (List)params.keySet().stream().map((key) -> {
            return key + "=" + urlEncode((String)params.get(key));
        }).collect(Collectors.toList());
        return String.join("&", list);
    }

    long epochNow() {
        return Instant.now().getEpochSecond();
    }

    String gmtNow() {
        return Instant.ofEpochSecond(this.epochNow()).atZone(ZONE_GMT).format(DT_FORMAT);
    }
}

