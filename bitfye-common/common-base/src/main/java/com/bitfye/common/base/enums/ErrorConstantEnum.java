package com.bitfye.common.base.enums;

import cn.gjing.tools.common.util.ParamUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

public enum ErrorConstantEnum {

    /**
     * 系统响应码
     */
    SUCCESS(200, "请求成功", "The request is successful."),
    FAILURE(500,"系统繁忙，请稍后再试", "System busy, please try again later."),
    REQUEST_FREQUENTLY(501, "请求频繁", "The request is frequent."),

    ;

    private Integer errCode;
    /**
     * 中文提示信息
     */
    private String errMsg;
    /**
     * 英文提示信息
     */
    private String enErrMsg;
    private Predicate predicate;


    ErrorConstantEnum(Integer errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    ErrorConstantEnum(Integer errCode, String errMsg, String enErrMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
        this.enErrMsg = enErrMsg;
    }

    ErrorConstantEnum(Integer errCode, String errMsg, Predicate predicate) {
        this.errCode = errCode;
        this.errMsg = errMsg;
        this.predicate = predicate;

    }

    public Integer getErrCode() {
        return errCode;
    }

    public String getErrMsg() {
        return this.errMsg;
    }

    public String getEnErrMsg() {
        return this.enErrMsg;
    }

    public boolean verify(Object object) {
        return predicate.test(object);
    }

    public boolean isSuccess() {
        return ErrorConstantEnum.SUCCESS.getErrCode().equals(this.getErrCode());
    }

    public static ErrorConstantEnum getErrorByMsg(String msg, ErrorConstantEnum defaultErrorEnum) {
        if (StringUtils.isEmpty(msg)) {
            return defaultErrorEnum;
        }
        for (ErrorConstantEnum errorConstantEnum : ErrorConstantEnum.values()) {
            if (errorConstantEnum.getErrMsg().contains(msg)) {
                return errorConstantEnum;
            }
        }
        return defaultErrorEnum;
    }

    @Override
    public String toString() {
        return "ErrorConstantEnum{" + "errCode=" + errCode + ", errMsg='" + errMsg + '\'' + '}';
    }

    public static String info(ErrorConstantEnum errorConstantEnum) {
        return info(null, errorConstantEnum, null);
    }


    public static String info(ErrorConstantEnum errorConstantEnum, Object request) {
        return info(null, errorConstantEnum, request);
    }

    public static String info(String prefix, ErrorConstantEnum errorConstantEnum, Object request) {
        StringBuffer info = new StringBuffer();
        if (ParamUtil.isNotEmpty(prefix)) {
            info.append(prefix).append(":");
        }
        if (null != errorConstantEnum) {
            info.append("errorCode=").append(errorConstantEnum.getErrCode()).append(",errorMessage=").append(errorConstantEnum.getErrMsg()).append(";");
        }
        if (ParamUtil.isNotEmpty(request)) {
            info.append("request=").append(JSONObject.toJSONString(request));
        }
        return info.toString();
    }

    public static ErrorConstantEnum getByCode(Integer code){
        for (ErrorConstantEnum value : ErrorConstantEnum.values()) {
            if(value.getErrCode().equals(code)){
                return value;
            }
        }
        return null;
    }
}
