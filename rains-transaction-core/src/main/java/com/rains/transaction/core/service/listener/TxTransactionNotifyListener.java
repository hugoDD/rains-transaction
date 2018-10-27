package com.rains.transaction.core.service.listener;

import com.rains.transaction.common.netty.bean.TxTransactionGroup;
import com.rains.transaction.common.netty.bean.TxTransactionItem;
import com.rains.transaction.common.notify.CallbackListener;
import com.rains.transaction.core.concurrent.task.BlockTask;
import com.rains.transaction.core.concurrent.task.BlockTaskHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;


import java.util.List;

@Slf4j
public class TxTransactionNotifyListener implements CallbackListener {

    private String modelName;


    public TxTransactionNotifyListener() {
    }

    public TxTransactionNotifyListener(String modelNameService) {
        this.modelName = modelNameService;
    }


    /**
     * 通知提交或者本地事务
     *
     * @param txTransactionGroup 当前事务组
     * @return 本地事务提交是否成功
     */
    public boolean notify(TxTransactionGroup txTransactionGroup) {
        final List<TxTransactionItem> txTransactionItems = txTransactionGroup
                .getItemList();
        if ( !CollectionUtils.isEmpty(txTransactionItems) ) {
            final TxTransactionItem item = txTransactionItems.get(0);
            log.info("唤醒task key :{}", item.getTaskKey());
            final BlockTask task = BlockTaskHelper.getInstance().getTask(item.getTaskKey());
            task.setAsyncCall(objects -> item.getStatus());
            task.signal();
        }
        return true;
    }


    /**
     * 当前服务名称，在tm时用以获取当前监听对象
     *
     * @return
     */
    @Override
    public String getModeName() {
        return this.modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
