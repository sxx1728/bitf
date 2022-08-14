package com.bitfye.risk.controller;

import com.alibaba.fastjson.JSON;
import com.bitfye.common.base.util.ResultVo;
import com.bitfye.common.client.client.WalletClient;
import com.bitfye.common.model.vo.DepositAddressVerifyReqVo;
import com.bitfye.common.model.vo.DepositTransactionVerifyReqVo;
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

/**
 * @author ming.jia
 * @version 1.0
 * @description 风控接口
 * @date 2022/8/5 下午21:09
 **/
@RestController
@Slf4j
@RequestMapping("/v1/risk/")
public class RiskController {

    private static final String TRANSACTION_STATUS_SUCCESS = "success";

    @Autowired
    private WalletClient walletClient;

    @Autowired
    private CoboClient coboClient;

    @ApiOperation("充币地址二次确认，调用钱包服务")
    @PostMapping("depositAddressVerify")
    public ResultVo depositAddressVerify(@Validated @RequestBody DepositAddressVerifyReqVo reqVo) {
        ApiResponse<Address> response = coboClient.verifyDepositAddress(reqVo.getCoin(), reqVo.getAddress());

        if(response.isSuccess()) {
            return ResultVo.buildSuccess();
        } else {
            log.info("cobo addressVerify req:{} response:{}", JSON.toJSONString(reqVo), JSON.toJSONString(response));
            return ResultVo.buildFailse(response.getErrorMessage());
        }
    }

    @ApiOperation("充币交易二次确认，调用钱包服务")
    @PostMapping("depositTransactionVerify")
    public ResultVo depositTransactionVerify(@Validated @RequestBody DepositTransactionVerifyReqVo reqVo) {
        ApiResponse<Transaction> response = coboClient.getTransactionDetails(reqVo.getId());

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

        if (!response.getResult().getAbsAmount().equals(reqVo.getAbsAmount())){
            log.info("Cobo depositTransactionVerify req:{} response:{}", JSON.toJSONString(reqVo), JSON.toJSONString(response));
            return ResultVo.buildFailse("Cobo Invalid transaction abs amount: " + response.getResult().getAbsAmount());
        }


        return ResultVo.buildSuccess();
    }


    @ApiOperation("提币二次确认，调用业务系统服务")
    @PostMapping("depositAddressVerify")
    public ResultVo withdrawVerify(@Validated @RequestBody DepositAddressVerifyReqVo reqVo) {

        ApiResponse<Address> response = coboClient.verifyDepositAddress(reqVo.getCoin(), reqVo.getAddress());

        if(response.isSuccess()) {
            return ResultVo.buildSuccess();
        } else {
            log.info("cobo addressVerify req:{} response:{}", JSON.toJSONString(reqVo), JSON.toJSONString(response));
            return ResultVo.buildFailse(response.getErrorMessage());
        }
    }


}
