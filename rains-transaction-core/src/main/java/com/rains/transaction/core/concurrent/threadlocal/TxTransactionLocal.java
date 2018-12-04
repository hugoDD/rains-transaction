
package com.rains.transaction.core.concurrent.threadlocal;


/**
 * @author dourx
 */
public class TxTransactionLocal {

    private static final TxTransactionLocal TX_TRANSACTION_LOCAL = new TxTransactionLocal();
    private static final ThreadLocal<String> CURRENT_LOCAL = new ThreadLocal<>();

    private TxTransactionLocal() {

    }

    public static TxTransactionLocal getInstance() {
        return TX_TRANSACTION_LOCAL;
    }

    public String getTxGroupId() {
        return CURRENT_LOCAL.get();
    }

    public void setTxGroupId(String txGroupId) {
        CURRENT_LOCAL.set(txGroupId);
    }

    public void removeTxGroupId() {
        CURRENT_LOCAL.remove();
    }


}
