
package com.rains.transaction.common.notify;

import com.rains.transaction.common.exception.TransactionRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(ListenerManager.class);
    private static final ThreadLocal<String> CURRENT_LOCAL = new ThreadLocal<>();

    /**
     * 最大连接数
     */
    private int maxConnection = 500;

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


    private Map<String, Set<String>> clientsMap =new ConcurrentHashMap<>();


    private final static ListenerManager manager = new ListenerManager();

    private ListenerManager() {
    }

    public static ListenerManager getInstance() {
        return manager;
    }


    public Set<String> containsByGroupId(final String groupId) {
        if (Objects.isNull(groupId)) {
            LOGGER.error("参数groupId不能为空,请检查当前事务是否有效,将结束事务");
            throw new TransactionRuntimeException("参数groupId为空,将rollback事务");
        }
            return clientsMap.get(groupId);
    }

    public Set<String> getAddressForCurLocal() {
       final String groupId =CURRENT_LOCAL.get();
        if (Objects.isNull(groupId)) {
            LOGGER.error("参数groupId不能为空,请检查当前事务是否有效,将结束事务");
            throw new TransactionRuntimeException("参数groupId为空,将rollback事务");
        }
        return clientsMap.get(groupId);
    }



    public void addClient(final String groupId,final String address) {
       // clients.add(client);
        if(Objects.isNull(groupId)){
            throw new TransactionRuntimeException("callbackListtener client为空");
        }
        if(!allowConnection){
            throw new TransactionRuntimeException("callbackListtener client缓存队列已满");
        }
        Set<String> addressSet = clientsMap.get(groupId);
        if(Objects.isNull(addressSet)){
            addressSet = new HashSet<>();
            clientsMap.put(groupId,addressSet);

            allowConnection = (maxConnection != nowConnection.incrementAndGet());
        }
        addressSet.add(address);

        //设置当前groupId在当前线程中
        CURRENT_LOCAL.set(groupId);

    }

    public void removeClient(final String groupId) {
        clientsMap.remove(groupId);
        CURRENT_LOCAL.remove();
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
