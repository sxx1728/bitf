package com.bitfye.common.base.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class RequestContext {

    private static final ThreadLocal<RequestContext> CACHE = ThreadLocal.withInitial(RequestContext::new);

    public static RequestContext create(String id) {

        RequestContext context = new RequestContext();
        CACHE.set(context);

        context.setId(id);
        return context;
    }

    public static RequestContext get() {
        return CACHE.get();
    }

    public static void remove() {
        CACHE.remove();
    }

    private final long startTime = DateUtil.currentTimeMillis();

    @Getter(AccessLevel.NONE)
    private final Map<Class, Object> data = new HashMap<>();

    private String id;

    public long getElapsedTime() {
        return DateUtil.currentTimeMillis() - startTime;
    }

    public <T> T setData(@Nonnull T value) {
        return (T) data.put(value.getClass(), value);
    }

    public <T> T setData(Class<T> type, @Nonnull T value) {
        return (T) data.put(type, value);
    }

    public <T> T getData(Class<T> type) {
        return (T) data.get(type);
    }

    public <T> T remove(Class<T> type) {
        return (T) data.remove(type);
    }
}
