package com.bitfye.risk.aop;

import com.bitfye.common.base.enums.ErrorConstantEnum;
import com.bitfye.common.base.util.ResultVo;
import com.bitfye.common.crypto.signature.ApiSignature;
import com.bitfye.common.crypto.signature.BadSignatureException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 内部接口验证签名拦截器
 * @author jiaming
 */
@Aspect
@Component
@Slf4j
public class SignAndVerifyAspect {

    @Autowired
    private ApiSignature apiSignature;

    @Pointcut(value = "@annotation(signAndVerify)", argNames = "signAndVerify")
    public void signVerify(SignAndVerify signAndVerify) {}

    @Around("signVerify(signAndVerify)")
    public Object beforePointcut(ProceedingJoinPoint joinPoint, SignAndVerify signAndVerify) throws Throwable {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        boolean isInnerService= signAndVerify.innerService();
        if(isInnerService){
            try {
                apiSignature.checkSignature(request);
            }catch (BadSignatureException e){
                log.info("验证签名异常，验签失败",e);
                return ResultVo.build(ErrorConstantEnum.SIGN_VERIFY_FAILED);
            }
        }
        return joinPoint.proceed();
    }
}
