package com.bitfye.common.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author ming.jia
 * @version 1.0
 * @description TODO
 * @date 2022/8/14 下午4:44
 **/
@Data
@TableName("t_deposit_address")
@Builder
public class DepositAddressPo extends Model<DepositAddressPo> implements Serializable {

    private static final long serialVersionUID = 3096810802891361290L;

    @TableId(value = "f_id", type = IdType.AUTO)
    private Long id;

    private Long uid;

    private String coin;

    private String address;

    private Date createdAt;

    private Date updatedAt;

}
