package com.bitfye.wallet.aop;

import java.lang.annotation.*;

/**
 * 内部验证签名注解
 * @author jiaming
 */
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface SignAndVerify {

    boolean innerService() default  false;
}
