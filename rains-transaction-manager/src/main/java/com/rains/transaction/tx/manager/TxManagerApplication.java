/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.rains.transaction.tx.manager;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.rains.transaction.remote.service.TxManagerRemoteService;
import com.rains.transaction.tx.manager.spi.repository.JdbcTransactionRecoverRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;



/*
 * 文 件 名:  TxManagerApplication.java
 * 版    权:  Copyright (c) 2018 com.rains.hugosz
 * 描    述:  事务管理器启动类
 * 创 建 人:  hugosz
 * 创建时间:  2018/11/8
 */
@SpringBootApplication
@EnableScheduling
public class TxManagerApplication {
    private static final Logger logger = LoggerFactory.getLogger(TxManagerApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(TxManagerApplication.class, args);

    }
    @Bean
    public JdbcTransactionRecoverRepository jdbcTransactionRecoverRepository(){
        return new JdbcTransactionRecoverRepository();
    }

    @Bean
    public ServiceConfig<TxManagerRemoteService> serviceConfig(ApplicationConfig applicationConfig, RegistryConfig registryConfig, ProtocolConfig protocolConfig,TxManagerRemoteService txManagerRemoteService) {
        ServiceConfig<TxManagerRemoteService> service = new ServiceConfig<>();
        service.setApplication(applicationConfig);
        service.setInterface(TxManagerRemoteService.class);
        service.setRegistry(registryConfig);
        service.setRef(txManagerRemoteService);

        service.setProtocol(protocolConfig);


        service.export();


        return service;
    }




}

