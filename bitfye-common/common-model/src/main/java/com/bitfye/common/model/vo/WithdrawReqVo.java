package com.bitfye.common.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawReqVo implements Serializable {

    private static final long serialVersionUID = 2673640158181214522L;

    private String coin;

    private String requestId;

    private String address;

    private String amount;

    private String memo;

    private String forceExternal;

    private String forceInternal;
}
