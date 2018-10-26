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
package com.rains.transaction.core.bootstrap;

import com.rains.transaction.core.helper.SpringBeanUtils;
import com.rains.transaction.common.config.TxConfig;
import com.rains.transaction.common.exception.TransactionRuntimeException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author xiaoyu
 */
@Component
public class TxTransactionBootstrap extends TxConfig implements ApplicationContextAware {


    /**
     * 初始化实体
     */
    private final TxTransactionInitialize txTransactionInitialize;
    private ConfigurableApplicationContext cfgContext;

    @Autowired
    public TxTransactionBootstrap(TxTransactionInitialize txTransactionInitialize) {
        this.txTransactionInitialize = txTransactionInitialize;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        cfgContext = (ConfigurableApplicationContext) applicationContext;
        SpringBeanUtils.getInstance().setCfgContext(cfgContext);
        start(this);
    }


    private void start(TxConfig txConfig) {
        if (!checkDataConfig(txConfig)) {
            throw new TransactionRuntimeException("分布式事务配置信息不完整！");
        }
        txTransactionInitialize.init(txConfig);
    }

    private boolean checkDataConfig(TxConfig txConfig) {
        if(txConfig==null){
            return false;
        }
        if(txConfig.getCompensation()){
            return Objects.nonNull(txConfig.getCompensationCacheType());
        }
        return false;
    }
}




























