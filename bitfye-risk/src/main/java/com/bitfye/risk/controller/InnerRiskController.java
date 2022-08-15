package com.bitfye.risk.controller;

import com.alibaba.fastjson.JSON;
import com.bitfye.common.base.util.ResultVo;
import com.bitfye.common.client.client.UCenterClient;
import com.bitfye.common.model.vo.*;
import com.bitfye.risk.aop.SignAndVerify;
import com.bitfye.risk.cobo.CoboClient;
import com.cobo.custody.api.client.domain.ApiResponse;
import com.cobo.custody.api.client.domain.account.Address;
import com.cobo.custody.api.client.domain.transaction.Transaction;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * @author ming.jia
 * @version 1.0
 * @description 风控接口
 * @date 2022/8/5 下午21:09
 **/
@RestController
@RequestMapping("/v1/risk/inner/")
@Slf4j
public class InnerRiskController {

    private static final String TRANSACTION_STATUS_SUCCESS = "success";

    @Autowired
    private UCenterClient ucenterClient;

    @Autowired
    private CoboClient coboClient;

    @SignAndVerify(innerService = true)
    @ApiOperation("充币地址二次确认，调用钱包服务")
    @PostMapping("deposit_address_verify")
    public ResultVo depositAddressVerify(@Validated @RequestBody DepositAddressVerifyReqVo reqVo) {
        ApiResponse<Address> response = coboClient.verifyDepositAddress(reqVo.getCoin(), reqVo.getAddress());

        if(response.isSuccess()) {
            return ResultVo.buildSuccess();
        } else {
            log.info("cobo addressVerify req:{} response:{}", JSON.toJSONString(reqVo), JSON.toJSONString(response));
            return ResultVo.buildFailse(response.getErrorMessage());
        }
    }

    @SignAndVerify(innerService = true)
    @ApiOperation("充币交易二次确认，调用钱包服务")
    @PostMapping("deposit_transaction_verify")
    public ResultVo depositTransactionVerify(@Validated @RequestBody DepositTransactionVerifyReqVo reqVo) {
        ApiResponse<Transaction> response = coboClient.getTransactionDetails(reqVo.getTransactionId());

        if (!response.isSuccess()) {
            log.info("Cobo depositTransactionVerify req:{} response:{}", JSON.toJSONString(reqVo), JSON.toJSONString(response));
            return ResultVo.buildFailse(response.getErrorMessage());
        }

        if (!response.getResult().getStatus().equals(TRANSACTION_STATUS_SUCCESS)){
            log.info("Cobo depositTransactionVerify req:{} response:{}", JSON.toJSONString(reqVo), JSON.toJSONString(response));
            return ResultVo.buildFailse("Cobo Invalid transaction status: " + response.getResult().getStatus());
        }

        if (!response.getResult().getAddress().equals(reqVo.getAddress())){
            log.info("Cobo depositTransactionVerify req:{} response:{}", JSON.toJSONString(reqVo), JSON.toJSONString(response));
            return ResultVo.buildFailse("Cobo Invalid transaction address: " + response.getResult().getAddress());
        }

        if (!response.getResult().getAbsAmount().equals(reqVo.getAmount())){
            log.info("Cobo depositTransactionVerify req:{} response:{}", JSON.toJSONString(reqVo), JSON.toJSONString(response));
            return ResultVo.buildFailse("Cobo Invalid transaction abs amount: " + response.getResult().getAbsAmount());
        }

        if (!response.getResult().getMemo().equals(reqVo.getMemo())){
            log.info("Cobo depositTransactionVerify req:{} response:{}", JSON.toJSONString(reqVo), JSON.toJSONString(response));
            return ResultVo.buildFailse("Cobo Invalid memo: " + response.getResult().getMemo());
        }

        return ResultVo.buildSuccess();
    }

    @SignAndVerify(innerService = true)
    @ApiOperation("提币二次确认，调用业务系统服务")
    @PostMapping("withdraw_verify")
    public ResultVo withdrawVerify(@Validated @RequestBody WithDrawVerifyReqVo reqVo) {

        ResultVo<GetWithdrawReviewResVo> reviewVo= ucenterClient.getWithdrawManualReview(reqVo.getUid(), reqVo.getWithdrawId());
        if(!reviewVo.isSuccess()) {
            log.info("ucenterclient getwithdrawManualRevew req:{} response:{}", JSON.toJSONString(reqVo), JSON.toJSONString(reviewVo));
            return ResultVo.buildFailse(reviewVo.getMessage());
        }
        if(reviewVo.getData().isNeedManualReview() && !reviewVo.getData().isPassedReview()){
            log.info("ucenterclient getwithdrawManualRevew isNeedManualReview:{} isPassedReview:{}", JSON.toJSONString(reviewVo.getData().isNeedManualReview()), JSON.toJSONString(reviewVo.getData().isPassedReview()));
            return ResultVo.buildSuccess(false);
        }

        ResultVo<GetAccountBalanceResVo> accountBalanceVo= ucenterClient.getAccountBalanceResVo(reqVo.getUid(), reqVo.getCoin());
        if(!accountBalanceVo.isSuccess()) {
            log.info("ucenterclient getAccountBalanceResVo req:{} response:{}", JSON.toJSONString(reqVo), JSON.toJSONString(accountBalanceVo));
            return ResultVo.buildFailse(accountBalanceVo.getMessage());
        }

        BigDecimal balance = new BigDecimal(accountBalanceVo.getData().getAvailableBalance());
        BigDecimal withdrawBalance = new BigDecimal(reqVo.getWithdrawAmount());
        if(balance.compareTo(withdrawBalance) < 0 ) {
            log.info("ucenterclient getwithdrawManualRevew balance:{} withdrawBalance:{}", accountBalanceVo.getData().getAvailableBalance(), reqVo.getWithdrawAmount());
            return ResultVo.buildSuccess(false);
        }

        return ResultVo.buildSuccess(true);
    }


}
