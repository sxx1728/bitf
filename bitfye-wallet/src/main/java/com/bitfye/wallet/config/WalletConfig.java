package com.bitfye.wallet.config;

import com.bitfye.common.crypto.signature.ApiSignature;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jiaming
 * @date 2022/2/11
 */
@Configuration
public class WalletConfig {
    @Bean("apiSignature")
    public ApiSignature apiSignature(@Value("${bitfye.security.signatures.wallet.appid}") String appKey,
                                     @Value("${bitfye.security.signatures.wallet.appkey}") String appSecretKey) {
        Map<String, String> map = Maps.newHashMap();
        map.put(appKey, appSecretKey);
        ApiSignature apiSignature = new ApiSignature();
        apiSignature.setApps(map);
        return apiSignature;
    }
}
