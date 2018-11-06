package com.rains.transaction.core.autoconfigure;

import com.rains.transaction.common.config.TxConfig;
import com.rains.transaction.core.bootstrap.TxTransactionInitialize;
import com.rains.transaction.core.service.InitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/*
 * 文 件 名:  TxClientAutoConfiguration
 * 版    权:  Copyright (c) 2018 com.rains.hugosz
 * 描    述:  todo
 * 创 建 人:  hugosz
 * 创建时间:  2018年 11 月  06日  13:58
 */
@Configuration
@ComponentScan(basePackages = "com.rains.transaction.core")
@Order(Integer.MAX_VALUE)
@EnableConfigurationProperties(TxConfig.class)
public class TxClientAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(TxClientAutoConfiguration.class);

    public TxClientAutoConfiguration(){
        logger.info("初始tx core 自动配置");
    }

    @Bean
    public TxTransactionInitialize txTransactionInitialize(InitService initService ,TxConfig txConfig){
        return new TxTransactionInitialize(initService,txConfig);
    }



}
