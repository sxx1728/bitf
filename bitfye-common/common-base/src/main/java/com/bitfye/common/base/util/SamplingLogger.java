package com.bitfye.common.base.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class SamplingLogger {

    public static SamplingLogger perMinute(Logger logger) {
        return new SamplingLogger(logger, 1, TimeUnit.MINUTES);
    }

    public static SamplingLogger perHour(Logger logger) {
        return new SamplingLogger(logger, 1, TimeUnit.HOURS);
    }

    private final Logger delegate;

    private final Cache<String, AtomicInteger> counters;

    public SamplingLogger(Logger delegate, long duration, TimeUnit unit) {
        this.delegate = delegate;
        this.counters = CacheBuilder.newBuilder()
                .expireAfterWrite(duration, unit)
                .removalListener(this::onRemoval)
                .build();
    }

    protected SamplingLogger(SamplingLoggerBuilder builder) {
        this.delegate = builder.getDelegate();
        CacheBuilder cacheBuilder = CacheBuilder.newBuilder();
        if (!builder.isUnsetMaxSize()) {
            cacheBuilder = cacheBuilder.maximumSize(builder.getMaxSize());
        }
        this.counters = cacheBuilder
                .expireAfterWrite(builder.getDuration(), builder.getUnit())
                .removalListener(this::onRemoval)
                .build();
    }

    public Logger distinct(Class<? extends Throwable> e) {
        return distinct(e.getName());
    }

    public Logger distinct(String key) {
        try {
            AtomicInteger count = counters.get(key, () -> new AtomicInteger(0));
            if (count.getAndIncrement() > 0) {
                return NOPLogger.NOP_LOGGER;
            }
        } catch (ExecutionException e) {
            log.error("get counter error, logger={}, key={}", delegate.getName(), key);
        }
        return delegate;
    }

    void onRemoval(RemovalNotification<String, AtomicInteger> notification) {
        String key = notification.getKey();
        AtomicInteger value = notification.getValue();

        int skip = value.decrementAndGet();
        if (skip > 0) {
            delegate.info("{} skip {} events", key, skip);
        }
    }
}
