package com.bitfye.common.client.client;

import com.alibaba.fastjson.JSON;
import com.bitfye.common.base.util.ResultVo;
import com.bitfye.common.client.HttpClientSupport;
import com.bitfye.common.client.http.*;
import com.bitfye.common.client.util.JsonUtil;
import com.bitfye.common.model.vo.BitfyeResponse;
import com.bitfye.common.model.vo.GetAccountBalanceResVo;
import com.bitfye.common.model.vo.GetWithdrawReviewResVo;
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
public class UCenterClient {

    @Value("${bitfye.security.signatures.risk.host:}")
    private String ucenterHost;
    @Value("${bitfye.security.signatures.risk.appid:}")
    private String ucenterAppId;
    @Value("${bitfye.security.signatures.risk.appkey:}")
    private String ucenterAppKey;

    @Value("${bitfye.security.signatures.risk.url.getWithdrawReview}")
    private String getWithdrawReivewUrl;

    @Value("${bitfye.security.signatures.risk.url.getAccountBalance}")
    private String getAccountBalanceUrl;

    @Autowired
    private HttpClientSupport httpClientSupport;
    @Value("${bitfye.client.wallet.retry-on-code.retry-time:3}")
    private int retryTimeOnCode;
    @Value("#{'${bitfye.client.wallet.retry-on-code.response-codes:504}'.split(',')}")
    private Set<Integer> retryResponseCodes;
    @Autowired
    @Qualifier("riskHttpConfig")
    private HttpClientConfig httpClientConfig;

    private HttpApi<BitfyeResponse<GetWithdrawReviewResVo>> getWithdrawManualReviewApi;

    private HttpApi<BitfyeResponse<GetAccountBalanceResVo>> getAccountBalanceApi;

    @PostConstruct
    void init() {
        ObjectMapper mapper = JsonUtil.createObjectMapper(PropertyNamingStrategy.KEBAB_CASE);

        HttpApiBuilder<String> retryOnCodeHttpApiBuilder = HttpApi.builder(httpClientConfig, mapper)
                .executorFactory(b -> new RequestExecutor.RetryOnCode(b, retryTimeOnCode, retryResponseCodes));

        Signature ucenterSignature = new Signature(ucenterAppId, ucenterAppKey);

        //风控系统-提币确认API
        getWithdrawManualReviewApi = retryOnCodeHttpApiBuilder.response(BitfyeResponse.type(GetWithdrawReviewResVo.class))
                .buildWithSignature(ucenterHost + getWithdrawReivewUrl, ucenterSignature);

        //风控系统-提币确认API
        getAccountBalanceApi = retryOnCodeHttpApiBuilder.response(BitfyeResponse.type(GetAccountBalanceResVo.class))
                .buildWithSignature(ucenterHost + getAccountBalanceUrl, ucenterSignature);

    }

    /**
     * 创建地址-钱包服务确认 getWithdrawManualReview
     * @param  req String uid
     *         req String withdraw_id
     * @return
     */

    public ResultVo<GetWithdrawReviewResVo> getWithdrawManualReview(String uid, String withdrawId) {
        Map<String, Object> paramMap = ImmutableMap.<String, Object>builder()
                .put("uid", uid)
                .put("withdrawId", withdrawId)
                .build();

        val response = getWithdrawManualReviewApi.request().doPost(paramMap);
        ResultVo<GetWithdrawReviewResVo> resultVo = httpClientSupport.handleUcenterResponse(response);
        log.info("wallet req:{} response:{}", JSON.toJSONString(paramMap), JSON.toJSONString(resultVo));
        return resultVo;
    }

    /**
     * 创建地址-钱包服务确认 createAddress
     * @param  req String uid
     *         req String withdraw_id
     * @return
     */

    public ResultVo<GetAccountBalanceResVo> getAccountBalanceResVo(String uid, String coin) {
        Map<String, Object> paramMap = ImmutableMap.<String, Object>builder()
                .put("uid", uid)
                .put("coin", coin)
                .build();

        val response = getAccountBalanceApi.request().doPost(paramMap);
        ResultVo<GetAccountBalanceResVo> resultVo = httpClientSupport.handleUcenterResponse(response);
        log.info("wallet req:{} response:{}", JSON.toJSONString(paramMap), JSON.toJSONString(resultVo));
        return resultVo;
    }

}
