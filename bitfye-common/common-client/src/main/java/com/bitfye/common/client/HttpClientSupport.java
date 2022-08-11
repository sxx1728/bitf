package com.bitfye.common.client;

import com.bitfye.common.base.util.SamplingLogger;
import com.bitfye.common.base.util.SamplingLoggerBuilder;
import com.bitfye.common.client.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @since 2019-11-30
 */
@Slf4j
@Component
public class HttpClientSupport {

    private static final SamplingLogger LOG = SamplingLoggerBuilder.newBuilder(log).perHours(8L).maxSize(100000L).build();


    private static String getErrorCode(HttpResponse resp) {
        Throwable ex = resp.getError();
        if (ex != null) {
            return ex.getClass().getName();
        } else {
            return "http.status-" + resp.getCode();
        }
    }
}
