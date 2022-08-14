package com.bitfye.wallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author ming.jia
 * @version 1.0
 * @description 钱包服务
 * @date 2022/8/5 下午9:29
 **/
@SpringBootApplication
@ComponentScan({"com.bitfye.*"})
public class WalletApplication {
    public static void main(String[] args) {
        SpringApplication.run(WalletApplication.class, args);
    }

}
