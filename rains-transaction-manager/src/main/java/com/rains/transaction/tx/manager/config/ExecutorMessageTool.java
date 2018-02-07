/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.rains.transaction.tx.manager.config;

import com.rains.transaction.common.enums.TransactionStatusEnum;
import com.rains.transaction.common.netty.bean.TxTransactionGroup;
import com.rains.transaction.common.netty.bean.TxTransactionItem;

import java.util.Collections;

/**
 * @author dourx
 */
public class ExecutorMessageTool {



    public static TxTransactionGroup buildNotifyMessage(TxTransactionItem item, TransactionStatusEnum transactionStatusEnum) {

        TxTransactionGroup txTransactionGroup = new TxTransactionGroup();
        item.setStatus(transactionStatusEnum.getCode());
        txTransactionGroup.setStatus(transactionStatusEnum.getCode());

        txTransactionGroup.setItemList(Collections.singletonList(item));

        return txTransactionGroup;
    }
}
