package com.bitfye.common.client.http;

import com.bitfye.common.client.util.StringConstant;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.val;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.text.StringSubstitutor;

import java.util.Map;

/**
 * @author zoujingyun
 */
public class HttpApi<T> {

    /**
     * url 中的变量标识符 {id}
     */
    private static final String PREFIX = "%7B";

    private static final String SUFFIX = "%7D";

    /**
     * 接口 URL，不包含参数 {@link #parameters}
     */
    protected final HttpUrl url;

    /**
     * 未转义的原始 path，可能包含参数变量
     */
    protected final String originPath;

    /**
     * 从原始 {@link #url} 上分离的固定参数
     */
    protected final Map<String, String> parameters;

    /**
     * HTTP 连接
     */
    protected final OkHttpClient client;

    protected final RequestExecutor<T> executor;

    public HttpApi(HttpUrl url, HttpApiBuilder<T> builder) {
        this.client = builder.client;
        this.executor = builder.createRequestExecutor();
        this.originPath = StringConstant.SPRIT + String.join(StringConstant.SPRIT, url.pathSegments());

        if (url.querySize() == 0) {
            this.url = url;
            this.parameters = ImmutableMap.of();
            return;
        }

        ImmutableMap.Builder<String, String> queryParameters = ImmutableMap.builder();
        for (String name : url.queryParameterNames()) {
            queryParameters.put(name, url.queryParameter(name));
        }

        this.url = url.newBuilder("?").removeAllQueryParameters("").build();
        this.parameters = queryParameters.build();
    }

    public final HttpRequest<T> request() {
        return request(url);
    }

    /**
     * @param params 支持REST风格接口URL，参数 {variable-name} 写在 path 中
     */
    public final HttpRequest<T> request(Map<String, ?> params) {
        HttpUrl httpUrl = this.url;
        String path = httpUrl.encodedPath();
        String replaced = StringSubstitutor.replace(path, params, PREFIX, SUFFIX);
        return request(httpUrl.newBuilder().encodedPath(replaced).build());
    }

    public HttpRequest<T> request(HttpUrl url) {
        return new HttpRequest<>(this, url).setParameters(parameters);
    }

    public HttpResponse<T> doGet() {
        return request().doGet();
    }

    /**
     * GET 请求
     */
    public HttpResponse<T> doGet(Map<String, ?> params) {
        return request().setParameters(params).doGet();
    }

    /**
     * POST 请求
     *
     * @param params url 参数
     * @param data   body 数据
     */
    public HttpResponse<T> doPost(Map<String, ?> params, Object data) {
        return request().setParameters(params).doPost(data);
    }

    public HttpResponse<T> doPost(Object data) {
        return request().doPost(data);
    }

    public HttpResponse<T> execute(HttpRequest<T> request) {
        return executor.execute(this, request);
    }

    protected Request.Builder builder(HttpRequest<T> request) {
        val builder = new Request.Builder();

        builder.url(buildRequestUrl(request.method, request));

        if (!request.headers.isEmpty()) {
            for (Map.Entry<String, String> entry : request.headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        return builder;
    }

    /**
     * 添加 URL 参数，子类可实现 URL 签名 {@link SignatureApi#buildRequestUrl}
     */
    protected HttpUrl buildRequestUrl(String method, HttpRequest<T> request) {
        HttpUrl httpUrl = request.url;
        Map<String, String> params = request.parameters;

        if (params.isEmpty()) {
            return httpUrl;
        }

        HttpUrl.Builder builder = httpUrl.newBuilder();
        addQueryParameter(builder, params);

        return builder.build();
    }

    protected static void addQueryParameter(HttpUrl.Builder builder, Map<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.addQueryParameter(entry.getKey(), entry.getValue());
        }
    }

    public static HttpApiBuilder<String> builder(HttpClientConfig config) {
        return builder(config, new ObjectMapper());
    }

    public static HttpApiBuilder<String> builder(HttpClientConfig config, ObjectMapper mapper) {
        return HttpApiBuilder.create(config.getOkHttpClient(), mapper);
    }

    public static HttpApiBuilder<String> builder(OkHttpClient client, ObjectMapper mapper) {
        return HttpApiBuilder.create(client, mapper);
    }
}
