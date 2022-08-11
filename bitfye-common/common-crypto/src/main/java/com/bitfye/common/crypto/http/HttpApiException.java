package com.bitfye.common.crypto.http;

import okhttp3.Request;
import okhttp3.Response;

public class HttpApiException extends RuntimeException {
    private final Response response;
    private String responseBody;

    public HttpApiException(Response response) {
        this(response, (String)null, (Throwable)null);
    }

    public HttpApiException(Response response, String responseBody) {
        this(response, responseBody, (Throwable)null);
    }

    public HttpApiException(Response response, String responseBody, Throwable cause) {
        super((String)null, cause, false, false);
        this.response = response;
        this.responseBody = responseBody;
    }

    @Override
    public String getMessage() {
        if (this.response != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("HTTP(").append(this.response.code()).append(") ").append(this.response.message());
            Request request = this.response.request();
            if (request != null) {
                sb.append("; ").append(request.method()).append(' ').append(request.url().toString());
            }

            return sb.toString();
        } else {
            Throwable cause = this.getCause();
            return cause != null ? cause.getMessage() : null;
        }
    }

    public Response getResponse() {
        return this.response;
    }

    public String getResponseBody() {
        return this.responseBody;
    }
}

