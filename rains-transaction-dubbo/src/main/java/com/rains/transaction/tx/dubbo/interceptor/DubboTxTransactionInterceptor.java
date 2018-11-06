
package com.rains.transaction.tx.dubbo.interceptor;


import com.alibaba.dubbo.rpc.RpcContext;
import com.rains.transaction.common.constant.CommonConstant;
import com.rains.transaction.core.interceptor.TxTransactionInterceptor;
import com.rains.transaction.core.service.AspectTransactionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
public class DubboTxTransactionInterceptor implements TxTransactionInterceptor {

    private final AspectTransactionService aspectTransactionService;

    @Autowired
    public DubboTxTransactionInterceptor(AspectTransactionService aspectTransactionService) {
        this.aspectTransactionService = aspectTransactionService;
    }


    @Override
    public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {
        String groupId = RpcContext.getContext().getAttachment(CommonConstant.TX_TRANSACTION_GROUP);
        return aspectTransactionService.invoke(groupId,pjp);
    }

}
