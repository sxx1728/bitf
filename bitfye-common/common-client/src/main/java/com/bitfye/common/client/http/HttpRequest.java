package com.bitfye.common.client.http;


import lombok.Getter;
import okhttp3.HttpUrl;

import java.util.HashMap;
import java.util.Map;


public class HttpRequest<T> {

    final HttpApi<T> api;

    @Getter
    final HttpUrl url;

    @Getter
    final Map<String, String> parameters = new HashMap<>();

    @Getter
    final Map<String, String> headers = new HashMap<>();

    @Getter
    String method;

    @Getter
    Object data;

    public HttpRequest(HttpApi<T> api, HttpUrl url) {
        this.api = api;
        this.url = url;
    }

    public String getOriginPath() {
        return api.originPath;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public HttpRequest<T> setHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    public HttpRequest<T> setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    public HttpRequest<T> setParameter(String name, Object value) {
        if (value == null) {
            parameters.remove(name);
        } else {
            parameters.put(name, value.toString());
        }
        return this;
    }

    public HttpRequest<T> setParameters(Map<String, ?> parameters) {
        if (!parameters.isEmpty()) {
            for (Map.Entry<String, ?> entry : parameters.entrySet()) {
                Object value = entry.getValue();
                if (value == null) {
                    this.parameters.remove(entry.getKey());
                } else {
                    this.parameters.put(entry.getKey(), value.toString());
                }
            }
        }
        return this;
    }

    public HttpResponse<T> doGet() {
        return execute("GET", null);
    }

    public HttpResponse<T> doPost(Object data) {
        return execute("POST", data);
    }

    public HttpResponse<T> execute(String method, Object body) {
        this.method = method;
        this.data = body;
        return api.execute(this);
    }
}
