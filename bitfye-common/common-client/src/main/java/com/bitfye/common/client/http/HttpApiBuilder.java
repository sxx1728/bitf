package com.bitfye.common.client.http;

import com.bitfye.common.base.enums.ErrorConstantEnum;
import com.bitfye.common.base.exception.BusinessException;
import com.bitfye.common.client.util.StringConstant;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.Functions;

import java.util.Objects;

/**
 * @since 2019-11-29
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpApiBuilder<T> {

    public static final MediaType APPLICATION_JSON = MediaType.parse(StringConstant.APPLICATION_JSON);

    protected final OkHttpClient client;

    protected final ObjectMapper mapper;

    protected final RequestBodyBuilder requestBodyBuilder;

    protected final ResponseBodyHandler<T> responseBodyHandler;

    protected final RequestExecutor.Factory executorFactory;

    public static HttpApiBuilder<String> create(OkHttpClient client, ObjectMapper mapper) {
        Objects.requireNonNull(client, "client required");
        Objects.requireNonNull(mapper, "mapper required");


        RequestBodyBuilder.Json requestBodyBuilder = mapper::writeValueAsString;
        ResponseBodyHandler<String> responseBodyHandler = (req, resp) -> resp.getBodyString();
        RequestExecutor.Factory executorFactory = RequestExecutor.Default::new;

        return new HttpApiBuilder<>(client, mapper, requestBodyBuilder, responseBodyHandler, executorFactory);
    }

    public <R> HttpApiBuilder<R> response(Class<R> type) {
        ObjectMapper objectMapper = this.mapper;
        Objects.requireNonNull(type, "deserialize type required");
        return response(str -> objectMapper.readValue(str, type));
    }

    public <R> HttpApiBuilder<R> response(TypeReference<R> type) {
        ObjectMapper objectMapper = this.mapper;
        Objects.requireNonNull(type, "deserialize type required");
        return response(str -> objectMapper.readValue(str, type));
    }

    private <R> HttpApiBuilder<R> response(
            Functions.FailableFunction<String, R, Exception> deserializer) {

        Objects.requireNonNull(deserializer, "deserializer required");
        return responseBodyHandler((req, resp) -> {
            try {
                return deserializer.apply(resp.getBodyString());
            } catch (Exception e) {
                throw new BusinessException(ErrorConstantEnum.FAILURE,e);
            }
        });
    }

    public final <R> HttpApiBuilder<R> responseBodyHandler(ResponseBodyHandler<R> responseBodyHandler) {
        Objects.requireNonNull(responseBodyHandler, "converter required");
        return new HttpApiBuilder<>(client, mapper, requestBodyBuilder, responseBodyHandler, executorFactory);
    }

    public final HttpApiBuilder<T> requestBodyBuilder(RequestBodyBuilder requestBodyBuilder) {
        Objects.requireNonNull(requestBodyBuilder, "converter required");
        return new HttpApiBuilder<>(client, mapper, requestBodyBuilder, responseBodyHandler, executorFactory);
    }

    public final HttpApiBuilder<T> executorFactory(RequestExecutor.Factory executorFactory) {
        Objects.requireNonNull(executorFactory, "executorFactory required");
        return new HttpApiBuilder<>(client, mapper, requestBodyBuilder, responseBodyHandler, executorFactory);
    }

    public RequestExecutor<T> createRequestExecutor() {
        return Objects.requireNonNull(executorFactory.create(this), "executor required");
    }

    public HttpApi<T> build(String url) {
        val httpUrl = Objects.requireNonNull(HttpUrl.parse(url), "invalid url: " + url);
        return new HttpApi<>(httpUrl, this);
    }

    public HttpApi<T> buildWithSignature(String url, Signature signature) {
        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse(url), "invalid url: " + url);
        return new SignatureApi<>(httpUrl, this, signature);
    }
}
