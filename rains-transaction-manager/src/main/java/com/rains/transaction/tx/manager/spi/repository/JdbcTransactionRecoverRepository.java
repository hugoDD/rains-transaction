
package com.rains.transaction.tx.manager.spi.repository;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.collect.Maps;
import com.rains.transaction.common.bean.TransactionInvocation;
import com.rains.transaction.common.bean.TransactionRecover;
import com.rains.transaction.common.config.TxConfig;
import com.rains.transaction.common.config.TxDbConfig;
import com.rains.transaction.common.enums.CompensationCacheTypeEnum;
import com.rains.transaction.common.exception.TransactionException;
import com.rains.transaction.common.exception.TransactionRuntimeException;
import com.rains.transaction.common.holder.RepositoryPathUtils;
import com.rains.transaction.common.serializer.ObjectSerializer;

import com.rains.transaction.tx.manager.spi.TransactionRecoverRepository;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

/*
 * 文 件 名:  JdbcTransactionRecoverRepository
 * 版    权:  Copyright (c) 2018 com.rains.hugosz
 * 描    述:  补偿事务DB存储
 * 创 建 人:  hugosz
 * 创建时间:  2018/3/26  16:41
 */
public class JdbcTransactionRecoverRepository extends AbstractJdbcRecoverRepository implements TransactionRecoverRepository {


    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcTransactionRecoverRepository.class);

    private DruidDataSource dataSource;


    private String tableName;

    private ObjectSerializer serializer;

    @Override
    public void setSerializer(ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public int create(TransactionRecover recover) {
        String sql = "insert into " + tableName + "(id,target_class,target_method,retried_count,create_time,last_time,version,group_id,task_id,invocation)" +
                " values(?,?,?,?,?,?,?,?,?,?)";
        try {
            final TransactionInvocation transactionInvocation = recover.getTransactionInvocation();
            final String className = transactionInvocation.getTargetClazz().getName();
            final String method = transactionInvocation.getMethod();
            final byte[] serialize = serializer.serialize(transactionInvocation);
            return executeUpdate(sql, recover.getId(), className, method, recover.getRetriedCount(), recover.getCreateTime(), recover.getLastTime(),
                    recover.getVersion(), recover.getGroupId(), recover.getTaskId(), serialize);

        } catch (TransactionException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int remove(String id) {
        String sql = "delete from " + tableName + " where id = ? ";
        return executeUpdate(sql, id);
    }

    /**
     * 更新数据
     *
     * @param transactionRecover 事务对象
     * @return rows 1 成功 0 失败 失败需要抛异常
     */
    @Override
    public int update(TransactionRecover transactionRecover) throws TransactionRuntimeException {

        String sql = "update " + tableName +
                " set last_time = ?,version =version+ 1,retried_count =retried_count+1 where id = ? and version=? ";
        int success = executeUpdate(sql, new Date(), transactionRecover.getId(), transactionRecover.getVersion());
        if (success <= 0) {
            throw new TransactionRuntimeException("更新异常，数据已经被更新！");
        }
        return success;
    }


    /**
     * 根据id获取对象
     *
     * @param id 主键id
     * @return TransactionRecover
     */
    @Override
    public TransactionRecover findById(String id) {

        String selectSql = "select * from " + tableName + " where id=?";

        List<Map<String, Object>> list = executeQuery(selectSql);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.stream().filter(Objects::nonNull)
                    .map(this::buildByMap).collect(Collectors.toList()).get(0);
        }

        return null;
    }

    /**
     * 获取需要提交的事务
     *
     * @return List<TransactionRecover>
     */
    @Override
    public List<TransactionRecover> listAll() {
        String selectSql = "select * from " + tableName;
        List<Map<String, Object>> list = executeQuery(selectSql);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.stream().filter(Objects::nonNull)
                    .map(this::buildByMap).collect(Collectors.toList());
        }

        return null;
    }

    /**
     * 获取延迟多长时间后的事务信息,只要为了防止并发的时候，刚新增的数据被执行
     *
     * @param date 延迟后的时间
     * @return List<TransactionRecover>
     */
    @Override
    public List<TransactionRecover> listAllByDelay(Date date) {

        String sb = "select * from " +
                tableName +
                " where last_time <?";

        List<Map<String, Object>> list = executeQuery(sb, date);

        if (CollectionUtils.isNotEmpty(list)) {
            return list.stream().filter(Objects::nonNull)
                    .map(this::buildByMap).collect(Collectors.toList());
        }
        return null;
    }


    private TransactionRecover buildByMap(Map<String, Object> map) {
        TransactionRecover recover = new TransactionRecover();
        recover.setId((String) map.get("id"));
        recover.setRetriedCount((Integer) map.get("retried_count"));
        recover.setCreateTime((Date) map.get("create_time"));
        recover.setLastTime((Date) map.get("last_time"));
        recover.setTaskId((String) map.get("task_id"));
        recover.setGroupId((String) map.get("group_id"));
        recover.setVersion((Integer) map.get("version"));
        byte[] bytes = (byte[]) map.get("invocation");
        try {
            final TransactionInvocation transactionInvocation = serializer.deSerialize(bytes, TransactionInvocation.class);
            recover.setTransactionInvocation(transactionInvocation);
        } catch (TransactionException e) {
            e.printStackTrace();
        }
        return recover;
    }

    /**
     * 初始化操作
     *
     * @param modelName 模块名称
     * @param txConfig  配置信息
     */
    @Override
    public void init(String modelName, TxConfig txConfig) {
        dataSource = new DruidDataSource();
        final TxDbConfig txDbConfig = txConfig.getTxDbConfig();
        dataSource.setUrl(txDbConfig.getUrl());
        dataSource.setDriverClassName(txDbConfig.getDriverClassName());
        dataSource.setUsername(txDbConfig.getUsername());
        dataSource.setPassword(txDbConfig.getPassword());


        dataSource.setInitialSize(txDbConfig.getInitialSize());
        dataSource.setMaxActive(txDbConfig.getMaxActive());
        dataSource.setMinIdle(txDbConfig.getMinIdle());
        dataSource.setMaxWait(txDbConfig.getMaxWait());
        dataSource.setValidationQuery(txDbConfig.getValidationQuery());
        dataSource.setTestOnBorrow(txDbConfig.getTestOnBorrow());
        dataSource.setTestOnReturn(txDbConfig.getTestOnReturn());
        dataSource.setTestWhileIdle(txDbConfig.getTestWhileIdle());
        dataSource.setPoolPreparedStatements(txDbConfig.getPoolPreparedStatements());
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(txDbConfig.getMaxPoolPreparedStatementPerConnectionSize());


        this.tableName = RepositoryPathUtils.buildDbTableName(modelName);
        executeUpdate(SqlHelper.buildCreateTableSql(tableName, txDbConfig.getDriverClassName()));
    }


    /**
     * 设置scheme
     *
     * @return scheme 命名
     */
    @Override
    public String getScheme() {
        return CompensationCacheTypeEnum.DB.getCompensationCacheType();
    }

    private int executeUpdate(String sql, Object... params) {
        try {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql);) {
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        ps.setObject((i + 1), params[i]);
                    }
                }
                return ps.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.error("executeUpdate->" + e.getMessage());
        }
        return 0;
    }

    private List<Map<String, Object>> executeQuery(String sql, Object... params) {
        List<Map<String, Object>> list = null;
        try {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        ps.setObject((i + 1), params[i]);
                    }
                }
                ResultSet rs = ps.executeQuery();
                ResultSetMetaData md = rs.getMetaData();
                int columnCount = md.getColumnCount();
                list = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> rowData = Maps.newHashMap();
                    for (int i = 1; i <= columnCount; i++) {
                        rowData.put(md.getColumnName(i), rs.getObject(i));
                    }
                    list.add(rowData);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("executeQuery->" + e.getMessage());
        }
        return list;
    }
}
