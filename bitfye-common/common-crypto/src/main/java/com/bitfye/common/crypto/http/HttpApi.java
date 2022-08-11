package com.bitfye.common.crypto.http;

import com.bitfye.common.crypto.util.RSAKeyPair;
import com.bitfye.common.crypto.util.Signature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class HttpApi<T> {
    protected static final Logger logger = LoggerFactory.getLogger(HttpApi.class);
    protected static final MediaType APPLICATION_JSON = MediaType.parse("application/json");
    protected final HttpUrl url;
    protected final OkHttpClient client;
    protected final ObjectMapper mapper;
    protected final HttpApi.Converter<T> converter;
    protected final HttpApi.Interceptor interceptor;

    private HttpApi(HttpUrl url, HttpApi.Builder<T> builder) {
        this.url = (HttpUrl) Objects.requireNonNull(url);
        this.client = (OkHttpClient)Objects.requireNonNull(builder.client);
        this.mapper = (ObjectMapper)Objects.requireNonNull(builder.mapper);
        this.converter = (HttpApi.Converter)Objects.requireNonNull(builder.converter);
        this.interceptor = builder.interceptor != null ? builder.interceptor : HttpApi.Interceptor.DEFAULT;
    }

    public final HttpRequest<T> request() {
        return this.request(this.url);
    }

    public final HttpRequest<T> request(String path) {
        return this.request(this.url.newBuilder().encodedPath(path).build());
    }

    public HttpRequest<T> request(HttpUrl url) {
        return new HttpRequest(this, url);
    }

    public T doGet(Map<String, ?> params) {
        return this.request().setParameters(params).doGet();
    }

    public T doPost(Map<String, ?> params, Object data) {
        return this.request().setParameters(params).doPost(data);
    }

    public T doPost(Object data) {
        return this.request().doPost(data);
    }

    protected HttpUrl buildRequestUrl(String method, HttpRequest<T> request) {
        HttpUrl url = request.url;
        Map<String, String> params = request.parameters;
        if (params.isEmpty()) {
            return url;
        } else {
            HttpUrl.Builder builder = url.newBuilder();
            addQueryParameter(builder, params);
            return builder.build();
        }
    }

    protected RequestBody buildRequestBody(String data, HttpRequest<T> request) throws IOException {
        return RequestBody.create(APPLICATION_JSON, data);
    }

    protected Request buildRequest(HttpRequest<T> request) {
        Request.Builder requestBuilder = new Request.Builder();
        String method = request.method;
        Object data = request.data;

        try {
            HttpUrl url = this.buildRequestUrl(method, request);
            if (data == null) {
                requestBuilder.url(url).method(method, (RequestBody)null);
                logger.debug("{} {}", method, url);
            } else {
                String json = this.mapper.writeValueAsString(data);
                RequestBody body = this.buildRequestBody(json, request);
                requestBuilder.url(url).method(method, body);
                logger.debug("{} {}, body={}", new Object[]{method, url, json});
            }

            if (!request.headers.isEmpty()) {
                Iterator var9 = request.headers.entrySet().iterator();

                while(var9.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry)var9.next();
                    requestBuilder.addHeader((String)entry.getKey(), (String)entry.getValue());
                }
            }
        } catch (Throwable var8) {
            throw new HttpApiException((Response)null, (String)null, var8);
        }

        return requestBuilder.build();
    }

    protected T execute(HttpRequest<T> request) {
        Response response = null;
        String body = null;

        try {
            response = this.client.newCall(this.buildRequest(request)).execute();
            body = response.body().string();
            if (response.isSuccessful()) {
                body = this.interceptor.intercept(request, response, body);
                T data = this.converter.convert(body);
                logger.debug("response: {}", body);
                return data;
            }
        } catch (Throwable var7) {
            if (response != null) {
                try {
                    response.body().source().close();
                } catch (Throwable var6) {
                    var7.addSuppressed(var6);
                }
            }

            throw new HttpApiException(response, body, var7);
        }

        throw new HttpApiException(response, body);
    }

    protected static void addQueryParameter(HttpUrl.Builder builder, Map<String, String> params) {
        Iterator var2 = params.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry)var2.next();
            builder.addQueryParameter((String)entry.getKey(), (String)entry.getValue());
        }

    }

    public static HttpApi.Builder<String> builder(HttpConfig config) {
        return builder(config, new ObjectMapper());
    }

    public static HttpApi.Builder<String> builder(HttpConfig config, ObjectMapper mapper) {
        Objects.requireNonNull(config, "config required");
        Objects.requireNonNull(mapper, "mapper required");
        return new HttpApi.Builder(config.getOkHttpClient(), mapper, HttpApi.Builder.DEFAULT_CONVERTER);
    }

    public static HttpApi.Builder<String> builder(OkHttpClient client, ObjectMapper mapper) {
        Objects.requireNonNull(client, "client required");
        Objects.requireNonNull(mapper, "mapper required");
        return new HttpApi.Builder(client, mapper, HttpApi.Builder.DEFAULT_CONVERTER);
    }

    public static class Builder<T> {
        private static final HttpApi.Converter<String> DEFAULT_CONVERTER = (str) -> {
            return str;
        };
        protected final OkHttpClient client;
        protected final ObjectMapper mapper;
        protected final HttpApi.Converter<T> converter;
        protected final HttpApi.Interceptor interceptor;

        protected Builder(OkHttpClient client, ObjectMapper mapper, HttpApi.Converter<T> converter) {
            this.client = client;
            this.mapper = mapper;
            this.converter = converter;
            this.interceptor = null;
        }

        public final <R> HttpApi.Builder<R> response(Class<R> type) {
            ObjectMapper mapper = this.mapper;
            return this.response((str) -> {
                return mapper.readValue(str, type);
            });
        }

        public final <R> HttpApi.Builder<R> response(TypeReference<R> type) {
            ObjectMapper mapper = this.mapper;
            return this.response((str) -> {
                return mapper.readValue(str, type);
            });
        }

        public final <R> HttpApi.Builder<R> response(HttpApi.Converter<R> converter) {
            Objects.requireNonNull(converter, "converter required");
            return new HttpApi.Builder(this.client, this.mapper, converter, this.interceptor);
        }

        public final HttpApi.Builder<T> interceptor(HttpApi.Interceptor interceptor) {
            return new HttpApi.Builder(this.client, this.mapper, this.converter, interceptor);
        }

        public HttpApi<T> build(String url) {
            HttpUrl httpUrl = (HttpUrl)Objects.requireNonNull(HttpUrl.parse(url), "invalid url: " + url);
            return new HttpApi(httpUrl, this);
        }

        public HttpApi<T> buildWithCipher(String url, RSAKeyPair rsaKeyPair) {
            HttpUrl httpUrl = (HttpUrl)Objects.requireNonNull(HttpUrl.parse(url), "invalid url: " + url);
            HttpApi.Builder builder;
            if (this.interceptor == null) {
                builder = this.interceptor((request, response, body) -> {
                    return rsaKeyPair.decrypt(body);
                });
            } else {
                HttpApi.Interceptor originInterceptor = this.interceptor;
                builder = this.interceptor((request, response, body) -> {
                    String decrypted = rsaKeyPair.decrypt(body);
                    return originInterceptor.intercept(request, response, decrypted);
                });
            }

            return new HttpApi.CipherApi(httpUrl, builder, rsaKeyPair);
        }

        public HttpApi<T> buildWithSignature(String url, Signature signature) {
            HttpUrl httpUrl = (HttpUrl)Objects.requireNonNull(HttpUrl.parse(url), "invalid url: " + url);
            Objects.requireNonNull(signature);
            if (httpUrl.querySize() == 0) {
                return new HttpApi.SignatureApi(httpUrl, this, signature);
            } else {
                final Map<String, String> queryParameters = new HashMap();
                Iterator var5 = httpUrl.queryParameterNames().iterator();

                while(var5.hasNext()) {
                    String name = (String)var5.next();
                    queryParameters.put(name, httpUrl.queryParameter(name));
                }

                if (queryParameters.size() != httpUrl.querySize()) {
                    throw new IllegalArgumentException("duplicate param found: " + url);
                } else {
                    httpUrl = httpUrl.newBuilder("?").removeAllQueryParameters("").build();
                    return new HttpApi.SignatureApi<T>(httpUrl, this, signature) {
                        @Override
                        public HttpRequest<T> request(HttpUrl url) {
                            return super.request(url).setParameters(queryParameters);
                        }
                    };
                }
            }
        }

        public Builder(OkHttpClient client, ObjectMapper mapper, HttpApi.Converter<T> converter, HttpApi.Interceptor interceptor) {
            this.client = client;
            this.mapper = mapper;
            this.converter = converter;
            this.interceptor = interceptor;
        }
    }

    public static class CipherApi<T> extends HttpApi<T> {
        protected final RSAKeyPair rsaKeyPair;

        public CipherApi(HttpUrl url, HttpApi.Builder<T> builder, RSAKeyPair rsaKeyPair) {
            super(url, builder);
            this.rsaKeyPair = (RSAKeyPair)Objects.requireNonNull(rsaKeyPair);
        }

        @Override
        protected RequestBody buildRequestBody(String data, HttpRequest<T> request) throws IOException {
            return RequestBody.create(APPLICATION_JSON, this.rsaKeyPair.encrypt(data));
        }
    }

    public static class SignatureApi<T> extends HttpApi<T> {
        protected final Signature signature;

        public SignatureApi(HttpUrl url, HttpApi.Builder<T> builder, Signature signature) {
            super(url, builder);
            this.signature = (Signature)Objects.requireNonNull(signature);
            if (this.url.querySize() > 0) {
                throw new IllegalArgumentException("illegal url: " + url);
            }
        }

        @Override
        protected HttpUrl buildRequestUrl(String method, HttpRequest<T> request) {
            HttpUrl url = request.url;
            Map<String, String> params = request.parameters;
            Map<String, String> sign = this.signature.create(method, url.host(), url.encodedPath(), params);
            HttpUrl.Builder builder = url.newBuilder();
            addQueryParameter(builder, params);
            addQueryParameter(builder, sign);
            return builder.build();
        }

        @Override
        public HttpRequest<T> request(HttpUrl url) {
            return super.request(url).setHeader("Host", url.host());
        }
    }

    @FunctionalInterface
    public interface Converter<T> {
        T convert(String var1) throws IOException;
    }

    @FunctionalInterface
    public interface Interceptor {
        HttpApi.Interceptor DEFAULT = (request, response, body) -> {
            return body;
        };

        String intercept(HttpRequest<?> var1, Response var2, String var3) throws IOException;
    }
}

