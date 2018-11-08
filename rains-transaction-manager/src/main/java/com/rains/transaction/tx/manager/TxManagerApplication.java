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

import com.alibaba.dubbo.config.*;
import com.rains.transaction.common.notify.CallbackListener;
import com.rains.transaction.common.util.IdGen;
import com.rains.transaction.remote.service.TxManagerRemoteService;
import com.rains.transaction.tx.manager.spi.repository.JdbcTransactionRecoverRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaoyu
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
        long nextId = IdGen.get().nextId();
        logger.info("注册 group is : {}",nextId);
        service.setGroup("tx_"+ nextId);
        service.setProtocol(protocolConfig);

        List<MethodConfig> mothodConfigs = new ArrayList<>();

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

        mothodConfigs.add(createGroupMethodConfig);
        mothodConfigs.add(addTransactionMethodConfig);

        service.setMethods(mothodConfigs);

        service.export();
        return service;
    }


}

