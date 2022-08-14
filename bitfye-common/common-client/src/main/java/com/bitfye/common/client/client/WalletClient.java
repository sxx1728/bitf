package com.bitfye.common.client.client;

import com.alibaba.fastjson.JSON;
import com.bitfye.common.base.util.ResultVo;
import com.bitfye.common.client.HttpClientSupport;
import com.bitfye.common.client.http.*;
import com.bitfye.common.client.util.JsonUtil;
import com.bitfye.common.model.vo.BitfyeResponse;
import com.bitfye.common.model.vo.CreateAddressResVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;

/**
 * @author ming.jia
 * @version 1.0
 * @description 连接Wallet服务
 * @date 2022/8/11 下午4:29
 **/
@Lazy
@Slf4j
@Component
public class WalletClient {

    @Value("${bitfye.security.signatures.wallet.host:}")
    private String walletHost;
    @Value("${bitfye.security.signatures.wallet.appid:}")
    private String walletAppId;
    @Value("${bitfye.security.signatures.wallet.appkey:}")
    private String walletAppKey;
    @Value("${bitfye.security.signatures.wallet.url.createAddress}")
    private String createAddress;

    @Autowired
    private HttpClientSupport httpClientSupport;
    @Value("${bitfye.client.wallet.retry-on-code.retry-time:3}")
    private int retryTimeOnCode;
    @Value("#{'${bitfye.client.wallet.retry-on-code.response-codes:504}'.split(',')}")
    private Set<Integer> retryResponseCodes;
    @Autowired
    @Qualifier("riskHttpConfig")
    private HttpClientConfig httpClientConfig;

    private HttpApi<BitfyeResponse<CreateAddressResVo>> walletCreateAddressApi;

    @PostConstruct
    void init() {
        ObjectMapper mapper = JsonUtil.createObjectMapper(PropertyNamingStrategy.KEBAB_CASE);

        HttpApiBuilder<String> retryOnCodeHttpApiBuilder = HttpApi.builder(httpClientConfig, mapper)
                .executorFactory(b -> new RequestExecutor.RetryOnCode(b, retryTimeOnCode, retryResponseCodes));

        Signature walletSignature = new Signature(walletAppId, walletAppKey);

        //风控系统-提币确认
        walletCreateAddressApi = retryOnCodeHttpApiBuilder.response(BitfyeResponse.type(CreateAddressResVo.class))
                .buildWithSignature(walletHost + createAddress, walletSignature);

    }

    /**
     * 创建地址-钱包服务确认 createAddress
     * @param coin coin
     * @return
     */
    public ResultVo<CreateAddressResVo> createAddress(String coin, Long uid) {
        Map<String, Object> paramMap = ImmutableMap.<String, Object>builder()
                .put("coin", coin)
                .put("uid", uid)
                .build();

        val response = walletCreateAddressApi.request().doPost(paramMap);
        ResultVo<CreateAddressResVo> resultVo = httpClientSupport.handleWalletResponse(response);
        log.info("wallet req:{} response:{}", JSON.toJSONString(paramMap), JSON.toJSONString(resultVo));
        return resultVo;
    }

}
