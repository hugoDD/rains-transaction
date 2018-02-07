package com.happylifeplat.transaction.tx.manager.service;

import com.happylifeplat.transaction.common.netty.bean.TxTransactionItem;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface NotifyManagerService {
    boolean notifyCommit(final  String groupId,final Map<String, List<TxTransactionItem>> listMap);
    boolean notifyRollBack(final String groupId,final Map<String, List<TxTransactionItem>> listMap);
}
