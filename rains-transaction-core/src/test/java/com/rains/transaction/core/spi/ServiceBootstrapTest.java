package com.rains.transaction.core.spi;

import com.rains.transaction.common.enums.CompensationCacheTypeEnum;
import com.rains.transaction.common.enums.SerializeProtocolEnum;
import com.rains.transaction.common.holder.ServiceBootstrap;
import com.rains.transaction.common.serializer.ObjectSerializer;
import com.rains.transaction.remote.service.TransactionRecoverService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

public class ServiceBootstrapTest {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBootstrapTest.class);


    @Test
    public void loadFirst()  {
        final ObjectSerializer objectSerializer = ServiceBootstrap.loadFirst(ObjectSerializer.class);
        LOGGER.info("加载的序列化名称为：{}", objectSerializer.getClass().getName());

    }


    @Test
    public void loadAll() {
        //spi  serialize
        final SerializeProtocolEnum serializeProtocolEnum =
                SerializeProtocolEnum.HESSIAN;
        final ServiceLoader<ObjectSerializer> objectSerializers = ServiceBootstrap.loadAll(ObjectSerializer.class);

        final Optional<ObjectSerializer> serializer = StreamSupport.stream(objectSerializers.spliterator(), false)
                .filter(objectSerializer ->
                        Objects.equals(objectSerializer.getScheme(), serializeProtocolEnum.getSerializeProtocol())).findFirst();

        serializer.ifPresent(objectSerializer -> LOGGER.info("加载的序列化名称为：{}", objectSerializer.getClass().getName()));


        //spi  RecoverRepository support
        final CompensationCacheTypeEnum compensationCacheTypeEnum = CompensationCacheTypeEnum.DB;
        final ServiceLoader<TransactionRecoverService> recoverRepositories = ServiceBootstrap.loadAll(TransactionRecoverService.class);


        final Optional<TransactionRecoverService> repositoryOptional = StreamSupport.stream(recoverRepositories.spliterator(), false)
                .filter(recoverRepository ->
                        Objects.equals(recoverRepository.getScheme(), compensationCacheTypeEnum.getCompensationCacheType())).findFirst();
        //将compensationCache实现注入到spring容器
//        repositoryOptional.ifPresent(repository -> {
//            serializer.ifPresent(repository::setSerializer);
//            SpringBeanUtils.getInstance().registerBean(TransactionRecoverService.class.getName(), repository);
//        });


    }

}