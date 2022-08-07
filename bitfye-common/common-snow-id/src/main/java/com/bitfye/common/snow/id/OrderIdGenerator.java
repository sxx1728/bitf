package com.bitfye.common.snow.id;

import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;

public class OrderIdGenerator {
    private static final Logger log = LoggerFactory.getLogger(OrderIdGenerator.class);
    private final BaseIdGenerator idGenerator;
    private final IdGeneratorFactory idGeneratorFactory;
    private final long workId;
    private final String contextName;

    public OrderIdGenerator(RedissonClient redissonClient) {
        this.contextName = IdContextNameEnum.BrokerageOrderId.name();
        this.idGeneratorFactory = new IdGeneratorFactory(redissonClient);
        this.idGenerator = this.idGeneratorFactory.createIdGenerator(this.contextName);
        this.workId = this.idGenerator.getShardId();
    }

    public long nextId() {
        return this.idGenerator.nextId();
    }

    @PreDestroy
    public void preDestroy() {
        log.info("OrderIdGenerator , unregister workId. {},{},{}", new Object[]{this.workId, this.contextName, IdGeneratorFactory.getInstanceName()});
        this.idGeneratorFactory.unregister(this.idGenerator, this.contextName);
    }

    public Long getShardId() {
        return this.idGenerator.getShardId();
    }
}
