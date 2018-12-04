package com.rains.transaction.core.service.impl;

import com.rains.transaction.common.enums.TransactionStatusEnum;
import com.rains.transaction.common.netty.bean.TxTransactionGroup;
import com.rains.transaction.common.netty.bean.TxTransactionItem;
import com.rains.transaction.common.notify.CallbackModel;
import com.rains.transaction.core.service.TxManagerMessageService;
import com.rains.transaction.remote.service.TxManagerRemoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class TxManagerLocalServiceImpl implements TxManagerMessageService {


    @Resource
    private TxManagerRemoteService txManagerRemoteService;
    @Resource
    private CallbackModel callbackModel;

    /**
     * 保存事务组 在事务发起方的时候进行调用
     *
     * @param txTransactionGroup 事务组
     * @return true 成功 false 失败
     */
    @Override
    public Boolean saveTxTransactionGroup(TxTransactionGroup txTransactionGroup) {

        return txManagerRemoteService.createGroup(txTransactionGroup);
    }

    /**
     * 往事务组添加事务
     *
     * @param txGroupId         事务组id
     * @param txTransactionItem 子事务项
     * @return true 成功 false 失败
     */
    @Override
    public Boolean addTxTransaction(String txGroupId, TxTransactionItem txTransactionItem) {

        txTransactionItem.setTxGroupId(txGroupId);
        txTransactionItem.setModelName(callbackModel.getModelName());
        txTransactionItem.setTmDomain(callbackModel.getModelDomain());
        return txManagerRemoteService.addTransaction(txTransactionItem);

    }

    /**
     * 获取事务组状态
     *
     * @param txGroupId 事务组id
     * @return 事务组状态
     */
    @Override
    public int findTransactionGroupStatus(String txGroupId) {
        return txManagerRemoteService.getTransactionGroupStatus(txGroupId);
    }

    /**
     * 获取事务组信息
     *
     * @param txGroupId 事务组id
     * @return TxTransactionGroup
     */
    @Override
    public TxTransactionGroup findByTxGroupId(String txGroupId) {
        TxTransactionGroup txTransactionGroup = new TxTransactionGroup();
        txTransactionGroup.setId(txGroupId);
        return txManagerRemoteService.findTransactionGroupInfo(txTransactionGroup);
    }

    /**
     * 通知tm 回滚整个事务组
     *
     * @param txGroupId 事务组id
     * @return true 成功 false 失败
     */
    @Override
    public Boolean rollBackTxTransaction(String txGroupId) {
        return txManagerRemoteService.rollback(txGroupId);
    }

    /**
     * 通知tm自身业务已经执行完成，等待提交事务
     * tm 收到后，进行pre_commit  再进行doCommit
     *
     * @param txGroupId 事务组id
     * @return true 成功 false 失败
     */
    @Override
    public Boolean preCommitTxTransaction(String txGroupId) {
        boolean preCommit = txManagerRemoteService.perCommit(txGroupId);
        boolean commit = false;
        if (preCommit) {
            commit = txManagerRemoteService.notifyCommit(txGroupId);
        } else {
            commit = txManagerRemoteService.notifyRollBack(txGroupId);
        }
        log.info("事务组:{} 预提交成功否:{}", txGroupId, commit);
        return commit;
    }

    /**
     * 完成提交自身的事务
     *
     * @param txGroupId 事务组id
     * @param taskKey   子事务的taskKey
     * @param status    状态  {@linkplain TransactionStatusEnum}
     * @return true 成功 false 失败
     */
    @Override
    public Boolean completeCommitTxTransaction(String txGroupId, String taskKey, int status) {
        TxTransactionItem item = new TxTransactionItem();
        item.setTxGroupId(txGroupId);
        item.setTaskKey(taskKey);
        item.setStatus(status);

        return txManagerRemoteService.completeCommit(item);
    }

    /**
     * 异步完成自身事务的提交
     *
     * @param txGroupId 事务组id
     * @param taskKey   子事务的taskKey
     * @param status    状态  {@linkplain TransactionStatusEnum}
     * @param message   完成信息 返回结果，或者是异常信息
     */
    @Override
    @Async
    public void asyncCompleteCommit(String txGroupId, String taskKey, int status, Object message) {


        TxTransactionItem item = new TxTransactionItem();
        item.setTaskKey(taskKey);
        item.setStatus(status);
        item.setMessage(message);
        item.setTxGroupId(txGroupId);

        txManagerRemoteService.completeCommit(item);
    }

    /**
     * 提交参与者事务状态
     *
     * @param txGroupId         事务组id
     * @param txTransactionItem 参与者
     * @param status            状态
     * @return true 成功 false 失败
     */
    @Override
    public Boolean commitActorTxTransaction(String txGroupId, TxTransactionItem txTransactionItem, int status) {

        return null;
    }


//    private void receivedCommand(String key, boolean success) {
//        int result =success?NettyResultEnum.SUCCESS.getCode():NettyResultEnum.FAIL.getCode();
//
//        final BlockTask blockTask = BlockTaskHelper.getInstance().getTask(key);
//        if (Objects.nonNull(blockTask)) {
//            blockTask.setAsyncCall(objects -> result == NettyResultEnum.SUCCESS.getCode());
//            blockTask.signal();
//        }
//    }
}
