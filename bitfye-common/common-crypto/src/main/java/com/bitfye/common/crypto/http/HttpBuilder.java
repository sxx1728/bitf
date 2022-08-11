package com.bitfye.common.crypto.http;

import com.bitfye.common.crypto.encrypt.AesUtil;
import com.bitfye.common.crypto.encrypt.RSAKeyPair;
import com.bitfye.common.crypto.signature.ApiSignature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import okhttp3.Request.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/** @deprecated */
@Deprecated
public class HttpBuilder {
    private static final Map<TimeoutConfig, OkHttpClient> CLIENT_CACHE = new ConcurrentHashMap();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    static final MediaType JSON = MediaType.parse("application/json");
    static ObjectMapper MAPPER = new ObjectMapper();
    final String scheme;
    final String method;
    final String host;
    final int port;
    final String path;
    Map<String, String> queryParams;
    Object postData;
    ObjectMapper mapper;
    Map<String, String> headerMap;
    private HttpBuilder.TimeoutConfig timeoutConfig;
    String appKeyId;
    String appKeySecret;
    RSAKeyPair myRsaKeyPair;
    RSAKeyPair heRsaKeyPair;

    private HttpBuilder(String method, String scheme, String host, int port, String path) {
        this.mapper = MAPPER;
        this.timeoutConfig = new HttpBuilder.TimeoutConfig();
        this.method = method;
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.path = path;
    }

    public static HttpBuilder get(String url) throws IOException {
        return parse("GET", url);
    }

    public static HttpBuilder post(String url) throws IOException {
        return parse("POST", url);
    }

    static HttpBuilder parse(String method, String aUrl) throws IOException {
        URL url = new URL(aUrl);
        return new HttpBuilder(method, url.getProtocol(), url.getHost(), url.getPort(), url.getPath());
    }

    public HttpBuilder withMapper(ObjectMapper mapper) {
        this.mapper = mapper;
        return this;
    }

    public HttpBuilder withParameters(Map<String, String> params) {
        this.queryParams = new HashMap(params);
        return this;
    }

    public HttpBuilder withHeaders(Map<String, String> headerMap) {
        this.headerMap = new HashMap(headerMap);
        return this;
    }

    public HttpBuilder withSignature(String appKeyId, String appKeySecret) {
        this.appKeyId = appKeyId;
        this.appKeySecret = appKeySecret;
        return this;
    }

    public HttpBuilder withHeRSAKeyPair(RSAKeyPair rsaKeyPair) {
        this.heRsaKeyPair = rsaKeyPair;
        return this;
    }

    public HttpBuilder withMyRSAKeyPair(RSAKeyPair rsaKeyPair) {
        this.myRsaKeyPair = rsaKeyPair;
        return this;
    }

    public String encodePostData() throws JsonProcessingException {
        if (this.method.equals("POST")) {
            String json = this.mapper.writeValueAsString(this.postData);
            if (this.heRsaKeyPair != null) {
                byte[] aseKey = AesUtil.randomKey();
                byte[] encIv = AesUtil.randomIV();

                byte[] encMessage;
                byte[] enAseKey;
                byte[] enEncIv;
                try {
                    encMessage = AesUtil.encrypt(aseKey, encIv, json.getBytes("UTF-8"));
                    enAseKey = this.heRsaKeyPair.encryptByPublicKey(aseKey);
                    enEncIv = this.heRsaKeyPair.encryptByPublicKey(encIv);
                } catch (Exception var8) {
                    throw new RuntimeException(var8);
                }

                String enPostData = "";
                enPostData = enPostData + Base64.getEncoder().encodeToString(enAseKey) + "\n";
                enPostData = enPostData + Base64.getEncoder().encodeToString(enEncIv) + "\n";
                enPostData = enPostData + Base64.getEncoder().encodeToString(encMessage) + "\n";
                return enPostData;
            } else {
                return json;
            }
        } else {
            throw new IllegalArgumentException("Only post can encode data.");
        }
    }

    public String decodeResponseDate(String response) {
        if (this.myRsaKeyPair == null) {
            return response;
        } else {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8))));
                String encKey = br.readLine();
                String encIv = br.readLine();
                StringBuilder sb = new StringBuilder(8192);
                char[] buffer = new char[1024];
                boolean var7 = false;

                int n;
                while((n = br.read(buffer)) != -1) {
                    sb.append(buffer, 0, n);
                }

                String encMessage = sb.toString();
                byte[] aesKey = this.myRsaKeyPair.decryptByPrivateKey(Base64.getDecoder().decode(encKey.getBytes(StandardCharsets.UTF_8)));
                byte[] aesIv = this.myRsaKeyPair.decryptByPrivateKey(Base64.getDecoder().decode(encIv.getBytes(StandardCharsets.UTF_8)));
                if (aesKey.length != 32) {
                    throw new IllegalArgumentException("invalid key size: " + aesKey.length);
                } else if (aesIv.length != 16) {
                    throw new IllegalArgumentException("invalid iv size: " + aesIv.length);
                } else {
                    byte[] decrypted = AesUtil.decrypt(aesKey, aesIv, Base64.getDecoder().decode(encMessage.getBytes(StandardCharsets.UTF_8)));
                    return new String(decrypted);
                }
            } catch (Exception var12) {
                throw new IllegalArgumentException(var12);
            }
        }
    }

    public HttpBuilder data(Object data) throws IOException {
        if (this.method.equals("POST")) {
            this.postData = data;
            return this;
        } else {
            throw new IllegalArgumentException("Only post can send data.");
        }
    }

    public String prepareCall() throws IOException {
        String url = this.scheme + "://" + this.host;
        if (this.port > 0) {
            url = url + ":" + this.port + this.path;
        } else {
            url = url + this.path;
        }

        if (this.appKeyId != null) {
            if (this.queryParams == null) {
                this.queryParams = new HashMap();
            }

            ApiSignature apiSignature = new ApiSignature();
            apiSignature.createSignature(this.appKeyId, this.appKeySecret, this.method, this.host.toLowerCase(), this.path, this.queryParams);
        }

        if (this.queryParams != null) {
            url = url + "?" + ApiSignature.urlEncode(this.queryParams);
        }

        return url;
    }

    public String call() throws IOException {
        String url = this.prepareCall();
        this.logger.debug("{}: {}", this.method, url);
        Long startTime = System.currentTimeMillis();
        Request request = null;
        if ("POST".equals(this.method)) {
            String json = this.encodePostData();
            RequestBody body = RequestBody.create(JSON, json);
            request = (new Builder()).headers(this.headers()).url(url).post(body).build();
        } else {
            request = (new Builder()).headers(this.headers()).url(url).get().build();
        }

        Response response = this.buildOkHttpClient().newCall(request).execute();
        String s = response.body().string();
        Long diffTime = System.currentTimeMillis() - startTime;
        if (diffTime > 2000L) {
            this.logger.warn("{}: {}, timeMillis: {}", new Object[]{this.method, url, diffTime});
        }

        return this.decodeResponseDate(s);
    }

    private Headers headers() {
        Headers.Builder builder = new Headers.Builder();
        if (this.headerMap != null && !this.headerMap.isEmpty()) {
            Iterator var2 = this.headerMap.keySet().iterator();

            while(var2.hasNext()) {
                String key = (String)var2.next();
                builder.add(key, (String)this.headerMap.get(key));
            }

            return builder.build();
        } else {
            return builder.build();
        }
    }

    OkHttpClient buildOkHttpClient() {
        OkHttpClient client = (OkHttpClient)CLIENT_CACHE.get(this.timeoutConfig);
        if (client == null) {
            OkHttpClient.Builder b = new OkHttpClient.Builder();
            b.connectTimeout((long)this.timeoutConfig.connect, TimeUnit.SECONDS);
            b.readTimeout((long)this.timeoutConfig.read, TimeUnit.SECONDS);
            b.writeTimeout((long)this.timeoutConfig.write, TimeUnit.SECONDS);
            client = b.build();
            CLIENT_CACHE.put(this.timeoutConfig, client);
        }

        return client;
    }

    public HttpBuilder setConnectTimeout(int connectTimeout) {
        this.timeoutConfig.connect = connectTimeout;
        return this;
    }

    public HttpBuilder setReadTimeout(int readTimeout) {
        this.timeoutConfig.read = readTimeout;
        return this;
    }

    public HttpBuilder setWriteTimeout(int writeTimeout) {
        this.timeoutConfig.write = writeTimeout;
        return this;
    }

    private static class TimeoutConfig {
        private int connect = 10;
        private int read = 10;
        private int write = 10;

        public TimeoutConfig() {
        }

        public int getConnect() {
            return this.connect;
        }

        public int getRead() {
            return this.read;
        }

        public int getWrite() {
            return this.write;
        }

        public void setConnect(int connect) {
            this.connect = connect;
        }

        public void setRead(int read) {
            this.read = read;
        }

        public void setWrite(int write) {
            this.write = write;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof HttpBuilder.TimeoutConfig)) {
                return false;
            } else {
                HttpBuilder.TimeoutConfig other = (HttpBuilder.TimeoutConfig)o;
                if (!other.canEqual(this)) {
                    return false;
                } else if (this.getConnect() != other.getConnect()) {
                    return false;
                } else if (this.getRead() != other.getRead()) {
                    return false;
                } else {
                    return this.getWrite() == other.getWrite();
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof HttpBuilder.TimeoutConfig;
        }

        @Override
        public int hashCode() {
            boolean PRIME = true;
            int result = 1;
            result = result * 59 + this.getConnect();
            result = result * 59 + this.getRead();
            result = result * 59 + this.getWrite();
            return result;
        }

        @Override
        public String toString() {
            return "HttpBuilder.TimeoutConfig(connect=" + this.getConnect() + ", read=" + this.getRead() + ", write=" + this.getWrite() + ")";
        }
    }
}

