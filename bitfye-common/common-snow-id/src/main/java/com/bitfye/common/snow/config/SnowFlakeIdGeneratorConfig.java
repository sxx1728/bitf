package com.bitfye.common.snow.config;

import com.bitfye.common.snow.id.SnowFlakeIdGenerator;
import lombok.Setter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Setter
@Configuration
@ConditionalOnProperty("bitfye.generator.prefix")
public class SnowFlakeIdGeneratorConfig {

    @Bean
    public SnowFlakeIdGenerator txIdGenerator(final RedissonClient redissonClient,
                                              @Value("${bitfye.generator.prefix}") final String prefix) {
        return new SnowFlakeIdGenerator(redissonClient, prefix);
    }
}
