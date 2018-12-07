package com.rains.transaction.tx.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.rains.transaction.common.netty.bean.TxTransactionGroup;
import com.rains.transaction.remote.service.CallbackListener;
import com.rains.transaction.tx.manager.service.CallBackListenerLocal;
import org.springframework.stereotype.Service;

@Service
public class CallBackListenerDubboLocal implements CallBackListenerLocal {

    @Reference(check = false,loadbalance = "txNotifyDubbo")
    private CallbackListener listener;

    @Override
    public boolean notify(TxTransactionGroup txTransactionGroup) {
        return listener.notify(txTransactionGroup);
    }

    @Override
    public String getModeName() {
        return listener.getModeName();
    }
}
