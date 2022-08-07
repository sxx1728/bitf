package com.bitfye.common.base.util;

import com.bitfye.common.base.constants.Constants;
import com.bitfye.common.base.enums.ErrorConstantEnum;
import com.bitfye.common.base.enums.LanguageEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.function.Supplier;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ResultVo<T> implements Serializable {
    private static final long serialVersionUID = -1L;
    // 响应业务状态
    private Integer code;

    // 响应消息
    private String message;

    // 响应中的数据
    private T data;

    private Boolean success;

    public boolean isSuccess(){
        if(null==success){
            success=code.equals(ErrorConstantEnum.SUCCESS.getErrCode());
        }
        return success;
    }

    public static ResultVo build(ErrorConstantEnum errorConstantEnum, LocaleUtils localeUtils) {
        String errMsg = localeUtils.getMessage(errorConstantEnum.name(), getLanguage());
        ResultVo resultVo = new ResultVo(errorConstantEnum.getErrCode(), errMsg);
        if(ErrorConstantEnum.SUCCESS.getErrCode().equals(errorConstantEnum.getErrCode())){
            resultVo.setSuccess(true);
        }else {
            resultVo.setSuccess(false);
        }
        return resultVo;
    }

    public static ResultVo build(ErrorConstantEnum errorConstantEnum) {
        String errMsg = errorConstantEnum.getErrMsg();
        if(getLanguage() == LanguageEnum.ENGLISH) {
            errMsg = errorConstantEnum.getEnErrMsg();
        }
        ResultVo resultVo = new ResultVo(errorConstantEnum.getErrCode(), errMsg);
        if(ErrorConstantEnum.SUCCESS.getErrCode().equals(errorConstantEnum.getErrCode())){
            resultVo.setSuccess(true);
        }else {
            resultVo.setSuccess(false);
        }
        return resultVo;
    }

    private static LanguageEnum getLanguage() {
        ServletRequestAttributes servletRequestAttributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        if(servletRequestAttributes == null) {
            return LanguageEnum.CHINESE;
        }
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String acceptLanguage = request.getHeader(Constants.ACCEPT_LANGUAGE);
        if(!StringUtils.hasText(acceptLanguage)) {
            return LanguageEnum.CHINESE;
        }
        return LanguageEnum.getInstance(acceptLanguage);
    }

    public static ResultVo buildFailse(String message) {
        ResultVo resultVo = new ResultVo(
                ErrorConstantEnum.FAILURE.getErrCode(), StringUtils.isEmpty(message) ? ErrorConstantEnum.FAILURE.getErrMsg() : message
        );
        resultVo.setSuccess(false);
        return resultVo;
    }

    public static ResultVo buildFailse() {
        ResultVo resultVo = new ResultVo(ErrorConstantEnum.FAILURE.getErrCode(), ErrorConstantEnum.FAILURE.getErrMsg());
        resultVo.setSuccess(false);
        return resultVo;
    }

    public ResultVo(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static <T> ResultVo buildSuccess(T data) {
        ResultVo result = ResultVo.build(ErrorConstantEnum.SUCCESS);
        result.setData(data);
        result.setSuccess(true);
        return result;
    }

    public static ResultVo buildSuccess(Supplier supplier) {
        ResultVo result = ResultVo.build(ErrorConstantEnum.SUCCESS);
        result.setData(supplier.get());
        result.setSuccess(true);
        return result;
    }

    public static ResultVo buildSuccess() {
        return ResultVo.build(ErrorConstantEnum.SUCCESS);
    }

    public static ResultVo buildSuccess(String message) {
        ResultVo result = ResultVo.build(ErrorConstantEnum.SUCCESS);
        result.setMessage(message);
        result.setSuccess(true);
        return result;
    }

    public static <T> ResultVo buildSuccess(ErrorConstantEnum errorConstantEnum, T data) {
        ResultVo result = ResultVo.build(errorConstantEnum);
        result.setData(data);
        result.setSuccess(true);
        return result;
    }

    public boolean success() {
        return isSuccess();
    }

    public boolean notSuccess() {
        return !success();
    }
}
