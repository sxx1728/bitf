package com.bitfye.common.crypto.signature;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties("bitfye.security")
public class SignatureConfigs {
    private Map<String, SignatureConfigs.SignatureConfig> signatures = new HashMap();

    public SignatureConfigs() {
    }

    public Map<String, SignatureConfigs.SignatureConfig> getSignatures() {
        return this.signatures;
    }

    public void setSignatures(Map<String, SignatureConfigs.SignatureConfig> signatures) {
        this.signatures = signatures;
    }

    public static class SignatureConfig {
        private String appid;
        private String appkey;

        public SignatureConfig() {
        }

        public String getAppid() {
            return this.appid;
        }

        public void setAppid(String appid) {
            this.appid = appid;
        }

        public String getAppkey() {
            return this.appkey;
        }

        public void setAppkey(String appkey) {
            this.appkey = appkey;
        }
    }
}

