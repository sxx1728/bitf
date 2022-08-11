package com.bitfye.common.client.http;

import lombok.extern.slf4j.Slf4j;
import okhttp3.logging.HttpLoggingInterceptor;

@Slf4j
public class HttpLogger implements HttpLoggingInterceptor.Logger {

    public static HttpLoggingInterceptor getLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new HttpLogger());
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }

    @Override
    public void log(String message) {
        log.info(message.length() > 1000 ? message.substring(0, 1000): message);
    }
}
