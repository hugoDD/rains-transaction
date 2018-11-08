package com.rains.transaction.tx.dubbo.configuration;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;
import com.rains.transaction.core.interceptor.AbstractTxTransactionAspect;
import com.rains.transaction.core.interceptor.TxTransactionInterceptor;
import com.rains.transaction.core.recover.TransactionRecoverServiceStub;
import com.rains.transaction.core.service.AspectTransactionService;
import com.rains.transaction.core.service.ModelNameService;
import com.rains.transaction.remote.service.TransactionRecoverService;
import com.rains.transaction.remote.service.TxManagerRemoteService;
import com.rains.transaction.tx.dubbo.interceptor.DubboTxTransactionAspect;
import com.rains.transaction.tx.dubbo.interceptor.DubboTxTransactionInterceptor;
import com.rains.transaction.tx.dubbo.service.DubboModelNameServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dourx
 * 2018年 11 月  02日  10:30
 * @version V1.0
 * TODO
 */
@Configuration
@ConditionalOnClass({ReferenceConfig.class,TxManagerRemoteService.class})
public class DubboTransactionAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(DubboTransactionAutoConfiguration.class);

   public DubboTransactionAutoConfiguration(){
       logger.info("Dubbo Transaction AutoConfiguration is starting");
    }

    /**
     * 初始化补偿日志服务
     * @param applicationConfig
     * @param registryConfig
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public TxManagerRemoteService txManagerRemoteService(ApplicationConfig applicationConfig, RegistryConfig registryConfig){
        ReferenceConfig<TxManagerRemoteService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setApplication(applicationConfig);
        referenceConfig.setRegistry(registryConfig);
        referenceConfig.setInterface(TxManagerRemoteService.class);
        referenceConfig.setProtocol("dubbo");
        referenceConfig.setGroup("*");
        referenceConfig.setCluster("failsafe");
        referenceConfig.setTimeout(5000);

        ReferenceConfigCache cache = ReferenceConfigCache.getCache();

        return cache.get(referenceConfig);
    }

    @Bean
    @ConditionalOnMissingBean
    public TransactionRecoverService transactionRecoverService(ApplicationConfig applicationConfig, RegistryConfig registryConfig){
        ReferenceConfig<TransactionRecoverService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setApplication(applicationConfig);
        referenceConfig.setRegistry(registryConfig);
        referenceConfig.setInterface(TransactionRecoverService.class);
        // referenceConfig.setGroup("*");
        referenceConfig.setStub(true);
        referenceConfig.setStub(TransactionRecoverServiceStub.class.getName());
        referenceConfig.setCluster("failsafe");
        referenceConfig.setTimeout(5000);

        ReferenceConfigCache cache = ReferenceConfigCache.getCache();

        return cache.get(referenceConfig);
    }

    @Bean
    public ModelNameService modelNameService(){
        return  new DubboModelNameServiceImpl();
    }

    @Bean
    public TxTransactionInterceptor txTransactionInterceptor(AspectTransactionService aspectTransactionService){
        return new DubboTxTransactionInterceptor(aspectTransactionService);
    }
    @Bean
    public AbstractTxTransactionAspect dubboTxTransactionAspect(DubboTxTransactionInterceptor dubboTxTransactionInterceptor){
        return new DubboTxTransactionAspect(dubboTxTransactionInterceptor);
    }

}
