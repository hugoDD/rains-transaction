/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rains.transaction.core.compensation.impl;

import com.rains.transaction.common.bean.TransactionInvocation;
import com.rains.transaction.common.bean.TransactionRecover;
import com.rains.transaction.common.config.TxConfig;
import com.rains.transaction.common.constant.CommonConstant;
import com.rains.transaction.common.enums.CompensationActionEnum;
import com.rains.transaction.common.enums.TransactionStatusEnum;
import com.rains.transaction.common.holder.LogUtil;
import com.rains.transaction.common.netty.bean.TxTransactionGroup;
import com.rains.transaction.common.netty.bean.TxTransactionItem;
import com.rains.transaction.common.notify.CallbackModel;
import com.rains.transaction.core.compensation.TxCompensationService;
import com.rains.transaction.core.compensation.command.TxCompensationAction;
import com.rains.transaction.core.concurrent.threadlocal.CompensationLocal;
import com.rains.transaction.core.concurrent.threadpool.TransactionThreadPool;
import com.rains.transaction.core.concurrent.threadpool.TxTransactionThreadFactory;
import com.rains.transaction.core.helper.SpringBeanUtils;
import com.rains.transaction.core.service.TxManagerMessageService;
import com.rains.transaction.remote.service.TransactionRecoverService;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

/*
 * 文 件 名:  TxCompensationServiceImpl.java
 * 版    权:  Copyright (c) 2018 com.rains.hugosz
 * 描    述:  补偿事务执行逻辑
 * 创 建 人:  hugosz
 * 创建时间:  2018/11/6
 */
@Service
public class TxCompensationServiceImpl implements TxCompensationService {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TxCompensationServiceImpl.class);
    private static BlockingQueue<TxCompensationAction> QUEUE;
    private final CallbackModel callbackModel;
    private final TxManagerMessageService txManagerMessageService;
    private TransactionRecoverService transactionRecoverService;
    private TxConfig txConfig;
    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    public TxCompensationServiceImpl(CallbackModel modelNameService, TxManagerMessageService txManagerMessageService) {
        this.callbackModel = modelNameService;
        this.txManagerMessageService = txManagerMessageService;
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(1,
                TxTransactionThreadFactory.create("CompensationService", true));
    }

    @Override
    public void compensate() {
        scheduledExecutorService
                .scheduleAtFixedRate(() -> {
                    LogUtil.debug(LOGGER, "compensate recover execute delayTime:{}", () -> txConfig.getCompensationRecoverTime());
                    final List<TransactionRecover> transactionRecovers =
                            transactionRecoverService.listAllByDelay(acquireData());
                    if (Objects.nonNull(transactionRecovers)&&transactionRecovers.size()>0) {
                        for (TransactionRecover transactionRecover : transactionRecovers) {
                            if (transactionRecover.getRetriedCount() > txConfig.getRetryMax()) {
                                LogUtil.error(LOGGER, "此事务超过了最大重试次数，不再进行重试：{}",
                                        () -> transactionRecover.getTransactionInvocation().getTargetClazz().getName()
                                                + ":" + transactionRecover.getTransactionInvocation().getMethod()
                                                + "事务组id：" + transactionRecover.getGroupId());
                                continue;
                            }
                            try {
                                final int update = transactionRecoverService.update(transactionRecover);
                                if (update > 0) {
                                    final TxTransactionGroup byTxGroupId = txManagerMessageService
                                            .findByTxGroupId(transactionRecover.getGroupId());
                                    if (Objects.nonNull(byTxGroupId) && !CollectionUtils.isEmpty(byTxGroupId.getItemList())) {
                                        final Optional<TxTransactionItem> any = byTxGroupId.getItemList().stream()
                                                .filter(item -> Objects.equals(item.getTaskKey(), transactionRecover.getGroupId()))
                                                .findAny();
                                        if (any.isPresent()) {
                                            final int status = any.get().getStatus();
                                            //如果整个事务组状态是提交的
                                            if (TransactionStatusEnum.COMMIT.getCode() == status) {
                                                final Optional<TxTransactionItem> txTransactionItem = byTxGroupId.getItemList().stream()
                                                        .filter(item -> Objects.equals(item.getTaskKey(), transactionRecover.getTaskId()))
                                                        .findAny();
                                                if (txTransactionItem.isPresent()) {
                                                    final TxTransactionItem item = txTransactionItem.get();
                                                    //自己的状态不是提交，那么就进行补偿
                                                    if (item.getStatus() != TransactionStatusEnum.COMMIT.getCode()) {
                                                        submit(buildCompensate(transactionRecover));
                                                    }
                                                }
                                            } else {
                                                //不需要进行补偿，就删除
                                                submit(buildDel(transactionRecover));
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                LogUtil.error(LOGGER, "执行事务补偿异常:{}", e::getMessage);
                            }

                        }
                    }
                }, 30, txConfig.getCompensationRecoverTime(), TimeUnit.SECONDS);

    }


    /**
     * 启动本地补偿事务，根据配置是否进行补偿
     */
    @Override
    public void start(TxConfig txConfig) throws Exception {
        this.txConfig = txConfig;
        if (txConfig.getCompensation()) {
            final String modelName = callbackModel.getModelName();
            transactionRecoverService = SpringBeanUtils.getInstance().getBean(TransactionRecoverService.class);
            transactionRecoverService.init(modelName, txConfig);
            initCompensatePool();//初始化补偿操作的线程池
            compensate();//执行定时补偿
        }
    }

    public void initCompensatePool() {
        synchronized (LOGGER) {
            QUEUE = new LinkedBlockingQueue<>(txConfig.getCompensationQueueMax());
            final int compensationThreadMax = txConfig.getCompensationThreadMax();
            final TransactionThreadPool threadPool = SpringBeanUtils.getInstance().getBean(TransactionThreadPool.class);
            final ExecutorService executorService = threadPool.newCustomFixedThreadPool(compensationThreadMax);
            LogUtil.info(LOGGER, "启动补偿操作线程数量为:{}", () -> compensationThreadMax);
            for (int i = 0; i < compensationThreadMax; i++) {
                executorService.execute(new Worker());
            }

        }
    }

    /**
     * 保存补偿事务信息
     *
     * @param transactionRecover 实体对象
     * @return 主键id
     */
    @Override
    public String save(TransactionRecover transactionRecover) {
        final int rows = transactionRecoverService.create(transactionRecover);
        if (rows > 0) {
            return transactionRecover.getId();
        }
        return null;

    }

    /**
     * 删除补偿事务信息
     *
     * @param id 主键id
     * @return true成功 false 失败
     */
    @Override
    public boolean remove(String id) {
        final int rows = transactionRecoverService.remove(id);
        return rows > 0;
    }

    /**
     * 更新
     *
     * @param transactionRecover 实体对象
     */
    @Override
    public void update(TransactionRecover transactionRecover) {
        transactionRecoverService.update(transactionRecover);
    }

    /**
     * 提交补偿操作
     *
     * @param txCompensationAction 补偿命令
     */
    @Override
    public Boolean submit(TxCompensationAction txCompensationAction) {
        try {
            if (txConfig.getCompensation()) {
                QUEUE.put(txCompensationAction);
            }
        } catch (InterruptedException e) {
            LogUtil.error(LOGGER, "补偿命令提交队列失败：{}", e::getMessage);
            LOGGER.error(e.getMessage(),e);
            return false;

        }
        return true;
    }

    /**
     * 执行补偿
     *
     * @param transactionRecover 补偿信息
     */
    @SuppressWarnings("unchecked")
    private void compensatoryTransfer(TransactionRecover transactionRecover) {
        if (Objects.nonNull(transactionRecover)) {
            final TransactionInvocation transactionInvocation = transactionRecover.getTransactionInvocation();
            if (Objects.nonNull(transactionInvocation)) {
                final Class clazz = transactionInvocation.getTargetClazz();
                final String method = transactionInvocation.getMethod();
                final Object[] argumentValues = transactionInvocation.getArgumentValues();
                final Class[] argumentTypes = transactionInvocation.getArgumentTypes();
                final Object bean = SpringBeanUtils.getInstance().getBean(clazz);
                try {
                    CompensationLocal.getInstance().setCompensationId(CommonConstant.COMPENSATE_ID);
                    MethodUtils.invokeMethod(bean, method, argumentValues, argumentTypes);
                    //通知tm自身已经完成提交 //删除本地信息
                    final Boolean success = txManagerMessageService.completeCommitTxTransaction(transactionRecover.getGroupId(),
                            transactionRecover.getTaskId(), TransactionStatusEnum.COMMIT.getCode());
                    if (success) {
                        transactionRecoverService.remove(transactionRecover.getId());
                    }

                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    LogUtil.error(LOGGER, "补偿方法反射调用失败！{}", e::getMessage);
                }

            }
        }

    }

    private TxCompensationAction buildCompensate(TransactionRecover transactionRecover) {
        TxCompensationAction compensationAction = new TxCompensationAction();
        compensationAction.setCompensationActionEnum(CompensationActionEnum.COMPENSATE);
        compensationAction.setTransactionRecover(transactionRecover);
        return compensationAction;
    }

    private TxCompensationAction buildDel(TransactionRecover transactionRecover) {
        TxCompensationAction compensationAction = new TxCompensationAction();
        compensationAction.setCompensationActionEnum(CompensationActionEnum.DELETE);
        compensationAction.setTransactionRecover(transactionRecover);
        return compensationAction;
    }

    private Date acquireData() {
        return new Date(LocalDateTime.now()
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                (txConfig.getRecoverDelayTime() * 1000));

    }

    /**
     * 线程执行器
     */
    class Worker implements Runnable {

        @Override
        public void run() {
            execute();
        }

        /**
         * 事务执行..
         */
        private void execute() {
            while (true) {
                try {
                    //得到需要回滚的事务对象
                    TxCompensationAction transaction = QUEUE.take();
                    if (transaction != null) {
                        final int code = transaction.getCompensationActionEnum().getCode();
                        if (CompensationActionEnum.SAVE.getCode() == code) {
                            save(transaction.getTransactionRecover());
                        } else if (CompensationActionEnum.DELETE.getCode() == code) {
                            remove(transaction.getTransactionRecover().getId());
                        } else if (CompensationActionEnum.UPDATE.getCode() == code) {
                            update(transaction.getTransactionRecover());
                        } else if (CompensationActionEnum.COMPENSATE.getCode() == code) {
                            compensatoryTransfer(transaction.getTransactionRecover());
                        }
                    }
                } catch (Exception e) {
                    LogUtil.error(LOGGER, "执行补偿命令失败：{}", e::getMessage);
                    LOGGER.error("错误堆栈",e);

                }
            }

        }
    }

}
