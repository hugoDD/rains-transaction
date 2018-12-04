package com.rains.transaction.tx.manager.cluster;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.loadbalance.RandomLoadBalance;
import com.rains.transaction.common.notify.ListenerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public class TxNotifyDubboLoadBalance extends RandomLoadBalance {
    private static final Logger LOGGER = LoggerFactory.getLogger(TxNotifyDubboLoadBalance.class);

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {

        //获取事务组内
        final Set<String> addressSet = ListenerManager.getInstance().getAddressForCurLocal();

        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("当前事务组的地址列表:{}",addressSet);
        }

       for(final Invoker invoker : invokers){
           String rpcAddress = invoker.getUrl().getAddress();
           if(addressSet.contains(rpcAddress)){
               LOGGER.info("获取到唤醒的地址:{}",rpcAddress);
               return invoker;
           }
       }

       return super.select(invokers,url,invocation);
    }
}