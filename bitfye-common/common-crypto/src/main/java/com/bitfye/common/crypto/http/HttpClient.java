package com.bitfye.common.crypto.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Primary
@Component
public class HttpClient {
    final OkHttpClient client;
    final ObjectMapper mapper;

    public static HttpClient singleton() {
        return HttpClient.LazyHolder.SINGLETON;
    }

    public HttpClient(OkHttpClient client) {
        this.client = (OkHttpClient) Objects.requireNonNull(client);
        this.mapper = new ObjectMapper();
    }

    public HttpClient(HttpConfig config) {
        this(config, Optional.empty());
    }

    @Autowired
    public HttpClient(HttpConfig config, Optional<ObjectMapper> mapper) {
        this.client = config.getOkHttpClient();
        this.mapper = (ObjectMapper)mapper.orElseGet(ObjectMapper::new);
    }

    public HttpApi.Builder<String> apiBuilder() {
        return HttpApi.builder(this.client, this.mapper);
    }

    public HttpApi.Builder<String> apiBuilder(ObjectMapper mapper) {
        return HttpApi.builder(this.client, mapper);
    }

    private static class LazyHolder {
        static final HttpClient SINGLETON = new HttpClient(new OkHttpClient());

        private LazyHolder() {
        }
    }
}

