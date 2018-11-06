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
package com.rains.transaction.core.bootstrap;


import com.rains.transaction.common.config.TxConfig;
import com.rains.transaction.common.exception.TransactionRuntimeException;
import com.rains.transaction.common.holder.LogUtil;
import com.rains.transaction.core.service.InitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/*
 * 文 件 名:  TxTransactionInitialize.java
 * 版    权:  Copyright (c) 2018 com.rains.hugosz
 * 描    述:  todo
 * 创 建 人:  hugosz
 * 创建时间:  2018/11/6
 */
public class TxTransactionInitialize {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TxTransactionInitialize.class);

    private final InitService initService;

    private final TxConfig txConfig ;

    public TxTransactionInitialize(InitService initService,TxConfig txConfig) {
        this.initService = initService;
        this.txConfig = txConfig;
        init();
    }

    /**
     * 初始化服务
     */
    public void init() {
        if (Objects.isNull(txConfig)) {
            throw new TransactionRuntimeException("分布式事务配置信息不完整！");
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> LOGGER.error("系统关闭")));
        try {
            initService.initialization(txConfig);
        } catch (RuntimeException ex) {
            LogUtil.error(LOGGER, "初始化异常:{}", ex::getMessage);
            //非正常关闭
            System.exit(1);
        }
    }


}
