package com.rains.transaction.tx.manager.cluster;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.loadbalance.RandomLoadBalance;
import com.rains.transaction.common.netty.bean.TxTransactionGroup;
import com.rains.transaction.common.netty.bean.TxTransactionItem;
import com.rains.transaction.common.notify.ListenerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public class TxNotifyDubboLoadBalance extends RandomLoadBalance {
    private static final Logger LOGGER = LoggerFactory.getLogger(TxNotifyDubboLoadBalance.class);
    public TxNotifyDubboLoadBalance(){
        LOGGER.info("初始化tx manager dubbo load balance");
    }

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {

        String methodName =invocation.getMethodName();
       Object[] arg = invocation.getArguments();
       String groupId = "";
       String taskKey ="";
       if("notify".equals(methodName) ){
           if(arg!=null && arg.length>=1&&arg[0] instanceof TxTransactionGroup){
               TxTransactionGroup txTransactionGroup =(TxTransactionGroup) arg[0];

              TxTransactionItem item = txTransactionGroup.getItemList().get(0);
               groupId =item.getTxGroupId();
               taskKey= item.getTaskKey();
           }

       }

        //获取事务组内
        final Set<String> addressSet = ListenerManager.getInstance().containsByGroupId(groupId);



        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("当前事务组的地址列表:{}",addressSet);
        }

       for(final Invoker invoker : invokers){
           String rpcAddress = invoker.getUrl().getAddress()+":"+taskKey;

           if(addressSet.contains(rpcAddress)){
               LOGGER.info("获取到唤醒的地址:{}",rpcAddress);
               return invoker;
           }
       }

       return super.select(invokers,url,invocation);
    }
}