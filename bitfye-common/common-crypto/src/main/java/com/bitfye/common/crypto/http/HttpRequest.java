package com.bitfye.common.crypto.http;

import okhttp3.HttpUrl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HttpRequest<Response> {
    final HttpApi<Response> api;
    final HttpUrl url;
    final Map<String, String> parameters = new HashMap();
    final Map<String, String> headers = new HashMap();
    String method;
    Object data;

    public HttpRequest(HttpApi<Response> api, HttpUrl url) {
        this.api = api;
        this.url = url;
    }

    public String getPath() {
        return this.url.encodedPath();
    }

    public String getHeader(String name) {
        return (String)this.headers.get(name);
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public String getParameter(String name) {
        return (String)this.parameters.get(name);
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public HttpRequest<Response> setHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    public HttpRequest<Response> setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    public HttpRequest<Response> setParameter(String name, Object value) {
        if (value == null) {
            this.parameters.remove(name);
        } else {
            this.parameters.put(name, value.toString());
        }

        return this;
    }

    public HttpRequest<Response> setParameters(Map<String, ?> parameters) {
        if (!parameters.isEmpty()) {
            Iterator var2 = parameters.entrySet().iterator();

            while(var2.hasNext()) {
                Map.Entry<String, ?> entry = (Map.Entry)var2.next();
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

    public Response doGet() {
        return this.execute("GET", (Object)null);
    }

    public Response doPost(Object data) {
        return this.execute("POST", data);
    }

    public Response doHead() {
        return this.execute("HEAD", (Object)null);
    }

    public Response doDelete() {
        return this.execute("DELETE", (Object)null);
    }

    public Response doDelete(Object body) {
        return this.execute("DELETE", body);
    }

    public Response doPut(Object body) {
        return this.execute("PUT", body);
    }

    public Response doPatch(Object body) {
        return this.execute("PATCH", body);
    }

    public Response execute(String method, Object body) {
        this.method = method;
        this.data = body;
        return this.api.execute(this);
    }
}

