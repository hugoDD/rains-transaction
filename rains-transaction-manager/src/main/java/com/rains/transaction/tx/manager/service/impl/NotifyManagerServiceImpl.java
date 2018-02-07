package com.rains.transaction.tx.manager.service.impl;

import com.rains.transaction.common.enums.TransactionStatusEnum;
import com.rains.transaction.common.holder.LogUtil;
import com.rains.transaction.common.netty.bean.TxTransactionGroup;
import com.rains.transaction.common.netty.bean.TxTransactionItem;
import com.rains.transaction.common.notify.CallbackListener;
import com.rains.transaction.tx.manager.config.Address;
import com.rains.transaction.tx.manager.config.ExecutorMessageTool;
import com.rains.transaction.tx.manager.service.NotifyManagerService;
import com.rains.transaction.tx.manager.socket.ListenerManager;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class NotifyManagerServiceImpl implements NotifyManagerService {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NotifyManagerServiceImpl.class);
    @Override
    public boolean notifyCommit(final String groupId,final Map<String, List<TxTransactionItem>> listMap) {
        List<TxTransactionItem> txTransactionItems = listMap.get(Address.getInstance().getDomain());
        final List<String> isAllNotifyCommitModleNames = new ArrayList<>();
        try {
            txTransactionItems.forEach(item -> {
                TxTransactionGroup txTransactionGroup = ExecutorMessageTool.buildNotifyMessage(item,TransactionStatusEnum.COMMIT);
                CallbackListener listener = ListenerManager.getInstance().getChannelByModelName(item.getModelName());
                boolean isNotifyCommit = false;
                if (Objects.nonNull(listener)) {
                    isNotifyCommit =listener.notify(txTransactionGroup);
                    LogUtil.info(LOGGER, "txManger 成功发送doCommit指令 事务taskKey为：{}", item::getTaskKey);
                } else {
                    LOGGER.error("txManger 发送doCommit指令失败，channel为空，事务组id：{}, 事务taskKey为:{}", groupId, item.getTaskKey());
                }
                if(isNotifyCommit){
                    isAllNotifyCommitModleNames.add(item.getModelName());
                }

                ListenerManager.getInstance().removeClient(listener);

            });
        } catch (Exception e) {
            LogUtil.info(LOGGER, "txManger 发送doCommit指令异常 ", e::getMessage);
        }finally {

        }
        return txTransactionItems.size()==isAllNotifyCommitModleNames.size();
    }

    @Override
    public boolean notifyRollBack(final String groupId,final Map<String, List<TxTransactionItem>> listMap) {
        List<TxTransactionItem> txTransactionItems = listMap.get(Address.getInstance().getDomain());
        try {
            if (CollectionUtils.isNotEmpty(txTransactionItems)) {
                final CompletableFuture[] cfs = txTransactionItems
                        .stream()
                        .map(item ->
                                CompletableFuture.runAsync(() -> {

                                   TxTransactionGroup txTransactionGroup =ExecutorMessageTool.buildNotifyMessage(item,
                                            TransactionStatusEnum.ROLLBACK);
                                    CallbackListener listener = ListenerManager.getInstance().getChannelByModelName(item.getModelName());
                                    if (Objects.nonNull(listener)) {
                                        listener.notify(txTransactionGroup);
                                        ListenerManager.getInstance().removeClient(listener);
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
        }
    }
}
