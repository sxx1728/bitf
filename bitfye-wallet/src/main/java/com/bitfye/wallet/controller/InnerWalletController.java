package com.bitfye.wallet.controller;

import com.bitfye.common.model.vo.NewAddressReqVo;
import com.bitfye.wallet.aop.SignAndVerify;
import com.bitfye.wallet.cobo.CoboClient;
import com.cobo.custody.api.client.domain.ApiResponse;
import com.cobo.custody.api.client.domain.account.Address;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author ming.jia
 * @version 1.0
 * @description 内部接口需要进行签名验证
 * @date 2022/8/14 上午10:59
 **/
@RestController
@RequestMapping("/v1/wallet/inner/")
@Slf4j
public class InnerWalletController {

    @Autowired
    private CoboClient coboClient;

    @SignAndVerify(innerService = true)
    @ApiOperation("生成充币地址")
    @PostMapping("new_address")
    public ApiResponse<Address> newAddress(@Validated @RequestBody NewAddressReqVo reqVo, HttpServletRequest request) {
        ApiResponse<Address> result = coboClient.getNewDepositAddress(reqVo.getCoin());
        log.info("coin:{} address:{}", result.getResult().getCoin(), result.getResult().getAddress());
        ApiResponse<Boolean> result1 = coboClient.verifyValidAddress(result.getResult().getCoin(), result.getResult().getAddress());
        log.info("newAddress result1:{}", result1);

        //TODO 插入数据库
        return result;
    }
}
