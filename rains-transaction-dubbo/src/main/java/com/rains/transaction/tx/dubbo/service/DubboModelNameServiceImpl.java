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
package com.rains.transaction.tx.dubbo.service;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.rains.transaction.core.service.ModelNameService;
import org.springframework.beans.factory.annotation.Autowired;

/*
 * 文 件 名:  DubboModelNameServiceImpl
 * 版    权:  Copyright (c) 2018 com.rains.hugosz
 * 描    述:  <描述>
 * 创 建 人:  hugosz
 * 创建时间:  2018/3/23  17:59
 */
public class DubboModelNameServiceImpl implements ModelNameService {


    /**
     * dubbo ApplicationConfig
     */
    @Autowired(required = false)
    private ApplicationConfig applicationConfig;

    @Override
    public String findModelName() {
        return applicationConfig.getName();
    }
}
