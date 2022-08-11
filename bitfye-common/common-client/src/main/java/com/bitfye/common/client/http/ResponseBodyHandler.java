package com.bitfye.common.client.http;

/**
 * 处理响应数据，将结果写入 {@link HttpResponse#body}
 *
 * @since 2019-11-29
 */
@FunctionalInterface
public interface ResponseBodyHandler<T> {

    T handleResponseBody(HttpRequest<T> req, HttpResponse resp);
}
