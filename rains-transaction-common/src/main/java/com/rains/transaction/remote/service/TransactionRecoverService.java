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
package com.rains.transaction.remote.service;


import com.rains.transaction.common.bean.TransactionRecover;
import com.rains.transaction.common.config.TxConfig;
import com.rains.transaction.common.exception.TransactionRuntimeException;

import java.util.Date;
import java.util.List;

/**
 * @author dourx
 * @version V1.0
 * 创建日期 2018/11/5
 */
public interface TransactionRecoverService {

    /**
     * 创建本地事务对象
     *
     * @param transactionRecover 事务对象
     * @return rows
     */
    int create(TransactionRecover transactionRecover);

    /**
     * 删除对象
     *
     * @param id 事务对象id
     * @return rows
     */
    int remove(String id);


    /**
     * 更新数据
     *
     * @param transactionRecover 事务对象
     * @return rows 1 成功 0 失败 失败需要抛异常
     * @throws TransactionRuntimeException 更新异常
     */
    int update(TransactionRecover transactionRecover) throws TransactionRuntimeException;

    /**
     * 根据id获取对象
     *
     * @param id 主键id
     * @return TransactionRecover
     */
    TransactionRecover findById(String id);

    /**
     * 获取需要提交的事务
     *
     * @return List<TransactionRecover>
     */
    List<TransactionRecover> listAll();


    /**
     * 获取延迟多长时间后的事务信息,只要为了防止并发的时候，刚新增的数据被执行
     *
     * @param date 延迟后的时间
     * @return List<TransactionRecover>
     */
    List<TransactionRecover> listAllByDelay(Date date);


    /**
     * 初始化操作
     *
     * @param modelName 模块名称
     * @param txConfig  配置信息
     * @throws Exception 初始化异常信息
     */
    void init(String modelName, TxConfig txConfig) throws Exception;

    /**
     * 设置scheme
     *
     * @return scheme 命名
     */
    String getScheme();



}
