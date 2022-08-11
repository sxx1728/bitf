package com.bitfye.common.client.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * @since 2019-11-29
 */
@RequiredArgsConstructor
public class HttpResponse<T> {

    @Getter
    private final HttpRequest<T> request;

    private final Response response;

    @Getter
    @Setter
    String bodyString;

    @Getter
    @Setter
    T body;

    @Getter
    @Setter
    Throwable error;

    public HttpResponse(HttpRequest<T> request, Throwable error) {
        this.request = request;
        this.response = null;
        this.error = error;
    }

    public boolean isSuccessful() {
        return response.isSuccessful();
    }

    public int getCode() {
        return response.code();
    }

    public Headers getHeaders() {
        return response.headers();
    }

    public long getNetworkTime() {
        return response.receivedResponseAtMillis() - response.sentRequestAtMillis();
    }
}
