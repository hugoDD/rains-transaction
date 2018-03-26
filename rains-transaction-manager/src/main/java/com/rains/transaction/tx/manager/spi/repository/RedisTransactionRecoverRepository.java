
package com.rains.transaction.tx.manager.spi.repository;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.rains.transaction.common.bean.TransactionRecover;
import com.rains.transaction.common.config.TxConfig;
import com.rains.transaction.common.config.TxRedisConfig;
import com.rains.transaction.common.enums.CompensationCacheTypeEnum;
import com.rains.transaction.common.exception.TransactionIoException;
import com.rains.transaction.common.exception.TransactionRuntimeException;
import com.rains.transaction.common.holder.LogUtil;
import com.rains.transaction.common.holder.RedisKeyUtils;
import com.rains.transaction.common.holder.RepositoryPathUtils;
import com.rains.transaction.common.holder.TransactionRecoverUtils;
import com.rains.transaction.common.jedis.JedisClient;
import com.rains.transaction.common.jedis.JedisClientCluster;
import com.rains.transaction.common.jedis.JedisClientSingle;
import com.rains.transaction.common.serializer.ObjectSerializer;
import com.rains.transaction.tx.manager.spi.TransactionRecoverRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * 文 件 名:  RedisTransactionRecoverRepository
 * 版    权:  Copyright (c) 2018 com.rains.hugosz
 * 描    述:  补偿事务redis存储(默认)
 * 创 建 人:  hugosz
 * 创建时间:  2018/3/26  16:50
 */
public class RedisTransactionRecoverRepository implements TransactionRecoverRepository {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisTransactionRecoverRepository.class);


    private ObjectSerializer objectSerializer;


    private String keyName;

    private JedisClient jedisClient;


    /**
     * 创建本地事务对象
     *
     * @param transactionRecover 事务对象
     * @return rows
     */
    @Override
    public int create(TransactionRecover transactionRecover) {
        try {
            final String redisKey = buildRecoverKey(keyName, transactionRecover.getId());
            jedisClient.set(redisKey, TransactionRecoverUtils.convert(transactionRecover, objectSerializer));
            return 1;
        } catch (Exception e) {
            throw new TransactionIoException(e);
        }
    }

    /**
     * 删除对象
     *
     * @param id 事务对象id
     * @return rows
     */
    @Override
    public int remove(String id) {
        try {
            final String redisKey = buildRecoverKey(keyName, id);
            return jedisClient.del(redisKey).intValue();
        } catch (Exception e) {
            throw new TransactionIoException(e);
        }
    }

    /**
     * 更新数据
     *
     * @param transactionRecover 事务对象
     * @return rows 1 成功 0 失败 失败需要抛异常
     */
    @Override
    public int update(TransactionRecover transactionRecover) throws TransactionRuntimeException {
        try {
            final String redisKey = buildRecoverKey(keyName, transactionRecover.getId());
            transactionRecover.setVersion(transactionRecover.getVersion() + 1);
            transactionRecover.setLastTime(new Date());
            transactionRecover.setRetriedCount(transactionRecover.getRetriedCount() + 1);
            final String result = jedisClient.set(redisKey, TransactionRecoverUtils.convert(transactionRecover, objectSerializer));
            return 1;
        } catch (Exception e) {
            throw new TransactionRuntimeException(e);
        }
    }


    /**
     * 根据id获取对象
     *
     * @param id 主键id
     * @return TransactionRecover
     */
    @Override
    public TransactionRecover findById(String id) {
        try {
            final String redisKey = buildRecoverKey(keyName, id);

            byte[] contents = jedisClient.get(redisKey.getBytes());

            return TransactionRecoverUtils.transformBean(contents, objectSerializer);
        } catch (Exception e) {
            throw new TransactionIoException(e);
        }
    }

    /**
     * 获取需要提交的事务
     *
     * @return List<TransactionRecover>
     */
    @Override
    public List<TransactionRecover> listAll() {
        try {
            List<TransactionRecover> transactions = Lists.newArrayList();
            Set<byte[]> keys = jedisClient.keys((keyName + "*").getBytes());
            for (final byte[] key : keys) {
                byte[] contents = jedisClient.get(key);
                if (contents != null) {
                    transactions.add(TransactionRecoverUtils.transformBean(contents, objectSerializer));
                }
            }
            return transactions;
        } catch (Exception e) {
            throw new TransactionIoException(e);
        }
    }

    /**
     * 获取延迟多长时间后的事务信息,只要为了防止并发的时候，刚新增的数据被执行
     *
     * @param date 延迟后的时间
     * @return List<TransactionRecover>
     */
    @Override
    public List<TransactionRecover> listAllByDelay(Date date) {
        final List<TransactionRecover> tccTransactions = listAll();
        return tccTransactions
                .stream()
                .filter(transactionRecover -> transactionRecover.getLastTime().compareTo(date) > 0)
                .collect(Collectors.toList());
    }

    /**
     * 初始化操作
     *
     * @param modelName 模块名称
     * @param txConfig  配置信息
     */
    @Override
    public void init(String modelName, TxConfig txConfig) {
        keyName = RepositoryPathUtils.buildRedisKey(modelName);
        final TxRedisConfig txRedisConfig = txConfig.getTxRedisConfig();
        try {
            buildJedisClient(txRedisConfig);
        } catch (Exception e) {
            LogUtil.error(LOGGER, "redis 初始化异常！请检查配置信息:{}", e::getMessage);
        }
    }


    /**
     * 设置scheme
     *
     * @return scheme 命名
     */
    @Override
    public String getScheme() {
        return CompensationCacheTypeEnum.REDIS.getCompensationCacheType();
    }

    /**
     * 设置序列化信息
     *
     * @param objectSerializer 序列化实现
     */
    @Override
    public void setSerializer(ObjectSerializer objectSerializer) {
        this.objectSerializer = objectSerializer;
    }

    private void buildJedisClient(TxRedisConfig txRedisConfig) {
        JedisPool jedisPool;
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(txRedisConfig.getMaxIdle());
        //最小空闲连接数, 默认0
        config.setMinIdle(txRedisConfig.getMinIdle());
        //最大连接数, 默认8个
        config.setMaxTotal(txRedisConfig.getMaxTotal());
        //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        config.setMaxWaitMillis(txRedisConfig.getMaxWaitMillis());
        //在获取连接的时候检查有效性, 默认false
        config.setTestOnBorrow(txRedisConfig.getTestOnBorrow());
        //返回一个jedis实例给连接池时，是否检查连接可用性（ping()）
        config.setTestOnReturn(txRedisConfig.getTestOnReturn());
        //在空闲时检查有效性, 默认false
        config.setTestWhileIdle(txRedisConfig.getTestWhileIdle());
        //逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
        config.setMinEvictableIdleTimeMillis(txRedisConfig.getMinEvictableIdleTimeMillis());
        //对象空闲多久后逐出, 当空闲时间>该值 ，且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断  (默认逐出策略)，默认30m
        config.setSoftMinEvictableIdleTimeMillis(txRedisConfig.getSoftMinEvictableIdleTimeMillis());
        //逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
        config.setTimeBetweenEvictionRunsMillis(txRedisConfig.getTimeBetweenEvictionRunsMillis());
        //每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
        config.setNumTestsPerEvictionRun(txRedisConfig.getNumTestsPerEvictionRun());

        //如果是集群模式
        if (txRedisConfig.getCluster()) {
            final String clusterUrl = txRedisConfig.getClusterUrl();
            final Set<HostAndPort> hostAndPorts = Splitter.on(clusterUrl)
                    .splitToList(";").stream()
                    .map(HostAndPort::parseString).collect(Collectors.toSet());
            JedisCluster jedisCluster = new JedisCluster(hostAndPorts, config);
            jedisClient = new JedisClientCluster(jedisCluster);
        } else {
            if (StringUtils.isNoneBlank(txRedisConfig.getPassword())) {
                jedisPool = new JedisPool(config, txRedisConfig.getHostName(), txRedisConfig.getPort(), txRedisConfig.getTimeOut(), txRedisConfig.getPassword());
            } else {
                jedisPool = new JedisPool(config, txRedisConfig.getHostName(), txRedisConfig.getPort(), txRedisConfig.getTimeOut());
            }
            jedisClient = new JedisClientSingle(jedisPool);
        }

    }

    private  String buildRecoverKey(String keyPrefix, String id) {
        return String.join(":", keyPrefix, id);
    }

}
