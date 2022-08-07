package com.bitfye.common.model.vo;

import lombok.Data;

import java.math.BigInteger;


@Data
public class WithdrawReqVo {

    private String coin;

    private String requestId;

    private String address;

    private BigInteger amount;

    private String memo;

    private String forceExternal;

    private String forceInternal;
}
