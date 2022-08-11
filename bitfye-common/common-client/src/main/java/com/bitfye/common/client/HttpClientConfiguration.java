package com.bitfye.common.client;

import com.bitfye.common.client.http.HttpClientConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Lazy
@Configuration
public class HttpClientConfiguration {


    @Bean("riskHttpConfig")
    @ConfigurationProperties("bitfye.client.risk.http")
    public HttpClientConfig brokerHttpConfig() {
        return new HttpClientConfig("risk");
    }

    @Bean("walletHttpConfig")
    @ConfigurationProperties("bitfye.client.wallet.http")
    public HttpClientConfig auditHttpConfig() {
        return new HttpClientConfig("wallet");
    }


}
