package com.rains.transaction.tx.manager.service.impl;

import com.rains.transaction.common.enums.TransactionRoleEnum;
import com.rains.transaction.common.enums.TransactionStatusEnum;
import com.rains.transaction.common.holder.LogUtil;
import com.rains.transaction.common.netty.bean.TxTransactionGroup;
import com.rains.transaction.common.netty.bean.TxTransactionItem;
import com.rains.transaction.common.notify.CallbackListener;
import com.rains.transaction.remote.service.TxManagerRemoteService;
import com.rains.transaction.tx.manager.config.Address;
import com.rains.transaction.tx.manager.service.TxManagerService;
import com.rains.transaction.tx.manager.socket.ListenerManager;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class TxManagerRemoteServiceImpl implements TxManagerRemoteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TxManagerRemoteServiceImpl.class);

    @Resource
    private TxManagerService txManagerService;


    @Resource
    private NotifyManagerServiceImpl notifyManagerService;

    @Override
    public Boolean createGroup(TxTransactionGroup txTransactionGroup, CallbackListener listener) {
        final List<TxTransactionItem> items = txTransactionGroup.getItemList();
        if (CollectionUtils.isNotEmpty(items)) {
            //String modelName = ctx.channel().remoteAddress().toString();
            String modelName = listener.getModeName();
            //这里创建事务组的时候，事务组也作为第一条数据来存储
            //第二条数据才是发起方 因此是get(1)
            final TxTransactionItem item = items.get(1);
            item.setModelName(modelName);
            item.setTmDomain(Address.getInstance().getDomain());
        }
        Boolean success = txManagerService.saveTxTransactionGroup(txTransactionGroup);
        if (success) {
            ListenerManager.getInstance().addClient(listener);
        }

        return success;
    }

    @Override
    public boolean addTransaction(TxTransactionItem item, CallbackListener listener) {
        if (item == null) {
            return false;
        }
        //  String modelName = ctx.channel().remoteAddress().toString();
        String modelName = listener.getModeName();

        item.setModelName(modelName);
        item.setTmDomain(Address.getInstance().getDomain());
        Boolean success = txManagerService.addTxTransaction(item.getTxGroupId(), item);

        if (success) {
            ListenerManager.getInstance().addClient(listener);
        }

        return success;
    }

    @Override
    public boolean perCommit(String groupId) {
        return txManagerService.updateTxTransactionItemStatus(groupId, groupId, TransactionStatusEnum.COMMIT.getCode(), null);
    }

    @Override
    public boolean completeCommit(TxTransactionItem item) {
        if (item == null) {
            return false;
        }
        Boolean iscompleteCommit = txManagerService.updateTxTransactionItemStatus(item.getTxGroupId(),
                item.getTaskKey(),
                item.getStatus(), item.getMessage());
        if(iscompleteCommit){
            LOGGER.info("事务组:{} 完全更新的状态为:{} 成功",item.getTxGroupId(),TransactionStatusEnum.acquireDescByCode(item.getStatus()));
        }else{
            LOGGER.info("事务组:{} 完全更新的状态为:{} 失败",item.getTxGroupId(),TransactionStatusEnum.acquireDescByCode(item.getStatus()));
        }
        return iscompleteCommit;
    }

    @Override
    public boolean rollback(String groupId) {
        try {
            txManagerService.updateTxTransactionItemStatus(groupId, groupId, TransactionStatusEnum.ROLLBACK.getCode(),null);
            final List<TxTransactionItem> txTransactionItems = txManagerService.listByTxGroupId(groupId);
            if (CollectionUtils.isNotEmpty(txTransactionItems)) {
                //过滤掉发起方的数据，发起方已经进行提交，不需要再通信进行
                final Map<String, List<TxTransactionItem>> listMap = txTransactionItems.stream()
                        .filter(item -> item.getRole() == TransactionRoleEnum.ACTOR.getCode())
                        .collect(Collectors.groupingBy(m -> m.getTmDomain()));
                if (Objects.isNull(listMap)) {
                    LogUtil.info(LOGGER, "事务组id:{},提交失败！数据不完整", () -> groupId);
                    return false;
                }
                notifyManagerService.notifyRollBack(groupId,listMap);
            }
        } finally {
            //txManagerService.removeRedisByTxGroupId(txGroupId);
        }
        return true;
    }

    @Override
    public int getTransactionGroupStatus(String groupId) {
        final int status = txManagerService.findTxTransactionGroupStatus(groupId);
        return status;
    }

    @Override
    public TxTransactionGroup findTransactionGroupInfo(TxTransactionGroup txTransactionGroup) {
        final List<TxTransactionItem> txTransactionItems = txManagerService.listByTxGroupId(txTransactionGroup.getId());
        txTransactionGroup.setItemList(txTransactionItems);

        return txTransactionGroup;
    }

    @Override
    public boolean notifyCommit(String groupId) {

        final List<TxTransactionItem> txTransactionItems = txManagerService.listByTxGroupId(groupId);
        //过滤掉发起方的数据，发起方已经进行提交，不需要再通信进行
        final Map<String, List<TxTransactionItem>> listMap = txTransactionItems.stream()
                .filter(item -> item.getRole() == TransactionRoleEnum.ACTOR.getCode())
                .collect(Collectors.groupingBy(m -> m.getTmDomain()));


        if (Objects.isNull(listMap)) {
            LogUtil.info(LOGGER, "事务组id:{},提交失败！数据不完整", () -> groupId);
            return false;
        }

        boolean isAllCommit = notifyManagerService.notifyCommit(groupId, listMap);
        if (!isAllCommit) {
            notifyManagerService.notifyRollBack(groupId, listMap);
        }
        return isAllCommit;
    }

    @Override
    public boolean notifyRollBack(String groupId) {

        final List<TxTransactionItem> txTransactionItems = txManagerService.listByTxGroupId(groupId);
        //过滤掉发起方的数据，发起方已经进行提交，不需要再通信进行
        final Map<String, List<TxTransactionItem>> listMap = txTransactionItems.stream()
                .filter(item -> item.getRole() == TransactionRoleEnum.ACTOR.getCode())
                .collect(Collectors.groupingBy(m -> m.getTmDomain()));

        if (Objects.isNull(listMap) || listMap.isEmpty()) {
            LogUtil.info(LOGGER, "事务组id:{},提交失败！数据不完整", () -> groupId);
            return false;
        }


        return     notifyManagerService.notifyRollBack(groupId, listMap);
    }


}
