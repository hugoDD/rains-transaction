package com.rains.transaction.tx.manager.service.impl;

import com.rains.transaction.common.enums.TransactionStatusEnum;
import com.rains.transaction.common.holder.LogUtil;
import com.rains.transaction.common.netty.bean.TxTransactionGroup;
import com.rains.transaction.common.netty.bean.TxTransactionItem;
import com.rains.transaction.common.notify.ListenerManager;
import com.rains.transaction.tx.manager.config.ExecutorMessageTool;
import com.rains.transaction.tx.manager.service.CallBackListenerLocal;
import com.rains.transaction.tx.manager.service.NotifyManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
public class NotifyManagerServiceImpl implements NotifyManagerService {

    @Autowired
    private CallBackListenerLocal listener;

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NotifyManagerServiceImpl.class);
    @Override
    public boolean notifyCommit(final String groupId,final List<TxTransactionItem> txTransactionItems) {
        final List<String> isAllNotifyCommitModleNames = new ArrayList<>();
        try {
            txTransactionItems.forEach(item -> {
                TxTransactionGroup txTransactionGroup = ExecutorMessageTool.buildNotifyMessage(item,TransactionStatusEnum.COMMIT);
                ListenerManager.getInstance().addClient(item.getTxGroupId(),item.getTmDomain()+":"+item.getTaskKey());
                //CallbackModel listener = ListenerManager.getInstance().getChannelByModelName(item.getModelName());
                boolean isNotifyCommit = false;
                if (Objects.nonNull(txTransactionGroup)) {
                    isNotifyCommit = listener.notify(txTransactionGroup);
                    LogUtil.info(LOGGER, "txManger 成功发送doCommit指令 事务taskKey为：{}", item::getTaskKey);
                } else {
                    LOGGER.error("txManger 发送doCommit指令失败，channel为空，事务组id：{}, 事务taskKey为:{}", groupId, item.getTaskKey());
                }
                if(isNotifyCommit){
                    isAllNotifyCommitModleNames.add(item.getModelName());
                }



            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
            LogUtil.info(LOGGER, "txManger 发送doCommit指令异常 ", e::getMessage);
        }finally {
            ListenerManager.getInstance().removeClient(groupId);
        }
        return txTransactionItems.size()==isAllNotifyCommitModleNames.size();
    }

    @Override
    public boolean notifyRollBack(final String groupId,final List<TxTransactionItem> txTransactionItems) {
       // List<TxTransactionItem> txTransactionItems = listMap.get(Address.getInstance().getDomain());
        try {
            if (!CollectionUtils.isEmpty(txTransactionItems)) {
                final CompletableFuture[] cfs = txTransactionItems
                        .stream()
                        .map(item ->
                                CompletableFuture.runAsync(() -> {

                                   TxTransactionGroup txTransactionGroup =ExecutorMessageTool.buildNotifyMessage(item,
                                            TransactionStatusEnum.ROLLBACK);
                                    ListenerManager.getInstance().addClient(item.getTxGroupId(),item.getTmDomain()+":"+item.getTaskKey());
                                   // CallbackListener listener = ListenerManager.getInstance().getChannelByModelName(item.getModelName());
                                    if (Objects.nonNull(txTransactionGroup)) {
                                        listener.notify(txTransactionGroup);

//                                        channelSender.getChannel().writeAndFlush(heartBeat);
                                    } else {
                                        LOGGER.error("txManger rollback指令失败，listener为空，事务组id：{}, 事务taskId为:{}",
                                                item.getTxGroupId(), item.getTaskKey());
                                    }

                                }).whenComplete((v, e) ->
                                        LogUtil.info(LOGGER, "txManger 成功发送rollback指令 事务taskId为：{}", item::getTaskKey)))
                        .toArray(CompletableFuture[]::new);
                CompletableFuture.allOf(cfs).join();
                LogUtil.info(LOGGER, "txManger 成功发送rollback指令 事务组id为：{}", () -> groupId);
            }
           // httpExecute(elseItems, TransactionStatusEnum.ROLLBACK);
            return true;
        } catch (Exception e) {

            LogUtil.info(LOGGER, "txManger 发送rollback指令异常 ", e::getMessage);
            return false;
        }finally {
            ListenerManager.getInstance().removeClient(groupId);
        }
    }
}
