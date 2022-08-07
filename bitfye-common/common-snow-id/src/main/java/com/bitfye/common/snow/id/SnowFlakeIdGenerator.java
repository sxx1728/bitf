package com.bitfye.common.snow.id;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

import javax.annotation.PreDestroy;

@Slf4j
public class SnowFlakeIdGenerator {
    private final BaseIdGenerator idGenerator;

    private final IdGeneratorFactory idGeneratorFactory;

    private final long workId;

    private final String contextName;
    //  contextName，以bitfye_开头，避免重复。  其他一样。

    public SnowFlakeIdGenerator(RedissonClient redissonClient, String contextName) {
        idGeneratorFactory = new IdGeneratorFactory(redissonClient);
        this.contextName = contextName;
        this.idGenerator = idGeneratorFactory.createIdGenerator(this.contextName);
        this.workId = this.idGenerator.getShardId();
    }

    public Long nextId() {
        return this.idGenerator.nextId();
    }

    @PreDestroy
    public void preDestroy() {
        log.info("SnowFlakeIdGenerator , unregister workId. {},{},{}", workId, contextName, IdGeneratorFactory.getInstanceName());
        idGeneratorFactory.unregister(this.idGenerator, contextName);
    }

    public Long getShardId() {
        return this.idGenerator.getShardId();
    }
}
