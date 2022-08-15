package com.bitfye.wallet.cobo;

import com.cobo.custody.api.client.impl.LocalSigner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.blade.Blade;
import com.blade.mvc.RouteContext;
import com.blade.mvc.handler.RouteHandler;
import org.springframework.util.StringUtils;

/**
 * @author ming.jia
 * @version 1.0
 * @description TODO
 * @date 2022/8/15 下午5:33
 **/
@Component
@Slf4j
public class CoboSignVerify {

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

    public static void main(String[] args) {
        Blade.of().listen(9000)
                .get("/", ctx -> ctx.text("ok!"))
                .post("/custody_callback", custodyCallback).start();
    }

    public static boolean customCheck(RouteContext ctx) {
        //add you checking policy
        return true;
    }
}
