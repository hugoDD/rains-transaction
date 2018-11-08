/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rains.transaction.tx.manager;

import com.alibaba.dubbo.config.*;
import com.rains.transaction.common.notify.CallbackListener;
import com.rains.transaction.common.util.IdGen;
import com.rains.transaction.remote.service.TxManagerRemoteService;
import com.rains.transaction.tx.manager.spi.repository.JdbcTransactionRecoverRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.mongo.MongoHealthIndicatorAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.List;

/*
 * 文 件 名:  TxManagerApplication.java
 * 版    权:  Copyright (c) 2018 com.rains.hugosz
 * 描    述:  事务管理器启动类
 * 创 建 人:  hugosz
 * 创建时间:  2018/11/8
 */
@SpringBootApplication(exclude = MongoHealthIndicatorAutoConfiguration.class)
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
        long nextId = IdGen.get().nextId();
        logger.info("注册 group is : {}",nextId);
        service.setGroup("tx_"+ nextId);
        service.setProtocol(protocolConfig);

        List<MethodConfig> methodConfigs = new ArrayList<>();

        List<ArgumentConfig> arguments = new ArrayList<>();

        MethodConfig createGroupMethodConfig = new MethodConfig();
        createGroupMethodConfig.setName("createGroup");
        ArgumentConfig createGroupArgumentConfig = new ArgumentConfig();
        createGroupArgumentConfig.setIndex(1);
        createGroupArgumentConfig.setCallback(true);
        createGroupArgumentConfig.setType(CallbackListener.class.getName());
        arguments.add(createGroupArgumentConfig);
        createGroupMethodConfig.setArguments(arguments);

        MethodConfig addTransactionMethodConfig = new MethodConfig();
        addTransactionMethodConfig.setName("addTransaction");

        List<ArgumentConfig> addarguments = new ArrayList<>();

        ArgumentConfig addTransactionArgumentConfig = new ArgumentConfig();
        addTransactionArgumentConfig.setIndex(1);
        addTransactionArgumentConfig.setCallback(true);
        addTransactionArgumentConfig.setType(CallbackListener.class.getName());
        addarguments.add(addTransactionArgumentConfig);
        addTransactionMethodConfig.setArguments(addarguments);

        methodConfigs.add(createGroupMethodConfig);
        methodConfigs.add(addTransactionMethodConfig);

        service.setMethods(methodConfigs);

        service.export();
        return service;
    }


}

