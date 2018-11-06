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
package com.rains.transaction.common.bean;


import com.rains.transaction.common.enums.TransactionStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author dourx
 * @version V1.0
 * 创建日期 2018/11/6
 */
@Data
public class TransactionRecover implements Serializable {

    private static final long serialVersionUID = -3262858695515766275L;
    /**
     * 主键id
     */
    private String id;

    /**
     * 模块名
     */
    private String modelName;


    /**
     * 重试次数，
     */
    private int retriedCount = 0;

    /**
     * 创建时间
     */
    private Date createTime = new Date();


    /**
     * 创建时间
     */
    private Date lastTime = new Date();

    /**
     * 版本控制 防止并发问题
     */
    private int version = 1;

    /**
     * 事务组id
     */
    private String groupId;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 事务执行方法
     */
    private TransactionInvocation transactionInvocation;

    private String targetClass;

    private byte[] serializer;


    /**
     * {@linkplain TransactionStatusEnum}
     */
    private int status;


}
