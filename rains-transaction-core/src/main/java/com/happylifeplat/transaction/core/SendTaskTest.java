package com.happylifeplat.transaction.core;

import com.happylifeplat.transaction.common.enums.NettyMessageActionEnum;
import com.happylifeplat.transaction.common.enums.NettyResultEnum;
import com.happylifeplat.transaction.common.holder.IdWorkerUtils;
import com.happylifeplat.transaction.common.netty.bean.HeartBeat;
import com.happylifeplat.transaction.common.netty.bean.TxTransactionGroup;
import com.happylifeplat.transaction.core.concurrent.task.BlockTask;
import com.happylifeplat.transaction.core.concurrent.task.BlockTaskHelper;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SendTaskTest {
    public static void main(String[] args){
        System.out.println("测试开始");
        HeartBeat heartBeat = new HeartBeat();
        heartBeat.setAction(NettyMessageActionEnum.CREATE_GROUP.getCode());
        heartBeat.setTxTransactionGroup(new TxTransactionGroup ());
        SendTaskTest sendTaskTest = new SendTaskTest();
        Object res=sendTaskTest.sendTxManagerMessage( heartBeat);
        System.out.println("主线程结束");
    }

    /**
     * 向TxManager 发生消息
     *
     * @param heartBeat 定义的数据传输对象
     * @return Object
     */
    public Object sendTxManagerMessage(HeartBeat heartBeat) {
            System.out.println("主线程进入 sendTxManagerMessage方法 ");
            final String sendKey = IdWorkerUtils.getInstance().createTaskKey();
        System.out.println("主线程获取sendTask: "+sendKey);
            BlockTask sendTask = BlockTaskHelper.getInstance().getTask(sendKey);
            heartBeat.setKey(sendKey);
        ScheduledFuture schedule= Executors.newScheduledThreadPool(2).schedule(() -> {
            System.out.println("开始子线程获取notify: "+sendTask.isNotify());
            if (!sendTask.isNotify()) {
                if (NettyMessageActionEnum.GET_TRANSACTION_GROUP_STATUS.getCode()
                        == heartBeat.getAction()) {
                    sendTask.setAsyncCall(objects -> NettyResultEnum.TIME_OUT.getCode());
                } else if (NettyMessageActionEnum.FIND_TRANSACTION_GROUP_INFO.getCode()
                        == heartBeat.getAction()) {
                    sendTask.setAsyncCall(objects -> null);
                } else {
                    sendTask.setAsyncCall(objects -> false);
                }
                System.out.println("子线程获取唤醒主线程");
                sendTask.signal();
            }
        },10,TimeUnit.SECONDS);

            //发送线程在此等待，等tm是否 正确返回（正确返回唤醒） 返回错误或者无返回通过上面的调度线程唤醒
           System.out.println("主线程等待");
            sendTask.await();
        System.out.println("主线程结束等待");
            //如果已经被唤醒，就不需要去执行调度线程了 ，关闭上面的调度线程池中的任务
            if (!schedule.isDone()) {
                schedule.cancel(false);
            }

            try {
                return sendTask.getAsyncCall().callBack();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            } finally {
                BlockTaskHelper.getInstance().removeByKey(sendKey);
            }

        }

}
