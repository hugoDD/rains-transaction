package com.rains.transaction.tx.manager.service;

import com.rains.transaction.common.netty.bean.TxTransactionItem;

import java.util.List;

public interface NotifyManagerService {
    boolean notifyCommit(final  String groupId,final List<TxTransactionItem> items);
    boolean notifyRollBack(final String groupId,final  List<TxTransactionItem> items);
}
