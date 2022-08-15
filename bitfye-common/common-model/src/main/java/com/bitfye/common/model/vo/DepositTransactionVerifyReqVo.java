package com.bitfye.common.model.vo;

import lombok.Data;

@Data
public class DepositTransactionVerifyReqVo {

    private String transactionId;
    private String coin;
    private String address;
    private String memo;
    private String amount;

}
