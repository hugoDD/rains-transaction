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
package com.rains.transaction.core.concurrent.threadpool;


import com.rains.transaction.common.config.TxConfig;
import com.rains.transaction.common.enums.BlockingQueueTypeEnum;
import com.rains.transaction.common.enums.RejectedPolicyTypeEnum;
import com.rains.transaction.common.holder.LogUtil;
import com.rains.transaction.core.concurrent.threadpool.policy.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;
import java.util.function.Supplier;

/*
 * 文 件 名:  TransactionThreadPool
 * 版    权:  Copyright (c) 2018 com.rains.hugosz
 * 描    述:  初始化事务运行线程池,根据配置选择不同的有界阻塞工作队列的容器,
 * 创 建 人:  hugosz
 * 创建时间:  2018/3/23  11:04
 */
@Component
public class TransactionThreadPool {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionThreadPool.class);

    private static final String THREAD_FACTORY_NAME = "txTransaction";
    private static final int MAX_ARRAY_QUEUE = 1000;
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE =
            new ScheduledThreadPoolExecutor(1,
                    TxTransactionThreadFactory.create(THREAD_FACTORY_NAME, false));
    private TxConfig txConfig;
    private ScheduledExecutorService scheduledExecutorService;
    private ExecutorService fixExecutorService;


    @Autowired
    public TransactionThreadPool(TxConfig txConfig) {
        this.txConfig = txConfig;
    }

    @PostConstruct
    public void init() {
        scheduledExecutorService = new ScheduledThreadPoolExecutor(txConfig.getTransactionThreadMax(),
                TxTransactionThreadFactory.create(THREAD_FACTORY_NAME, false));

        fixExecutorService = new ThreadPoolExecutor(txConfig.getTransactionThreadMax(), txConfig.getTransactionThreadMax(), 0, TimeUnit.MILLISECONDS,
                createBlockingQueue(),
                TxTransactionThreadFactory.create(THREAD_FACTORY_NAME, false), createPolicy());

    }

    /**
    *@Author: hugosz
    *@Description: 线程池的工作队列满时,处理的策略
    *@Date:  2018/3/23
    */
    private RejectedExecutionHandler createPolicy() {
        RejectedPolicyTypeEnum rejectedPolicyType = RejectedPolicyTypeEnum.fromString(txConfig.getRejectPolicy());

        switch (rejectedPolicyType) {
            case BLOCKING_POLICY:
                return new BlockingPolicy();
            case CALLER_RUNS_POLICY:
                return new CallerRunsPolicy();
            case ABORT_POLICY:
                return new AbortPolicy();
            case REJECTED_POLICY:
                return new RejectedPolicy();
            case DISCARDED_POLICY:
                return new DiscardedPolicy();
            default:
                return new RejectedPolicy();
        }
    }

    private BlockingQueue<Runnable> createBlockingQueue() {
        BlockingQueueTypeEnum queueType = BlockingQueueTypeEnum.fromString(txConfig.getBlockingQueueType());

        switch (queueType) {
            case LINKED_BLOCKING_QUEUE:
                return new LinkedBlockingQueue<>(1024);
            case ARRAY_BLOCKING_QUEUE:
                return new ArrayBlockingQueue<>(MAX_ARRAY_QUEUE);
            case SYNCHRONOUS_QUEUE:
                return new SynchronousQueue<>();
            default:
                return new LinkedBlockingQueue<>(1024);
        }

    }

    public ExecutorService newCustomFixedThreadPool(int threads) {
        return new ThreadPoolExecutor(threads, threads, 0, TimeUnit.MILLISECONDS,
                createBlockingQueue(),
                TxTransactionThreadFactory.create(THREAD_FACTORY_NAME, false), createPolicy());
    }

    public ExecutorService newFixedThreadPool() {
        return fixExecutorService;
    }

    public ExecutorService newSingleThreadExecutor() {
        return new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                createBlockingQueue(),
                TxTransactionThreadFactory.create(THREAD_FACTORY_NAME, false), createPolicy());
    }

    public ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return SCHEDULED_EXECUTOR_SERVICE;
    }

    public ScheduledExecutorService newScheduledThreadPool() {
        return scheduledExecutorService;
    }


    public ScheduledFuture multiScheduled(Supplier<Object> supplier) {
        return scheduledExecutorService
                .schedule(() -> {

                            final Boolean o = (Boolean) supplier.get();
                            if (o) {
                                LogUtil.info(LOGGER, "多线程执行任务调度成功,调度时间为:{}", txConfig::getDelayTime);
                            } else {
                                LogUtil.info(LOGGER, "多线程执行任务调度未执行任务,调度时间为:{}", txConfig::getDelayTime);
                            }
                        },
                        txConfig.getDelayTime(), TimeUnit.SECONDS);
    }

    public ScheduledFuture multiScheduled(Supplier<Object> supplier, int waitTime) {
        return scheduledExecutorService
                .schedule(() -> {
                            final Boolean o = (Boolean) supplier.get();
                            if (o) {
                                LogUtil.info(LOGGER, "多线程执行任务调度成功,调度时间为:{}", txConfig::getDelayTime);
                            } else {
                                LogUtil.info(LOGGER, "多线程执行任务调度未执行任务,调度时间为:{}", txConfig::getDelayTime);
                            }
                        },
                        waitTime, TimeUnit.SECONDS);
    }

    public ScheduledFuture singleThreadScheduled(Supplier<Object> supplier) {
        return SCHEDULED_EXECUTOR_SERVICE
                .schedule(() -> {
                            final Boolean o = (Boolean) supplier.get();
                            if (o) {
                                LogUtil.info(LOGGER, "单线程执行任务调度成功,调度时间为:{}", txConfig::getDelayTime);
                            } else {
                                LogUtil.info(LOGGER, "单线程执行任务调度未执行任务,调度时间为:{}", txConfig::getDelayTime);
                            }
                        },
                        txConfig.getDelayTime(), TimeUnit.SECONDS);
    }

}

