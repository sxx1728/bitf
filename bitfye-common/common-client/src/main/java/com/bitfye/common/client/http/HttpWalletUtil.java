package com.bitfye.common.client.http;

import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author ming.jia
 * @version 1.0
 * @description TODO
 * @date 2021/11/20 下午9:45
 **/
@Slf4j
@SpringBootConfiguration
public class HttpWalletUtil {
    static OkHttpClient oc = null;

    static int connectTimeoutSeconds = 5;
    static int readTimeoutSeconds = 5;
    static int writeTimeoutSeconds = 5;

    @Value("${http.connect-timeout-seconds:5}")
    public void setConnectTimeoutSeconds(int value) {
        connectTimeoutSeconds = value;
    }
    @Value("${http.read-timeout-seconds:5}")
    public void setReadTimeoutSeconds(int value) {
        readTimeoutSeconds = value;
    }
    @Value("${http.write-timeout-seconds:5}")
    public void setWriteTimeoutSeconds(int value) {
        writeTimeoutSeconds = value;
    }


    static void init() {
        if (oc != null) {
            return;
        }
        oc = new OkHttpClient.Builder()
                .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS))
                .sslSocketFactory(TrustAll.socketFactory(), new TrustAll.trustManager())
                .hostnameVerifier(new TrustAll.hostnameVerifier())
                .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(writeTimeoutSeconds, TimeUnit.SECONDS)
                .addInterceptor(HttpLogger.getLoggingInterceptor())
                .build();
    }

    public static OkHttpClient getConnection() {
        init();
        return oc;
    }
}
