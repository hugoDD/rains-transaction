
package com.rains.transaction.core.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;

/*
 * 文 件 名:  TxTransactionInterceptor
 * 版    权:  Copyright (c) 2018 com.rains.hugosz
 * 描    述:  事务切面的拦截方法
 * 创 建 人:  hugosz
 * 创建时间:  2018/3/31  11:01
 */
public interface TxTransactionInterceptor {

    /**
     * 事务切面的拦截方法
     *
     * @param pjp spring事务切点
     * @return Object
     * @throws Throwable 异常
     */
    Object interceptor(ProceedingJoinPoint pjp) throws Throwable;
}
