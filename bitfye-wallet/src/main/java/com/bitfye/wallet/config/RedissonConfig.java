package com.bitfye.wallet.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ming.jia
 * @version 1.0
 * @description TODO
 * @date 2021/1/21 01:34
 **/
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
@Slf4j
public class RedissonConfig {

    @Value("${redisson.address:redis://127.0.0.1:6379}")
    String address;

    @Value("${redisson.timeout:5000}")
    int timeout;

    @Value("${redisson.max.poolsize:50}")
    int maxPoolSize;

    @Value("${redisson.min.idlesize:50}")
    int minIdleSize;

    @Value("${redisson.password:}")
    String password;

    @Value("${redisson.database:}")
    int database;

    @Bean
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient createRedisson() {
        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer();
        serverConfig.setAddress(address);
        serverConfig.setTimeout(timeout);
        serverConfig.setConnectTimeout(timeout);
        serverConfig.setConnectionPoolSize(maxPoolSize);
        serverConfig.setConnectionMinimumIdleSize(minIdleSize);
        serverConfig.setDatabase(database);

        if (!StringUtils.isBlank(password)) {
            serverConfig.setPassword(password);
        }
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
