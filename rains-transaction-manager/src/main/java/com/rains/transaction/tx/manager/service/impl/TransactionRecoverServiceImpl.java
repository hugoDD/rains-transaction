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
package com.rains.transaction.tx.manager.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.rains.transaction.common.bean.TransactionRecover;
import com.rains.transaction.common.config.TxConfig;
import com.rains.transaction.common.exception.TransactionRuntimeException;
import com.rains.transaction.remote.service.TransactionRecoverService;
import com.rains.transaction.tx.manager.spi.TransactionRecoverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/*
 * 文 件 名:  TransactionRecover
 * 版    权:  Copyright (c) 2018 com.rains.hugosz
 * 描    述:  本地补偿事务
 * 创 建 人:  hugosz
 * 创建时间:  2018/3/26  16:39
 */
@Service
@Transactional
public class TransactionRecoverServiceImpl implements TransactionRecoverService {

    @Autowired
    private TransactionRecoverRepository transactionRecoverRepository;
    /**
     * 创建本地事务对象
     *
     * @param transactionRecover 事务对象
     * @return rows
     */
   public  int create(TransactionRecover transactionRecover){
      return transactionRecoverRepository.create(transactionRecover);
    }

    /**
     * 删除对象
     *
     * @param id 事务对象id
     * @return rows
     */
    public int remove(String id){
        return transactionRecoverRepository.remove(id);
    }


    /**
     * 更新数据
     *
     * @param transactionRecover 事务对象
     * @return rows 1 成功 0 失败 失败需要抛异常
     * @throws TransactionRuntimeException 更新异常
     */
    public int update(TransactionRecover transactionRecover) throws TransactionRuntimeException{
        return transactionRecoverRepository.update(transactionRecover);
    }

    /**
     * 根据id获取对象
     *
     * @param id 主键id
     * @return TransactionRecover
     */
    public TransactionRecover findById(String id){
        return transactionRecoverRepository.findById(id);
    }

    /**
     * 获取需要提交的事务
     *
     * @return List<TransactionRecover>
     */
    public List<TransactionRecover> listAll(){
        return transactionRecoverRepository.listAll();
    }


    /**
     * 获取延迟多长时间后的事务信息,只要为了防止并发的时候，刚新增的数据被执行
     *
     * @param date 延迟后的时间
     * @return List<TransactionRecover>
     */
    public List<TransactionRecover> listAllByDelay(Date date){
        return transactionRecoverRepository.listAllByDelay(date);
    }


    /**
     * 初始化操作
     *
     * @param modelName 模块名称
     * @param txConfig  配置信息
     * @throws Exception 初始化异常信息
     */
    public void init(String modelName, TxConfig txConfig) throws Exception{
        transactionRecoverRepository.init(modelName,txConfig);
    }

    /**
     * 设置scheme
     *
     * @return scheme 命名
     */
    public String getScheme(){
        return transactionRecoverRepository.getScheme();
    }



}
