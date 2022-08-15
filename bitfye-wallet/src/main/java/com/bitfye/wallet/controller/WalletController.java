package com.bitfye.wallet.controller;

import com.bitfye.common.model.vo.NewAddressReqVo;
import com.bitfye.common.model.vo.WithdrawReqVo;
import com.bitfye.common.snow.id.SnowFlakeIdGenerator;
import com.bitfye.wallet.cobo.CoboClient;
import com.blade.Blade;
import com.blade.mvc.RouteContext;
import com.blade.mvc.handler.RouteHandler;
import com.cobo.custody.api.client.domain.ApiResponse;
import com.cobo.custody.api.client.domain.account.Address;
import com.cobo.custody.api.client.impl.LocalSigner;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
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

    private static final String coboPubKey = "032730060f719d7251f6530e0027a2ed2c6e78b09dbcb7e841444d950caa5caf53";

    static RouteHandler custodyCallback = ctx -> {
        String timestamp = ctx.header("Biz-Timestamp");
        String signature = ctx.header("Biz-Resp-Signature");
        boolean verifyResult = false;
        try {
            if (!StringUtils.isEmpty(timestamp) && !StringUtils.isEmpty(signature)) {
                String body = ctx.bodyToString();
                String content = body + "|" + timestamp;
                verifyResult = LocalSigner.verifyEcdsaSignature(content, signature, coboPubKey);
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        verifyResult &= customCheck(ctx);
        log.info("verifyResult: " + verifyResult);
        ctx.text(verifyResult ? "ok" : "deny");
    };

    public static boolean customCheck(RouteContext ctx) {
        //add you checking policy
        return true;
    }

    @ApiOperation("充值后的回调通知")
    @PostMapping("notification")
    public void notification() {
        Blade.of().listen(9000)
                .get("/", ctx -> ctx.text("ok!"))
                .post("/custody_callback", custodyCallback).start();
    }

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
