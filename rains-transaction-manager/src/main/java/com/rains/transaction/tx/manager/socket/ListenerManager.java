
package com.rains.transaction.tx.manager.socket;

import com.rains.transaction.common.notify.CallbackListener;
import com.rains.transaction.tx.manager.exception.TxManagerRuntimeException;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * 文 件 名:  ListenerManager
 * 版    权:  Copyright (c) 2018 com.rains.hugosz
 * 描    述:  事务参与者事务注册队列
 * 创 建 人:  hugosz
 * 创建时间:  2018/3/23  15:36
 */
public class ListenerManager {

    /**
     * 最大连接数
     */
    private int maxConnection = 50;

    public void setMaxConnection(int maxConnection) {
        this.maxConnection = maxConnection;
    }

    /**
     * 当前连接数
     */

    private AtomicInteger nowConnection = new AtomicInteger();

    /**
     * 允许连接请求 true允许 false拒绝
     */
    private volatile boolean allowConnection = true;


    private Map<String,CallbackListener> clientsMap =new ConcurrentHashMap<>();


    private final static ListenerManager manager = new ListenerManager();

    private ListenerManager() {
    }

    public static ListenerManager getInstance() {
        return manager;
    }


    public CallbackListener getChannelByModelName(String name) {
        if (Objects.nonNull(clientsMap)) {
            return clientsMap.get(name);
        }
        return null;
    }


    public void addClient(CallbackListener client) {
       // clients.add(client);
        if(Objects.isNull(client)){
            throw new TxManagerRuntimeException("callbackListtener client为空");
        }
        if(!allowConnection){
            throw new TxManagerRuntimeException("callbackListtener client缓存队列已满");
        }
        clientsMap.put(client.getModeName(),client);
        allowConnection = (maxConnection != nowConnection.incrementAndGet());
    }

    public void removeClient(CallbackListener client) {
        clientsMap.remove(client.getModeName());
        allowConnection = (maxConnection != nowConnection.decrementAndGet());
    }


    public int getMaxConnection() {
        return maxConnection;
    }

    public int getNowConnection() {
        return nowConnection.get();
    }

    public boolean isAllowConnection() {
        return allowConnection;
    }
}
