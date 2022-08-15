package com.bitfye.common.model.vo;

import lombok.Data;

@Data
public class WithDrawVerifyReqVo {

    private String withdrawId;
    private String uid;
    private String coin;
    private String address;
    private String withdrawAmount;

}
