package com.bitfye.common.client.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.internal.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.httpclient.DefaultOkHttpClientFactory;
import org.springframework.cloud.commons.httpclient.OkHttpClientFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


@Data
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
@ToString(exclude = "okHttpClient")
public class HttpClientConfig {

    private final String name;

    private final OkHttpClientFactory clientFactory;

    @Value("${http.disableSslValidation:false}")
    protected boolean disableSslValidation = false;

    @Value("${http.connectTimeout:5000}")
    protected int connectTimeout = 5000;

    @Value("${http.readTimeout:5000}")
    protected int readTimeout = 5000;

    @Value("${http.writeTimeout:5000}")
    protected int writeTimeout = 5000;

    @Value("${http.maxIdleConnections:5}")
    protected int maxIdleConnections = 5;

    @Value("${http.keepAliveDuration:300}")
    protected int keepAliveDuration = 5 * 60;

    @Value("${http.threadPoolSize:100}")
    protected int threadPoolSize = 100;

    @Value("${http.threadAliveSecond:60}")
    protected int threadAliveSecond = 60;

    @Value("${http.maxRequests:400}")
    protected int maxRequests = 400;

    @Value("${http.maxRequestsPerHost:200}")
    protected int maxRequestsPerHost = 200;

    @Setter(AccessLevel.NONE)
    private Supplier<OkHttpClient> okHttpClient = this::createOkHttpClient;

    @Autowired
    private HttpClientConfig() {
        this("default");
    }

    public HttpClientConfig(String name) {
        this(name, new OkHttpClient.Builder());
    }

    public HttpClientConfig(String name, OkHttpClient.Builder builder) {
        this.name = name;
        this.clientFactory = new DefaultOkHttpClientFactory(builder);
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient.get();
    }

    public HttpApiBuilder<String> createApiBuilder(ObjectMapper mapper) {
        return HttpApi.builder(this, mapper);
    }

    protected OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder builder = clientFactory.createBuilder(disableSslValidation);
        configure(builder);

        OkHttpClient client = builder.build();
        log.info("build `{}` OkHttpClient: {}", name, this);

        okHttpClient = () -> client;
        return client;
    }

    protected void configure(OkHttpClient.Builder builder) {
        builder.followSslRedirects(false)
                .followRedirects(false)
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
                .connectionPool(createConnectionPool())
                .dispatcher(createDispatcher());
    }

    protected ConnectionPool createConnectionPool() {
        return new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.SECONDS);
    }

    protected ExecutorService createExecutorService() {
        return new ThreadPoolExecutor(0, threadPoolSize, threadAliveSecond,
                TimeUnit.SECONDS, new SynchronousQueue<>(), Util.threadFactory("OkHttp Dispatcher", false));
    }

    protected Dispatcher createDispatcher() {
        Dispatcher dispatcher = new Dispatcher(createExecutorService());
        dispatcher.setMaxRequests(maxRequests);
        dispatcher.setMaxRequestsPerHost(maxRequestsPerHost);

        return dispatcher;
    }
}
