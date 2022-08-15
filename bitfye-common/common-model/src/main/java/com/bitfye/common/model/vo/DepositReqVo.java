package com.bitfye.common.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author ming.jia
 * @version 1.0
 * @description TODO
 * @date 2022/8/15 下午3:35
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositReqVo implements Serializable {

    private static final long serialVersionUID = 7378673789248967706L;

    private String id;

    private String coin;

    private String display_code;

    private String description;

    private Integer decimal;

    private String address;

    private String source_address;

    private String side;

    private String amount;

    private String absAmount;

    private String txid;

    private Integer voutN;

    private String requestId;

    private String status;

    private String absCoboFee;

    private Long createdTime;

    private Long lastTime;

    private Integer confirmedNum;

    private TxDetailResVo txDetailResVo;

    private String sourceAddressDetail;

    private Integer confirmingThreshold;

    private String type;



}
