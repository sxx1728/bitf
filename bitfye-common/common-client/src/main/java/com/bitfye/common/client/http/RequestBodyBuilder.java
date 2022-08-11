package com.bitfye.common.client.http;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * @since 2019-11-29
 */
@FunctionalInterface
public interface RequestBodyBuilder {

    RequestBody buildRequestBody(HttpRequest<?> request) throws Exception;

    @FunctionalInterface
    interface Json extends RequestBodyBuilder {

        MediaType APPLICATION_JSON = MediaType.parse("application/json");

        String toJson(Object data) throws Exception;

        @Override
        default RequestBody buildRequestBody(HttpRequest<?> request) throws Exception {
            String json = toJson(request.getData());
            return RequestBody.create(APPLICATION_JSON, json);
        }
    }
}
