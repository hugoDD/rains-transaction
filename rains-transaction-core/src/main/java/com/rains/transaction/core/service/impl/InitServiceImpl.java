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
package com.rains.transaction.core.service.impl;

import com.rains.transaction.common.config.TxConfig;
import com.rains.transaction.common.enums.SerializeProtocolEnum;
import com.rains.transaction.common.exception.TransactionRuntimeException;
import com.rains.transaction.common.holder.LogUtil;
import com.rains.transaction.common.holder.ServiceBootstrap;
import com.rains.transaction.common.serializer.ObjectSerializer;
import com.rains.transaction.core.compensation.TxCompensationService;
import com.rains.transaction.core.helper.SpringBeanUtils;
import com.rains.transaction.core.service.InitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/*
 * 文 件 名:  InitServiceImpl.java
 * 版    权:  Copyright (c) 2018 com.rains.hugosz
 * 描    述:  初始化服务
 * 创 建 人:  hugosz
 * 创建时间:  2018/11/6
 */
@Component
@Transactional
public class InitServiceImpl implements InitService {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InitServiceImpl.class);


    private final TxCompensationService txCompensationService;

    @Autowired
    public InitServiceImpl(TxCompensationService txCompensationService) {
        this.txCompensationService = txCompensationService;
    }

    @Override
    public void initialization(TxConfig txConfig) {
        try {
            loadSpi(txConfig);
            txCompensationService.start(txConfig);
        } catch (Exception e) {
            throw new TransactionRuntimeException("补偿配置异常：" + e.getMessage());
        }
        LogUtil.info(LOGGER, () -> "分布式补偿事务初始化成功！");

    }

    /**
     * 根据配置文件初始化spi
     *
     * @param txConfig 配置信息
     */
    private void loadSpi(TxConfig txConfig) {

        //spi  serialize
        final SerializeProtocolEnum serializeProtocolEnum =
                SerializeProtocolEnum.acquireSerializeProtocol(txConfig.getSerializer());
        final ServiceLoader<ObjectSerializer> objectSerializers = ServiceBootstrap.loadAll(ObjectSerializer.class);

        final Optional<ObjectSerializer> serializer = StreamSupport.stream(objectSerializers.spliterator(), false)
                .filter(objectSerializer ->
                        Objects.equals(objectSerializer.getScheme(), serializeProtocolEnum.getSerializeProtocol())).findFirst();
        serializer.ifPresent(s->SpringBeanUtils.getInstance().registerBean(ObjectSerializer.class.getName(),s));
        ;
        //spi  RecoverRepository support
       // final CompensationCacheTypeEnum compensationCacheTypeEnum = CompensationCacheTypeEnum.acquireCompensationCacheType(txConfig.getCompensationCacheType());
        //final ServiceLoader<TransactionRecoverService> recoverRepositories = ServiceBootstrap.loadAll(TransactionRecoverService.class);


//        final Optional<TransactionRecoverService> repositoryOptional =
//                StreamSupport.stream(recoverRepositories.spliterator(), false)
//                        .filter(recoverRepository ->
//                                Objects.equals(recoverRepository.getScheme(), compensationCacheTypeEnum.getCompensationCacheType()))
//                        .findFirst();
        //将compensationCache实现注入到spring容器
//        repositoryOptional.ifPresent(repository -> {
//            serializer.ifPresent(repository::setSerializer);
//            SpringBeanUtils.getInstance().registerBean(TransactionRecoverService.class.getName(), repository);
//        });


    }


}
