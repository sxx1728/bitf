package com.bitfye.wallet.controller;

import com.bitfye.common.base.util.ResultVo;
import com.bitfye.common.mapper.DepositAddressMapper;
import com.bitfye.common.model.po.DepositAddressPo;
import com.bitfye.common.model.vo.NewAddressReqVo;
import com.bitfye.wallet.aop.SignAndVerify;
import com.bitfye.wallet.cobo.CoboClient;
import com.bitfye.wallet.service.IDepositAddressService;
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
import java.util.Date;

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
    @Autowired
    private IDepositAddressService depositAddressService;

    @SignAndVerify(innerService = true)
    @ApiOperation("生成充币地址")
    @PostMapping("new_address")
    public ResultVo newAddress(@Validated @RequestBody NewAddressReqVo reqVo) {
        //生成充币地址
        ApiResponse<Address> result = coboClient.getNewDepositAddress(reqVo.getCoin());
        log.info("coin:{} address:{}", result.getResult().getCoin(), result.getResult().getAddress());

        //验证充币地址
        ApiResponse<Boolean> result1 = coboClient.verifyValidAddress(result.getResult().getCoin(), result.getResult().getAddress());
        log.info("newAddress result1:{}", result1);

        //插入数据库
        DepositAddressPo depositAddressPo = DepositAddressPo.builder()
                .uid(reqVo.getUid())
                .coin(result.getResult().getCoin())
                .address(result.getResult().getAddress())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        Boolean insertResult = depositAddressService.save(depositAddressPo);
        if(insertResult) {
            return ResultVo.buildSuccess(result.getResult().getAddress());
        } else {
            return ResultVo.buildFailse();
        }
    }
}
