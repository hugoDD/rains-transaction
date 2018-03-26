
package com.rains.transaction.tx.dubbo.service;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.rains.transaction.core.service.ModelNameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/*
 * 文 件 名:  DubboModelNameServiceImpl
 * 版    权:  Copyright (c) 2018 com.rains.hugosz
 * 描    述:  <描述>
 * 创 建 人:  hugosz
 * 创建时间:  2018/3/23  17:59
 */
@Service
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
