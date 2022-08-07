package com.bitfye.wallet.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.config.SentinelServersConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @Autowired
    RedisProperties redisProperties;

    @Configuration
    @ConditionalOnClass({Redisson.class})
    protected class RedissionSingleClientConfiguration {

        /**
         * 哨兵模式 redisson 客户端
         * @return
         */

        @Bean
        @ConditionalOnMissingClass
        public RedissonClient RedissonClient() {
            log.info("sentinel redisProperties:" + redisProperties.getSentinel());
            Config config = new Config();
            List<String> list = redisProperties.getSentinel().getNodes();
            log.info("redission list={}",list);
            String[] nodes = new String[list.size()];
            list.toArray(nodes);
            List<String> newNodes = new ArrayList(nodes.length);
            Arrays.stream(nodes).forEach((index) -> newNodes.add(index.startsWith("redis://") ? index : "redis://" + index));
            SentinelServersConfig serverConfig = config
                    .useSentinelServers()
                    .setIdleConnectionTimeout(10000)
                    .setPingConnectionInterval(1000)
                    .setConnectTimeout(10000)
                    .setTimeout(3000)
                    .setRetryAttempts(3)
                    .setRetryInterval(1500)
                    .addSentinelAddress(newNodes.toArray(new String[0]))
                    .setMasterName(redisProperties.getSentinel().getMaster())
                    .setReadMode(ReadMode.MASTER)
                    .setSslEnableEndpointIdentification(false) //关闭SSL终端识别
                    .setSubscriptionConnectionMinimumIdleSize(1)
                    .setDnsMonitoringInterval(5000)
                    .setSubscriptionsPerConnection(5)
                    .setSubscriptionConnectionPoolSize(5)
                    .setClientName("bitfye-wallet")
                    .setMasterConnectionPoolSize(100)
                    .setSlaveConnectionPoolSize(100);
            if (StringUtils.isNotBlank(redisProperties.getPassword())) {
                serverConfig.setPassword(redisProperties.getPassword());
            }
            config.setCodec(JsonJacksonCodec.INSTANCE);
            return Redisson.create(config);
        }
    }
}
