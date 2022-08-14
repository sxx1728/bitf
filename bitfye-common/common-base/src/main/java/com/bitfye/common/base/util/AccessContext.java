package com.bitfye.common.base.util;


import lombok.Getter;
import lombok.Setter;

/**
 * @since 2019-12-26
 */
@Getter
@Setter
public class AccessContext {

    public enum Auth {
        INTERNAL,
        API_KEY,
        TOKEN,
        LITE,
    }

    private final Auth auth;

    private String source;

    private AccessContext(Auth auth) {
        this.auth = auth;
        this.source = auth.name();
        RequestContext.get().setData(this);
    }

    public static AccessContext get() {
        return RequestContext.get().getData(AccessContext.class);
    }

    public static Auth auth() {
        return NullableUtils.apply(get(), AccessContext::getAuth);
    }

    public static String source() {
        return NullableUtils.apply(get(), AccessContext::getSource);
    }

    public static AccessContext create(Auth auth) {
        return new AccessContext(auth);
    }
}
