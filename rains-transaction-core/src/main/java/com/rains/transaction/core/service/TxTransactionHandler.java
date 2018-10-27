
package com.rains.transaction.core.service;

import com.rains.transaction.common.bean.TxTransactionInfo;
import org.aspectj.lang.ProceedingJoinPoint;

/*
 * 文 件 名:  TxTransactionHandler
 * 版    权:  Copyright (c) 2018 com.rains.hugosz
 * 描    述:  事务处理接口
 * 创 建 人:  hugosz
 * 创建时间:  2018/3/29  15:41
 */
public interface TxTransactionHandler {

    /**
     * 分布式事务处理接口
     *
     * @param point point 切点
     * @param info  信息
     * @return Object
     * @throws Throwable 异常
     */
    Object handler(ProceedingJoinPoint point, TxTransactionInfo info) throws Throwable;
}
