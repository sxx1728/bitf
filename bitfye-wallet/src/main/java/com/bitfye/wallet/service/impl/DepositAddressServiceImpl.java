package com.bitfye.wallet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bitfye.common.mapper.DepositAddressMapper;
import com.bitfye.common.model.po.DepositAddressPo;
import com.bitfye.wallet.service.IDepositAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author ming.jia
 * @version 1.0
 * @description TODO
 * @date 2022/8/14 下午6:13
 **/
@Service
@Slf4j
public class DepositAddressServiceImpl extends ServiceImpl<DepositAddressMapper, DepositAddressPo> implements IDepositAddressService {

}
