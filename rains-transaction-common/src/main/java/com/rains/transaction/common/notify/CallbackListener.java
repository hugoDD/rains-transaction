
package com.rains.transaction.common.notify;



import com.rains.transaction.common.netty.bean.TxTransactionGroup;

import java.io.Serializable;


public interface CallbackListener extends Serializable {
    /**
     * 通知提交或者回滚本地事务
     *
     * @param txTransactionGroup 当前事务组
     * @return 本地事务提交是否成功
     */
    boolean notify(TxTransactionGroup txTransactionGroup);

    /**
     * 当前服务名称，在tm时用以获取当前监听对象
     *
     * @return 模块名称
     */
    String getModeName();
}
