package com.bitfye.common.crypto.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.internal.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Primary
@Component
public class HttpConfig {
    private static final Logger log = LoggerFactory.getLogger(HttpConfig.class);
    private final String name;
    @Value("${http.connectTimeout:3000}")
    protected int connectTimeout = 3000;
    @Value("${http.readTimeout:3000}")
    protected int readTimeout = 3000;
    @Value("${http.writeTimeout:3000}")
    protected int writeTimeout = 3000;
    @Value("${http.maxIdleConnections:5}")
    protected int maxIdleConnections = 5;
    @Value("${http.keepAliveDuration:300}")
    protected int keepAliveDuration = 300;
    @Value("${http.threadPoolSize:100}")
    protected int threadPoolSize = 100;
    @Value("${http.threadAliveSecond:60}")
    protected int threadAliveSecond = 60;
    @Value("${http.maxRequests:64}")
    protected int maxRequests = 64;
    @Value("${http.maxRequestsPerHost:5}")
    protected int maxRequestsPerHost = 5;
    private Supplier<OkHttpClient> okHttpClient = this::createOkHttpClient;

    @Autowired
    private HttpConfig() {
        this.name = "default";
    }

    public OkHttpClient getOkHttpClient() {
        return (OkHttpClient)this.okHttpClient.get();
    }

    public HttpApi.Builder<String> createApiBuilder(ObjectMapper mapper) {
        return HttpApi.builder(this, mapper);
    }

    protected OkHttpClient createOkHttpClient() {
        OkHttpClient client = this.createOkHttpClientBuilder().build();
        log.info("build `{}` OkHttpClient: {}", this.name, this);
        this.okHttpClient = () -> {
            return client;
        };
        return client;
    }

    protected OkHttpClient.Builder createOkHttpClientBuilder() {
        return (new OkHttpClient.Builder()).followSslRedirects(false).followRedirects(false).connectTimeout((long)this.connectTimeout, TimeUnit.MILLISECONDS).readTimeout((long)this.readTimeout, TimeUnit.MILLISECONDS).writeTimeout((long)this.writeTimeout, TimeUnit.MILLISECONDS).connectionPool(this.createConnectionPool()).dispatcher(this.createDispatcher());
    }

    protected ConnectionPool createConnectionPool() {
        return new ConnectionPool(this.maxIdleConnections, (long)this.keepAliveDuration, TimeUnit.SECONDS);
    }

    protected ExecutorService createExecutorService() {
        return new ThreadPoolExecutor(0, this.threadPoolSize, (long)this.threadAliveSecond, TimeUnit.SECONDS, new SynchronousQueue(), Util.threadFactory("OkHttp Dispatcher", false));
    }

    protected Dispatcher createDispatcher() {
        Dispatcher dispatcher = new Dispatcher(this.createExecutorService());
        dispatcher.setMaxRequests(this.maxRequests);
        dispatcher.setMaxRequestsPerHost(this.maxRequestsPerHost);
        return dispatcher;
    }

    public String getName() {
        return this.name;
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    public int getReadTimeout() {
        return this.readTimeout;
    }

    public int getWriteTimeout() {
        return this.writeTimeout;
    }

    public int getMaxIdleConnections() {
        return this.maxIdleConnections;
    }

    public int getKeepAliveDuration() {
        return this.keepAliveDuration;
    }

    public int getThreadPoolSize() {
        return this.threadPoolSize;
    }

    public int getThreadAliveSecond() {
        return this.threadAliveSecond;
    }

    public int getMaxRequests() {
        return this.maxRequests;
    }

    public int getMaxRequestsPerHost() {
        return this.maxRequestsPerHost;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public void setMaxIdleConnections(int maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    public void setKeepAliveDuration(int keepAliveDuration) {
        this.keepAliveDuration = keepAliveDuration;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public void setThreadAliveSecond(int threadAliveSecond) {
        this.threadAliveSecond = threadAliveSecond;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public void setMaxRequestsPerHost(int maxRequestsPerHost) {
        this.maxRequestsPerHost = maxRequestsPerHost;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof HttpConfig)) {
            return false;
        } else {
            HttpConfig other = (HttpConfig)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                label71: {
                    Object this$name = this.getName();
                    Object other$name = other.getName();
                    if (this$name == null) {
                        if (other$name == null) {
                            break label71;
                        }
                    } else if (this$name.equals(other$name)) {
                        break label71;
                    }

                    return false;
                }

                if (this.getConnectTimeout() != other.getConnectTimeout()) {
                    return false;
                } else if (this.getReadTimeout() != other.getReadTimeout()) {
                    return false;
                } else if (this.getWriteTimeout() != other.getWriteTimeout()) {
                    return false;
                } else if (this.getMaxIdleConnections() != other.getMaxIdleConnections()) {
                    return false;
                } else if (this.getKeepAliveDuration() != other.getKeepAliveDuration()) {
                    return false;
                } else if (this.getThreadPoolSize() != other.getThreadPoolSize()) {
                    return false;
                } else if (this.getThreadAliveSecond() != other.getThreadAliveSecond()) {
                    return false;
                } else if (this.getMaxRequests() != other.getMaxRequests()) {
                    return false;
                } else if (this.getMaxRequestsPerHost() != other.getMaxRequestsPerHost()) {
                    return false;
                } else {
                    Object this$okHttpClient = this.getOkHttpClient();
                    Object other$okHttpClient = other.getOkHttpClient();
                    if (this$okHttpClient == null) {
                        if (other$okHttpClient != null) {
                            return false;
                        }
                    } else if (!this$okHttpClient.equals(other$okHttpClient)) {
                        return false;
                    }

                    return true;
                }
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof HttpConfig;
    }

    @Override
    public int hashCode() {
        boolean PRIME = true;
        int result = 1;
        Object $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        result = result * 59 + this.getConnectTimeout();
        result = result * 59 + this.getReadTimeout();
        result = result * 59 + this.getWriteTimeout();
        result = result * 59 + this.getMaxIdleConnections();
        result = result * 59 + this.getKeepAliveDuration();
        result = result * 59 + this.getThreadPoolSize();
        result = result * 59 + this.getThreadAliveSecond();
        result = result * 59 + this.getMaxRequests();
        result = result * 59 + this.getMaxRequestsPerHost();
        Object $okHttpClient = this.getOkHttpClient();
        result = result * 59 + ($okHttpClient == null ? 43 : $okHttpClient.hashCode());
        return result;
    }

    public HttpConfig(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "HttpConfig(name=" + this.getName() + ", connectTimeout=" + this.getConnectTimeout() + ", readTimeout=" + this.getReadTimeout() + ", writeTimeout=" + this.getWriteTimeout() + ", maxIdleConnections=" + this.getMaxIdleConnections() + ", keepAliveDuration=" + this.getKeepAliveDuration() + ", threadPoolSize=" + this.getThreadPoolSize() + ", threadAliveSecond=" + this.getThreadAliveSecond() + ", maxRequests=" + this.getMaxRequests() + ", maxRequestsPerHost=" + this.getMaxRequestsPerHost() + ")";
    }
}

