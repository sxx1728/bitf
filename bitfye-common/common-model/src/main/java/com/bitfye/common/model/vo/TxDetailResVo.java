package com.bitfye.common.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ming.jia
 * @version 1.0
 * @description TODO
 * @date 2022/8/15 下午5:08
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TxDetailResVo {

    private String txid;

    private Integer blocknum;

    private String blockhash;

    private String hexstr;
}
