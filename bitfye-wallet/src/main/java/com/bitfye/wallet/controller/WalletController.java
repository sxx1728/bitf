package com.bitfye.wallet.controller;

import com.bitfye.common.model.vo.NewAddressReqVo;
import com.bitfye.common.model.vo.WithdrawReqVo;
import com.bitfye.common.snow.id.SnowFlakeIdGenerator;
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

/**
 * @author ming.jia
 * @version 1.0
 * @description 钱包系统对外API
 * @date 2022/8/5 下午9:32
 **/
@RestController
@RequestMapping("/v1/wallet/")
@Slf4j
public class WalletController {

    @Autowired
    private CoboClient coboClient;

    @Autowired
    private SnowFlakeIdGenerator snowFlakeIdGenerator;

    @ApiOperation("提币")
    @PostMapping("withdraw")
    public ApiResponse<String> withdraw(@Validated @RequestBody WithdrawReqVo reqVo) {
        Long resultId = snowFlakeIdGenerator.nextId();
        reqVo.setRequestId(resultId.toString());
        ApiResponse<String> result = coboClient.submitWithdrawRequest(reqVo);
        return result;
    }

    @ApiOperation("充值")
    @PostMapping("deposit")
    public ApiResponse<Address> deposit(@Validated @RequestBody NewAddressReqVo reqVo) {
        ApiResponse<Address> result = coboClient.batchNewDepositAddress(reqVo.getCoin());
        return result;
    }
}
