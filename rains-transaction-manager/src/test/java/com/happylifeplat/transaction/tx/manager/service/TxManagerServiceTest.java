package com.happylifeplat.transaction.tx.manager.service;

import com.happylifeplat.transaction.common.constant.CommonConstant;
import com.happylifeplat.transaction.common.enums.NettyMessageActionEnum;
import com.happylifeplat.transaction.common.enums.TransactionStatusEnum;
import com.happylifeplat.transaction.common.holder.DateUtils;
import com.happylifeplat.transaction.common.holder.IdWorkerUtils;
import com.happylifeplat.transaction.common.netty.bean.HeartBeat;
import com.happylifeplat.transaction.common.netty.bean.TxTransactionGroup;
import com.happylifeplat.transaction.common.netty.bean.TxTransactionItem;
import com.happylifeplat.transaction.common.netty.serizlize.kryo.KryoPoolFactory;
import com.happylifeplat.transaction.common.netty.serizlize.kryo.KryoSerialize;
import com.happylifeplat.transaction.tx.manager.config.Constant;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * <p>Description: .</p>
 * <p>Company: 深圳市旺生活互联网科技有限公司</p>
 * <p>Copyright: 2015-2017 happylifeplat.com All Rights Reserved</p>
 *
 * @author yu.xiao@happylifeplat.com
 * @version 1.0
 * @date 2017/8/8 14:13
 * @since JDK 1.8
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TxManagerServiceTest {

    private final static String groupId = IdWorkerUtils.getInstance().createGroupId();

    private static final int MAX = 1000;

    @Autowired
    private TxManagerService txManagerService;

    @Autowired
    private RedisTemplate redisTemplate;
    @Test
    public void createTransactionGroup() throws IOException {
        final long start = System.currentTimeMillis();
        for (int i = 0; i < MAX; i++) {
            KryoSerialize kryoSerialization = new KryoSerialize(KryoPoolFactory.getKryoPoolInstance());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            String groupId = IdWorkerUtils.getInstance().createGroupId();
            //创建事务组信息
            TxTransactionGroup txTransactionGroup = new TxTransactionGroup();
            txTransactionGroup.setId(groupId);
            List<TxTransactionItem> items = new ArrayList<>(2);
            //tmManager 用redis hash 结构来存储 整个事务组的状态做为hash结构的第一条数据
            TxTransactionItem groupItem = new TxTransactionItem();
            //整个事务组状态为开始
            groupItem.setStatus(TransactionStatusEnum.BEGIN.getCode());
            //设置事务id为组的id  即为 hashKey
            groupItem.setTransId(groupId);
            groupItem.setTaskKey(groupId);
            items.add(groupItem);
            TxTransactionItem item = new TxTransactionItem();
            item.setTaskKey(IdWorkerUtils.getInstance().createTaskKey());
            item.setTransId(IdWorkerUtils.getInstance().createUUID());
            item.setStatus(TransactionStatusEnum.BEGIN.getCode());
            items.add(item);
            txTransactionGroup.setItemList(items);


            HeartBeat heartBeat = new HeartBeat();
            heartBeat.setAction(NettyMessageActionEnum.HEART.getCode());
            heartBeat.setTxTransactionGroup(txTransactionGroup);

            txManagerService.saveTxTransactionGroup(txTransactionGroup);

            kryoSerialization.serialize(byteArrayOutputStream, heartBeat);


            byte[] body = byteArrayOutputStream.toByteArray();


            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);

            final HeartBeat heartBeat1 = (HeartBeat)
                    kryoSerialization.deserialize(byteArrayInputStream);

            txManagerService.updateTxTransactionItemStatus(groupId, groupItem.getTaskKey(), TransactionStatusEnum.COMMIT.getCode(), item.getMessage());
            txManagerService.updateTxTransactionItemStatus(groupId, item.getTaskKey(), TransactionStatusEnum.COMMIT.getCode(), item.getMessage());

        }
        final long end = System.currentTimeMillis();

        System.out.println((end - start));
    }


    @Test
    public void saveTxTransactionGroup() throws Exception {

        TxTransactionGroup txTransactionGroup = new TxTransactionGroup();
        txTransactionGroup.setId(groupId);


        TxTransactionItem item = new TxTransactionItem();
        item.setStatus(5);
        item.setTaskKey(IdWorkerUtils.getInstance().createTaskKey());

        txTransactionGroup.setItemList(Collections.singletonList(item));

        txManagerService.saveTxTransactionGroup(txTransactionGroup);

        final List<TxTransactionItem> txTransactionItems = txManagerService.listByTxGroupId(groupId);
        Assert.assertNotNull(txTransactionItems);
    }

    @Test
    public void addTxTransaction() throws Exception {
    }

    @Test
    public void listByTxGroupId() throws Exception {
        final List<TxTransactionItem> txTransactionItems = txManagerService.listByTxGroupId(groupId);
        Assert.assertNotNull(txTransactionItems);
    }

    @Test
    public void removeRedisByTxGroupId() throws Exception {
       final Set<String> keys = redisTemplate.keys(Constant.REDIS_KEYS);
       redisTemplate.delete(keys);
        redisTemplate.delete(CommonConstant.REDIS_KEY_SET);
    }

    @Test
    public void updateTxTransactionItemStatus() throws Exception {
    }

    @Test
    public void findTxTransactionGroupStatus() throws Exception {
    }

    @Test
    public void removeCommitTxGroup() throws Exception {
        txManagerService.removeCommitTxGroup();
    }

}