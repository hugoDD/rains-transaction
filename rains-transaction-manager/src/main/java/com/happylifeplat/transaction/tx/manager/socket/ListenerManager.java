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
package com.happylifeplat.transaction.tx.manager.socket;

import com.google.common.collect.Lists;
import com.happylifeplat.transaction.common.notify.CallbackListener;
import io.netty.channel.Channel;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author xiaoyu
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

    private int nowConnection;

    /**
     * 允许连接请求 true允许 false拒绝
     */
    private volatile boolean allowConnection = true;

    private List<CallbackListener> clients = Lists.newCopyOnWriteArrayList();

    private static ListenerManager manager = new ListenerManager();

    private ListenerManager() {
    }

    public static ListenerManager getInstance() {
        return manager;
    }


    public CallbackListener getChannelByModelName(String name) {
        if (CollectionUtils.isNotEmpty(clients)) {
            final Optional<CallbackListener> first = clients.stream().filter(client ->
                    Objects.equals(client.getModeName(), name))
                    .findFirst();
            return first.orElse(null);
        }
        return null;
    }


    public void addClient(CallbackListener client) {
        clients.add(client);
        nowConnection = clients.size();
        allowConnection = (maxConnection != nowConnection);
    }

    public void removeClient(CallbackListener client) {
        clients.remove(client);
        nowConnection = clients.size();
        allowConnection = (maxConnection != nowConnection);
    }


    public int getMaxConnection() {
        return maxConnection;
    }

    public int getNowConnection() {
        return nowConnection;
    }

    public boolean isAllowConnection() {
        return allowConnection;
    }
}
