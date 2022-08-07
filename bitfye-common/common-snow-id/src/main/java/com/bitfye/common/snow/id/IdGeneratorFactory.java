package com.bitfye.common.snow.id;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class IdGeneratorFactory {
    private static final Logger log = LoggerFactory.getLogger(IdGeneratorFactory.class);
    private RedissonClient redissonClient;

    public IdGeneratorFactory(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    private WorkIdInfo register(String businessContextName) {
        String instanceName = getInstanceName();
        RMap<String, String> registedWorkId = this.redissonClient.getMap(getWorkIdKey(businessContextName), StringCodec.INSTANCE);
        RLock lock = this.redissonClient.getLock(this.getWorkIdLockKey(businessContextName));
        WorkIdInfo newWorkIdInfo = null;
        Gson gson = new Gson();

        try {
            int retry = 0;
            boolean registerSuccess = false;

            while(retry < 5) {
                ++retry;
                lock.lock(10L, TimeUnit.SECONDS);
                log.info("locked {}", getWorkIdKey(businessContextName));
                if (lock.isLocked()) {
                    registedWorkId.values().forEach((value) -> {
                        log.info("Registered workId: {}", value);
                    });

                    for(int i = 0; (long)i <= 127L; ++i) {
                        if (!registedWorkId.containsKey(String.valueOf(i))) {
                            newWorkIdInfo = new WorkIdInfo();
                            newWorkIdInfo.setBusinessContextName(businessContextName);
                            newWorkIdInfo.setRegisterBy(instanceName);
                            newWorkIdInfo.setRegisterTime(System.currentTimeMillis());
                            newWorkIdInfo.setWorkId(i);
                            registedWorkId.put(String.valueOf(i), gson.toJson(newWorkIdInfo));
                            log.info("成功注册workId: {}", newWorkIdInfo);
                            registerSuccess = true;
                            break;
                        }
                    }
                }

                if (registerSuccess) {
                    break;
                }
            }

            if (!registerSuccess) {
                log.error("Cannot register workId after tried 5 times. {},{}", businessContextName, instanceName);
                throw new NoAvailableWorkIdException(businessContextName);
            }
        } catch (Exception var13) {
            log.error("Register the workId failed: {},{}", new Object[]{businessContextName, var13.getMessage(), var13});
        } finally {
            lock.unlock();
            log.info("unlocked {}", getWorkIdKey(businessContextName));
        }

        return newWorkIdInfo;
    }

    private String getWorkIdLockKey(String businessContextName) {
        return getWorkIdKey(businessContextName) + ":lock";
    }

    public static String getWorkIdKey(String businessContextName) {
        return "institution:common:workId:" + businessContextName;
    }

    public static String getInstanceName() {
        String instanceName = System.getenv("POD_NAME");
        if (StringUtils.isEmpty(instanceName)) {
            InetAddress addr = null;

            try {
                addr = InetAddress.getLocalHost();
                String hostName = addr.getHostName();
                instanceName = hostName;
            } catch (UnknownHostException var3) {
                log.warn("{}", var3.getMessage(), var3);
                instanceName = "unknown_host";
            }
        }

        return instanceName;
    }

    public void unregister(BaseIdGenerator idGenerator, String businessContextName) {
        RMap<String, String> registedWorkId = this.redissonClient.getMap(getWorkIdKey(businessContextName), StringCodec.INSTANCE);
        RLock lock = this.redissonClient.getLock(this.getWorkIdLockKey(businessContextName));
        Gson gson = new Gson();

        try {
            lock.lock(10L, TimeUnit.SECONDS);
            log.info("locked {}", getWorkIdKey(businessContextName));
            if (lock.isLocked()) {
                Set<Map.Entry<String, String>> existedWorkIdEntries = registedWorkId.entrySet();
                existedWorkIdEntries.forEach((entry) -> {
                    log.info("Registered workId: {}", entry.getValue());
                });
                String value = (String)registedWorkId.get(String.valueOf(idGenerator.getShardId()));
                WorkIdInfo info = (WorkIdInfo)gson.fromJson(value, WorkIdInfo.class);
                if (info != null) {
                    if (StringUtils.equals(info.getBusinessContextName(), businessContextName) && StringUtils.equals(getInstanceName(), info.getRegisterBy())) {
                        registedWorkId.remove(String.valueOf(idGenerator.getShardId()));
                        log.info("注销workId: {}", info);
                    } else {
                        log.error("workId被重复注册，请检查!!!!!   {}", info);
                    }
                } else {
                    log.error("未找到已注册的workId，请检查是否丢失注册信息。 {},{},{}", new Object[]{idGenerator.getShardId(), businessContextName, getInstanceName()});
                }
            }
        } catch (Exception var12) {
            log.error("UnRegister the workId failed: {},{}", new Object[]{businessContextName, var12.getMessage(), var12});
        } finally {
            lock.unlock();
            log.info("unlocked {}", getWorkIdKey(businessContextName));
        }

    }

    public BaseIdGenerator createIdGenerator(String businessContextName) {
        BaseIdGenerator idGenerator = new BaseIdGenerator();
        WorkIdInfo workIdInfo = this.register(businessContextName);
        if (workIdInfo != null && workIdInfo.getWorkId() >= 0) {
            idGenerator.setShardId((long)workIdInfo.getWorkId());
            return idGenerator;
        } else {
            throw new NoAvailableWorkIdException(businessContextName);
        }
    }
}
