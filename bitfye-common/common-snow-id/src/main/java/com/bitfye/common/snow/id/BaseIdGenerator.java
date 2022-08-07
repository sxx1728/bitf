package com.bitfye.common.snow.id;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;

public class BaseIdGenerator {
    private static final Logger log = LoggerFactory.getLogger(BaseIdGenerator.class);
    private final long OFFSET = LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.of("Z")).toEpochSecond();
    private final long MAX_NEXT = 16383L;
    private long SHARD_ID = -1L;
    public static final long MAX_SHARD_ID = 127L;
    private long offset = 0L;
    private long lastEpoch = 0L;

    public BaseIdGenerator() {
    }

    public long nextId() {
        return this.nextId(System.currentTimeMillis());
    }

    private synchronized long nextId(long epochSecond) {
        if (this.SHARD_ID < 0L) {
            throw new NotInitializedIdGeneratorException();
        } else {
            if (epochSecond < this.lastEpoch) {
                log.warn("clock is back: " + epochSecond + " from previous:" + this.lastEpoch);
                epochSecond = this.lastEpoch + 1L;
            }

            if (this.lastEpoch != epochSecond) {
                this.lastEpoch = epochSecond;
                this.reset();
            }

            ++this.offset;
            long next = this.offset & 16383L;
            if (next == 0L) {
                log.warn("maximum id reached in 1 second in epoch: " + epochSecond);
                return this.nextId(epochSecond + 1L);
            } else {
                return this.generateId(epochSecond, next, this.SHARD_ID);
            }
        }
    }

    private void reset() {
        this.offset = 0L;
    }

    private long generateId(long epochSecond, long next, long shardId) {
        return epochSecond - this.OFFSET << 21 | next << 7 | shardId;
    }

    public void setShardId(Long workId) {
        this.SHARD_ID = workId;
    }

    public long getShardId() {
        return this.SHARD_ID;
    }
}
