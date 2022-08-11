package com.bitfye.wallet.aop;

import com.bitfye.common.crypto.signature.SignatureFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

//    @Bean
//    public FilterRegistrationBean registerMyFilter(){
//        FilterRegistrationBean<SignatureFilter> bean = new FilterRegistrationBean<>();
//        bean.setOrder(1);
//        bean.setFilter(new SignatureFilter());
//        // 匹配"/v1/wallet/"下面的所有url
//        bean.addUrlPatterns("/v1/wallet/*");
//        return bean;
//    }
}
