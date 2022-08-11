package com.bitfye.risk.controller;

import com.bitfye.common.base.util.ResultVo;
import com.bitfye.common.client.client.WalletClient;
import com.bitfye.common.model.vo.NewAddressReqVo;
import com.bitfye.risk.cobo.CoboClient;
import io.swagger.annotations.ApiOperation;
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
@RequestMapping("/v1/risk/")
public class RiskController {

    @Autowired
    private WalletClient walletClient;
    @Autowired
    private CoboClient coboClient;

    @ApiOperation("生成充币地址-调用钱包服务")
    @PostMapping("createAddress")
    public ResultVo createAddress(@Validated @RequestBody NewAddressReqVo reqVo) {
        ResultVo<Boolean> result = walletClient.createAddress(reqVo.getCoin());
        if(result.getSuccess()) {
            return ResultVo.buildSuccess();
        } else {
            return ResultVo.buildFailse(result.getMessage());
        }
    }

}
