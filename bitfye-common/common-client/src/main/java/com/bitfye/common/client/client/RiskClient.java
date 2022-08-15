package com.bitfye.common.client.client;

import com.alibaba.fastjson.JSON;
import com.bitfye.common.base.util.ResultVo;
import com.bitfye.common.client.HttpClientSupport;
import com.bitfye.common.client.http.*;
import com.bitfye.common.client.util.JsonUtil;
import com.bitfye.common.model.vo.BitfyeResponse;
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
 * @description 连接Risk
 * @date 2022/8/11 下午4:29
 **/
@Lazy
@Slf4j
@Component
public class RiskClient {

    @Value("${bitfye.security.signatures.risk.host:}")
    private String riskHost;
    @Value("${bitfye.security.signatures.risk.appid:}")
    private String riskAppId;
    @Value("${bitfye.security.signatures.risk.appkey:}")
    private String riskAppKey;
    @Value("${bitfye.security.signatures.risk.url.withdrawVerifyUrl}")
    private String withdrawVerifyUrl;

    @Value("${bitfye.security.signatures.risk.url.depositAddressVerifyUrl}")
    private String depositAddressVerifyUrl;

    @Value("${bitfye.security.signatures.risk.url.depositTransactionVerifyUrl}")
    private String depositTransactionVerifyUrl;

    @Autowired
    private HttpClientSupport httpClientSupport;
    @Value("${bitfye.client.risk.retry-on-code.retry-time:3}")
    private int retryTimeOnCode;
    @Value("#{'${bitfye.client.risk.retry-on-code.response-codes:504}'.split(',')}")
    private Set<Integer> retryResponseCodes;
    @Autowired
    @Qualifier("riskHttpConfig")
    private HttpClientConfig httpClientConfig;

    private HttpApi<BitfyeResponse<Boolean>> riskWithdrawVerifyApi;
    private HttpApi<BitfyeResponse<Boolean>> riskDepositAddressVerifyApi;
    private HttpApi<BitfyeResponse<Boolean>> riskDepositTransactionVerifyApi;

    @PostConstruct
    void init() {
        ObjectMapper mapper = JsonUtil.createObjectMapper(PropertyNamingStrategy.KEBAB_CASE);

        HttpApiBuilder<String> retryOnCodeHttpApiBuilder = HttpApi.builder(httpClientConfig, mapper)
                .executorFactory(b -> new RequestExecutor.RetryOnCode(b, retryTimeOnCode, retryResponseCodes));

        Signature riskSignature = new Signature(riskAppId, riskAppKey);

        //风控系统-提币确认
        riskWithdrawVerifyApi = retryOnCodeHttpApiBuilder.response(BitfyeResponse.type(Boolean.class))
                .buildWithSignature(riskHost + withdrawVerifyUrl, riskSignature);

        riskDepositAddressVerifyApi = retryOnCodeHttpApiBuilder.response(BitfyeResponse.type(Boolean.class))
                .buildWithSignature(riskHost + depositAddressVerifyUrl, riskSignature);

        riskDepositTransactionVerifyApi = retryOnCodeHttpApiBuilder.response(BitfyeResponse.type(Boolean.class))
                .buildWithSignature(riskHost + depositTransactionVerifyUrl, riskSignature);


    }

    /**
     * 提币接口-风控服务确认 withdrawVerify
     * @param req String uid,
     *            String coin,
     *            String address,
     *            String amount,
     *            String memo,
     * @return
     */
    public ResultVo<Boolean> withdrawVerify( String uid, String coin, String address, String memo, String amount) {
        Map<String, Object> paramMap = ImmutableMap.<String, Object>builder().
                put("uid", uid).
                put("coin", coin).
                put("address", address).
                put("amount", amount).
                put("memo", memo).build();

        val response = riskWithdrawVerifyApi.request().doPost(paramMap);
        ResultVo<Boolean> resultVo = httpClientSupport.handleRiskResponse(response);
        log.info("risk req:{} response:{}", JSON.toJSONString(paramMap), JSON.toJSONString(resultVo));
        return resultVo;
    }

    /**
     * 提币接口-风控服务确认 depositAddressVerify
     * @param req String uid,
     *            String coin,
     *            String address,
     *            String amount,
     *            String memo,
     * @return
     */
    public ResultVo<Boolean> depositAddressVerify( String coin, String address) {
        Map<String, Object> paramMap = ImmutableMap.<String, Object>builder().
                put("coin", coin).
                put("address", address).build();

        val response = riskDepositAddressVerifyApi.request().doPost(paramMap);
        ResultVo<Boolean> resultVo = httpClientSupport.handleRiskResponse(response);
        log.info("risk req:{} response:{}", JSON.toJSONString(paramMap), JSON.toJSONString(resultVo));
        return resultVo;
    }

    /**
     * 提币接口-风控服务确认 depositAddressVerify
     * @param req String uid,
     *            String coin,
     *            String address,
     *            String amount,
     *            String memo,
     * @return
     */
    public ResultVo<Boolean> depositTransactionVerify(String transactionId, String coin, String address, String memo, String amount) {
        Map<String, Object> paramMap = ImmutableMap.<String, Object>builder().
                put("transactionId", transactionId).
                put("coin", coin).
                put("address", address).
                put("memo", memo).
                put("amount", amount).build();

        val response = riskDepositTransactionVerifyApi.request().doPost(paramMap);
        ResultVo<Boolean> resultVo = httpClientSupport.handleRiskResponse(response);
        log.info("risk req:{} response:{}", JSON.toJSONString(paramMap), JSON.toJSONString(resultVo));
        return resultVo;
    }


}
