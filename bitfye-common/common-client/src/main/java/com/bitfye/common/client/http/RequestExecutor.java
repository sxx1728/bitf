package com.bitfye.common.client.http;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.net.SocketTimeoutException;
import java.util.Set;

/**
 * @since 2019-11-29
 */
@Slf4j
@RequiredArgsConstructor
public abstract class RequestExecutor<T>
        implements RequestBodyBuilder, ResponseBodyHandler<T> {

    public HttpResponse<T> execute(HttpApi<T> api, HttpRequest<T> request) {
        HttpResponse<T> response = null;
        try {
            val req = buildRequest(api, request);
            val resp = doExecute(api, req);
            val bodyString = resp.body().string();

            response = new HttpResponse<>(request, resp);
            response.setBodyString(bodyString);

            if (resp.isSuccessful()) {
                T body = handleResponseBody(request, response);
                response.setBody(body);
            }
        } catch (Exception e) {
            log.error("HttpResponse error", e);
            if (response == null) {
                response = new HttpResponse(request, e);
            } else {
                response.setError(e);
            }
        }
        return response;
    }

    protected Response doExecute(HttpApi<T> api, Request req) throws Exception {
        return api.client.newCall(req).execute();
    }

    protected Request buildRequest(HttpApi<T> api, HttpRequest<T> request) throws Exception {
        val builder = api.builder(request);
        if (request.data == null) {
            builder.method(request.method, null);
        } else {
            RequestBody body = buildRequestBody(request);
            builder.method(request.method, body);
        }
        return builder.build();
    }

    @FunctionalInterface
    public interface Factory {

        @SuppressWarnings("rawtypes")
        RequestExecutor create(HttpApiBuilder builder);
    }

    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Default<T> extends RequestExecutor<T> {

        @Delegate
        private final RequestBodyBuilder requestBodyBuilder;

        @Delegate
        private final ResponseBodyHandler<T> responseBodyHandler;

        public Default(HttpApiBuilder<T> builder) {
            this.requestBodyBuilder = builder.getRequestBodyBuilder();
            this.responseBodyHandler = builder.getResponseBodyHandler();
        }
    }

    public static class RetryOnTimeout<T> extends Default<T> {

        private final int retryTimes;

        public RetryOnTimeout(HttpApiBuilder<T> builder, int retryTimes) {
            super(builder);
            this.retryTimes = retryTimes;
        }

        @Override
        protected Response doExecute(HttpApi<T> api, Request req) throws Exception {
            int retry = retryTimes;
            SocketTimeoutException ex;
            do {
                try {
                    return super.doExecute(api, req);
                } catch (SocketTimeoutException e) {
                    ex = e;
                }
            } while (--retry >= 0);

            throw ex;
        }
    }

    public static class RetryOnCode<T> extends Default<T> {

        private final int retryTimes;
        private final Set<Integer> retryResponseCodes;

        public RetryOnCode(HttpApiBuilder<T> builder, int retryTimes, Set<Integer> retryResponseCodes) {
            super(builder);
            this.retryTimes = retryTimes;
            this.retryResponseCodes = retryResponseCodes;
        }

        @Override
        protected Response doExecute(HttpApi<T> api, Request req) throws Exception {
            int retry = retryTimes;
            Response resp;
            do {
                resp = super.doExecute(api, req);
                if (!retryResponseCodes.contains(resp.code())) {
                    return resp;
                }
            } while (--retry >= 0);
            return resp;
        }
    }
}
