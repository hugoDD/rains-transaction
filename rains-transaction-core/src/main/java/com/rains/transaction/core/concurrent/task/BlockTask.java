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
package com.rains.transaction.core.concurrent.task;


import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * 文 件 名:  BlockTask
 * 版    权:  Copyright (c) 2018 com.rains.hugosz
 * 描    述:  <描述>
 * 创 建 人:  hugosz
 * 创建时间:  2018/3/23  11:19
 */
@Slf4j
public class BlockTask {

    /**
     * 是否被唤醒
     */
    private volatile static boolean Notify = false;
    /**
     * 是否被唤醒
     */
    private volatile static boolean remove = false;
    private Lock lock;
    private Condition condition;
    private AsyncCall asyncCall;
    /**
     * 唯一标示key
     */
    private String key;

    /**
     * 数据状态用于业务处理
     */
    private int state = 0;


    public BlockTask() {
        lock = new ReentrantLock();
        condition = lock.newCondition();

    }

    public static boolean isRemove() {
        return remove;
    }

    public static void setRemove(boolean remove) {
        BlockTask.remove = remove;
    }


    public void signal() {
        try {
            lock.lock();
            Notify = true;
            condition.signal();
        } finally {
            lock.unlock();
        }

    }

    public void await() {
        try {
            lock.lock();
            condition.await();
        } catch (InterruptedException e) {
            log.error("阻塞线程中断");
            log.error(e.getMessage(),e);

        } finally {
            lock.unlock();
        }
    }

    public AsyncCall getAsyncCall() {
        return asyncCall;
    }

    public void setAsyncCall(AsyncCall asyncCall) {
        this.asyncCall = asyncCall;
    }

    public boolean isNotify() {
        return Notify;
    }

    public static void setNotify(boolean notify) {
        Notify = notify;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }


}
