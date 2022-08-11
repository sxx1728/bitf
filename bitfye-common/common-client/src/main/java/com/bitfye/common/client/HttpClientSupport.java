package com.bitfye.common.client;

import com.bitfye.common.base.util.ResultVo;
import com.bitfye.common.base.util.SamplingLogger;
import com.bitfye.common.base.util.SamplingLoggerBuilder;
import com.bitfye.common.client.http.HttpResponse;
import com.bitfye.common.client.util.JsonUtil;
import com.bitfye.common.model.vo.BitfyeResponse;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @since 2019-11-30
 */
@Slf4j
@Component
public class HttpClientSupport {

    private static final SamplingLogger LOG = SamplingLoggerBuilder.newBuilder(log).perHours(8L).maxSize(100000L).build();

    public <T> ResultVo<T> handleRiskResponse(HttpResponse<BitfyeResponse<T>> resp, Boolean isLog) {
        BitfyeResponse<T> response = resp.getBody();
        if(isLog){
            log.info("调用风控服务结果 resp={}", JsonUtil.writeValue(response));
        }

        if (response != null && response.isSuccess()) {
            return ResultVo.buildSuccess(()->response.getData());
        }

        String errorCode, errorMessage = null;
        if (response != null) {
            errorCode = "risk." + response.getCode();
            errorMessage = response.getMessage()==null ? "null" : response.getMessage();
            return ResultVo.buildFailse().setData(ImmutableMap.of("errorCode",errorCode,"errorMessage",errorMessage)).setMessage(errorMessage);
        } else {
            errorCode = getErrorCode(resp);
            return ResultVo.buildFailse().setData(ImmutableMap.of("errorCode",errorCode));
        }
    }

    public <T> ResultVo<T> handleRiskResponse(HttpResponse<BitfyeResponse<T>> resp) {
        return handleRiskResponse( resp,true);
    }


    public <T> ResultVo<T> handleWalletResponse(HttpResponse<BitfyeResponse<T>> resp, Boolean isLog) {
        BitfyeResponse<T> response = resp.getBody();
        if(isLog){
            log.info("调用钱包服务结果 resp={}", JsonUtil.writeValue(response));
        }

        if (response != null && response.isSuccess()) {
            return ResultVo.buildSuccess(()->response.getData());
        }

        String errorCode, errorMessage = null;
        if (response != null) {
            errorCode = "wallet." + response.getCode();
            errorMessage = response.getMessage()==null ? "null" : response.getMessage();
            return ResultVo.buildFailse().setData(ImmutableMap.of("errorCode",errorCode,"errorMessage",errorMessage)).setMessage(errorMessage);
        } else {
            errorCode = getErrorCode(resp);
            return ResultVo.buildFailse().setData(ImmutableMap.of("errorCode",errorCode));
        }
    }

    public <T> ResultVo<T> handleWalletResponse(HttpResponse<BitfyeResponse<T>> resp) {
        return handleWalletResponse( resp,true);
    }

    private static String getErrorCode(HttpResponse resp) {
        Throwable ex = resp.getError();
        if (ex != null) {
            return ex.getClass().getName();
        } else {
            return "http.status-" + resp.getCode();
        }
    }
}
