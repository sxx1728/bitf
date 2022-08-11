package com.bitfye.common.client.http;

import okhttp3.HttpUrl;

import java.util.Map;
import java.util.Objects;

/**
 * @since 2019-11-30
 */
public class SignatureApi<T> extends HttpApi<T> {

    protected final Signature signature;

    public SignatureApi(
            HttpUrl url, HttpApiBuilder<T> builder, Signature signature) {

        super(url, builder);
        this.signature = Objects.requireNonNull(signature, "signature required");
    }

    @Override
    protected HttpUrl buildRequestUrl(String method, HttpRequest<T> request) {
        HttpUrl url = request.url;
        Map<String, String> params = request.parameters;

        Map<String, String> sign = signature.create(method, url.host(), url.encodedPath(), params);

        HttpUrl.Builder builder = url.newBuilder();
        addQueryParameter(builder, params);
        addQueryParameter(builder, sign);
        return builder.build();
    }

    /**
     * 兼容非标准签名协议，设置不带端口的 Host
     */
    @Override
    public HttpRequest<T> request(HttpUrl url) {
        return super.request(url).setHeader("Host", url.host());
    }
}
