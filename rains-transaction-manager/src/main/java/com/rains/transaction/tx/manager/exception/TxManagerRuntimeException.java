
package com.rains.transaction.tx.manager.exception;

/*
 * 文 件 名:  TxManagerRuntimeException
 * 版    权:  Copyright (c) 2018 com.rains.hugosz
 * 描    述:  <描述>
 * 创 建 人:  hugosz
 * 创建时间:  2018/3/23  16:18
 */
public class TxManagerRuntimeException extends RuntimeException {
    private static final long serialVersionUID = -1949770547060521702L;

    public TxManagerRuntimeException() {
    }

    public TxManagerRuntimeException(String message) {
        super(message);
    }

    public TxManagerRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TxManagerRuntimeException(Throwable cause) {
        super(cause);
    }
}
