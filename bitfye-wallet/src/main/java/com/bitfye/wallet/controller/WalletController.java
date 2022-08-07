package com.bitfye.wallet.controller;

import com.bitfye.common.base.util.ResultVo;
import com.bitfye.common.cobo.api.CoboClient;
import com.bitfye.common.model.vo.NewAddressReqVo;
import com.bitfye.common.model.vo.WithdrawReqVo;
import com.cobo.custody.api.client.domain.ApiResponse;
import com.cobo.custody.api.client.domain.account.Address;
import io.swagger.annotations.ApiOperation;
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
 * @description 钱包系统对外API
 * @date 2022/8/5 下午9:32
 **/
@RestController
@RequestMapping("/v1/wallet/")
public class WalletController {

    @Autowired
    private CoboClient coboClient;

    @ApiOperation("生成充币地址")
    @PostMapping("/new_address")
    public ResultVo newAddress(@Validated @RequestBody NewAddressReqVo reqVo, HttpServletRequest request) {
        ApiResponse<Address> address = coboClient.getNewDepositAddress(reqVo.getCoin());
        return ResultVo.buildSuccess(address.getResult());
    }

    @ApiOperation("提币")
    @PostMapping("/withdraw")
    public ResultVo withdraw(@Validated @RequestBody WithdrawReqVo reqVo) {

        coboClient.submitWithdrawRequest(reqVo);
        return new ResultVo();
    }

    @ApiOperation("充值")
    @PostMapping("/deposit")
    public ResultVo deposit(String coin) {
        ApiResponse<Address> address = coboClient.getNewDepositAddress(coin);
        return new ResultVo();
    }
}
