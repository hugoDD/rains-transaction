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
package com.rains.transaction.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/*
 * 文 件 名:  TxConfig.java
 * 版    权:  Copyright (c) 2018 com.rains.hugosz
 * 描    述:  事务管理补偿事务配置类
 * 创 建 人:  hugosz
 * 创建时间:  2018/11/6
 */
@ConfigurationProperties(prefix = "tx.manager")
@Data
public class TxConfig {

    /**
     * 提供不同的序列化对象 {@linkplain com.rains.transaction.common.enums.SerializeProtocolEnum}
     */
    private String serializer = "kryo";


    /**
     * netty 传输的序列化协议
     */
   // private String nettySerializer = "kryo";


    /**
     * 延迟时间
     */
    private int delayTime = 30;


    /**
     * 执行事务的线程数大小
     */
    private int transactionThreadMax = Runtime.getRuntime().availableProcessors() << 1;


    /**
     * netty 工作线程大小
     */
   // private int nettyThreadMax = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * 心跳时间 默认10秒
     */
   // private int heartTime = 10;


    /**
     * 线程池的拒绝策略 {@linkplain com.rains.transaction.common.enums.RejectedPolicyTypeEnum}
     */
    private String rejectPolicy = "Abort";

    /**
     * 线程池的队列类型 {@linkplain com.rains.transaction.common.enums.BlockingQueueTypeEnum}
     */
    private String blockingQueueType = "Linked";

    /**
     * 是否需要补偿
     */
    private Boolean compensation = false;

    /**
     * 补偿存储类型 {@linkplain com.rains.transaction.common.enums.CompensationCacheTypeEnum}
     */
    private String compensationCacheType;


    /**
     * 回滚队列大小
     */
    private int compensationQueueMax = 5000;
    /**
     * 监听回滚队列线程数
     */
    private int compensationThreadMax = Runtime.getRuntime().availableProcessors() << 1;


    /**
     * 补偿恢复时间 单位秒
     */
    private int compensationRecoverTime = 60;


    /**
     * 更新tmInfo 的时间间隔
     */
    private int refreshInterval = 60;


    /**
     * 最大重试次数
     */
    private int retryMax = 10;


    /**
     * 事务恢复间隔时间 单位秒（注意 此时间表示本地事务创建的时间多少秒以后才会执行）
     */
    private int recoverDelayTime = 60;





    /**
     * db存储方式时候 数据库配置信息
     */
    private TxDbConfig txDbConfig;

    /**
     * mongo存储方式时候的 mongo配置信息
     */
    private TxMongoConfig txMongoConfig;


    /**
     * redis存储方式时候的 redis配置信息
     */
    private TxRedisConfig txRedisConfig;

    /**
     * 文件存储配置
     */
    private TxFileConfig txFileConfig;

    /**
     * zookeeper 存储的配置
     */
    private TxZookeeperConfig txZookeeperConfig;


    public String getSerializer() {
        return serializer;
    }

    public void setSerializer(String serializer) {
        this.serializer = serializer;
    }

    public String getCompensationCacheType() {
        return compensationCacheType;
    }

    public void setCompensationCacheType(String compensationCacheType) {
        this.compensationCacheType = compensationCacheType;
    }
}
