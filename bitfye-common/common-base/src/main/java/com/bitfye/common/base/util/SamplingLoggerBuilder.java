package com.bitfye.common.base.util;

import lombok.Getter;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

@Getter
public class SamplingLoggerBuilder {

    static final long UNSET_LONG = -1l;

    private Logger delegate;

    private long duration = 1;

    private TimeUnit unit = TimeUnit.MINUTES;

    private long maxSize = UNSET_LONG;

    private SamplingLoggerBuilder(Logger delegate) {
        this.delegate = delegate;
    }

    public static SamplingLoggerBuilder newBuilder(Logger delegate) {
        return new SamplingLoggerBuilder(delegate);
    }

    public boolean isUnsetMaxSize() {
        return maxSize == UNSET_LONG;
    }

    public SamplingLoggerBuilder perMinutes(long duration) {
        this.duration = duration;
        this.unit = TimeUnit.MINUTES;
        return this;
    }

    public SamplingLoggerBuilder perHours(long duration) {
        this.duration = duration;
        this.unit = TimeUnit.HOURS;
        return this;
    }

    public SamplingLoggerBuilder maxSize(long maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    public SamplingLogger build() {
        return new SamplingLogger(this);
    }
}
