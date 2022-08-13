package com.bitfye.wallet.aop;

import com.alibaba.fastjson.JSON;
import com.bitfye.common.crypto.signature.ApiSignature;
import com.bitfye.common.crypto.signature.BadSignatureException;
import com.bitfye.common.crypto.signature.SignatureConfigs;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Slf4j
public class SignAspect {

    @Autowired
    private ApiSignature apiSignature;

    @Pointcut("@annotation(com.bitfye.wallet.aop.SignVerify)")
    public void signAspect() {}

    @Around("signAspect()")
    public Object beforePointcut(ProceedingJoinPoint joinPoint) throws Throwable {
        //获取RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        //从获取RequestAttributes中获取HttpServletRequest的信息
        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
        try{
            apiSignature.checkSignature(request);
            //验证完成后调用 触发aop前置的方法 并返回处理完成的结果
            Object result = joinPoint.proceed();
            return result;
        } catch (BadSignatureException var6){
            log.warn(request.getRequestURI() + ": Bad signature: " + var6.getMessage(), var6);
            return JSON.toJSONString("Bad signature");
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString("Bad signature");
        }
    }
}
