package com.happylifeplat.transaction.remote.service;

import com.happylifeplat.transaction.common.netty.bean.TxTransactionGroup;
import com.happylifeplat.transaction.common.netty.bean.TxTransactionItem;
import com.happylifeplat.transaction.common.notify.CallbackListener;

public interface TxManagerRemoteService {

    /**
     * Begin transaction status enum.0
     * 创建事务组
     */
   Boolean createGroup(TxTransactionGroup txTransactionGroup,CallbackListener listener);


    /**
     * Add transaction netty message action enum. 1
     * 添加事务
     */
    boolean addTransaction(TxTransactionItem item,CallbackListener listener);



    /**
     * (3, "预提交")
     */
   boolean perCommit(String groupId);


    /**
     * (4, "完成提交"),
     */
   boolean completeCommit(TxTransactionItem item);

    /**
     * (5, "回滚"),
     */
    boolean rollback(String groupId);



    /**
     * Get transaction group netty message action enum.
     * 获取事务组状态
     */
   int getTransactionGroupStatus(String groupId);


    /**
     * 获取事务组信息
     * @param txTransactionGroup 事务组
     * @return 事务级信息
     */
    TxTransactionGroup findTransactionGroupInfo(TxTransactionGroup txTransactionGroup);

    /**
     * 通知回调所用参与者提交本地事务
     * @param groupId 事务组id
     * @return 是否成功通知所有参与者
     */
    boolean notifyCommit(String groupId);

    /**
     * 通知回调所用参与者回滚本地事务
     * @param groupId 事务组id
     * @return 是否成功通知所有参与者
     */
    boolean notifyRollBack(String groupId);
}
