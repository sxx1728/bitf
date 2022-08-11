package com.bitfye.risk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author ming.jia
 * @version 1.0
 * @description 风控系统
 * @date 2022/8/6 上午10:31
 **/
@SpringBootApplication
@ComponentScan(value = "com.bitfye.*")
public class RiskApplication {
    public static void main(String[] args) {
        SpringApplication.run(RiskApplication.class, args);
    }
}
