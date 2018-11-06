package com.rains.transaction.core.recover;

import com.rains.transaction.common.bean.TransactionInvocation;
import com.rains.transaction.common.bean.TransactionRecover;
import com.rains.transaction.common.config.TxConfig;
import com.rains.transaction.common.exception.TransactionException;
import com.rains.transaction.common.exception.TransactionRuntimeException;
import com.rains.transaction.common.serializer.ObjectSerializer;
import com.rains.transaction.core.helper.SpringBeanUtils;
import com.rains.transaction.remote.service.TransactionRecoverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * @author dourx
 * 2018年 11 月  05日  17:13
 * @version V1.0
 * 补偿事务本地存根
 */
public class TransactionRecoverServiceStub implements TransactionRecoverService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionRecoverServiceStub.class);

    private ObjectSerializer objectSerializer;

    private TransactionRecoverService transactionRecoverService;

    public TransactionRecoverServiceStub(TransactionRecoverService transactionRecoverService){
        logger.info("开始初始化 recover 本地存根");
        this.transactionRecoverService =transactionRecoverService;
        objectSerializer = SpringBeanUtils.getInstance().getBean(ObjectSerializer.class);
    }

    @Override
    public int create(TransactionRecover transactionRecover) {
        logger.info("调用 recover 本地存根的创建方法，开始执行Invocation的序列化");
        try {
            final byte[] bytes = objectSerializer.serialize(transactionRecover.getTransactionInvocation());
            transactionRecover.setSerializer(bytes);
            transactionRecover.setTargetClass(transactionRecover.getTransactionInvocation().getTargetClazz().getName());
            return transactionRecoverService.create(transactionRecover);
        }catch (TransactionException e){
            logger.error(e.getMessage(),e);
        }
       return -1;
    }

    @Override
    public int remove(String id) {
        return transactionRecoverService.remove(id);
    }

    @Override
    public int update(TransactionRecover transactionRecover) throws TransactionRuntimeException {
        return transactionRecoverService.update(transactionRecover);
    }

    @Override
    public TransactionRecover findById(String id) {
        TransactionRecover recover= transactionRecoverService.findById(id);
        buildByMap(recover);
        return recover;
    }

    @Override
    public List<TransactionRecover> listAll() {
        List<TransactionRecover> recoverList = transactionRecoverService.listAll();
        for (TransactionRecover recover : recoverList){
            buildByMap(recover);
        }

        return  recoverList;
    }

    @Override
    public List<TransactionRecover> listAllByDelay(Date date) {
        List<TransactionRecover> recoverList = transactionRecoverService.listAllByDelay(date);
        for (TransactionRecover recover : recoverList){
            buildByMap(recover);
        }

        return recoverList;
    }

    @Override
    public void init(String modelName, TxConfig txConfig) throws Exception {
        transactionRecoverService.init(modelName,txConfig);
    }

    @Override
    public String getScheme() {
        return transactionRecoverService.getScheme();
    }



    private TransactionRecover buildByMap(TransactionRecover recover) {
        logger.info("调用 recover 本地存根的查询方法，开始执行Invocation的反序列化");
        try {
            byte[] bytes = recover.getSerializer();
            final TransactionInvocation transactionInvocation = objectSerializer.deSerialize(bytes, TransactionInvocation.class);
            recover.setTransactionInvocation(transactionInvocation);
        } catch (TransactionException e) {
            logger.error(e.getMessage(),e);
        }
        return recover;
    }

}
