//package com.happylifeplat.transaction.tx.manager.netty.impl;
//
//import com.happylifeplat.transaction.common.enums.NettyMessageActionEnum;
//import com.happylifeplat.transaction.common.enums.NettyResultEnum;
//import com.happylifeplat.transaction.common.enums.TransactionStatusEnum;
//import com.happylifeplat.transaction.common.holder.IdWorkerUtils;
//import com.happylifeplat.transaction.common.netty.bean.HeartBeat;
//import com.happylifeplat.transaction.common.netty.bean.TxTransactionGroup;
//import com.happylifeplat.transaction.common.netty.bean.TxTransactionItem;
//import com.happylifeplat.transaction.common.netty.serizlize.kryo.KryoPoolFactory;
//import com.happylifeplat.transaction.common.netty.serizlize.kryo.KryoSerialize;
//import com.happylifeplat.transaction.tx.manager.netty.handler.NettyServerMessageHandler;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//import io.netty.channel.embedded.EmbeddedChannel;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import javax.annotation.Resource;
//import javax.validation.constraints.AssertTrue;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.Assert.*;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//public class NettyServerServiceImplTest {
//
//    @Resource
//    private NettyServerMessageHandler nettyServerMessageHandler;
//
//    @Test
//    public void nettyServiceHeart() throws IOException {
//
//        //(1) 创建一个 ByteBuf，并且写入 9 个负整数
//        ByteBuf buf = Unpooled.buffer();
//        //(2) 创建一个EmbeddedChannel，并安装一个要测试的 AbsIntegerEncoder
//        EmbeddedChannel channel = new EmbeddedChannel(nettyServerMessageHandler);
//
//        HeartBeat heartBeat = createHeart();
//        channel.writeInbound(heartBeat);
//        //assertTrue(isWrite);
//        assertTrue(channel.finish());
//        Object msg = channel.readOutbound();
//        assertTrue(msg instanceof HeartBeat);
//        assertEquals(heartBeat,msg);
//
//
//
//
//    }
//
//    @Test
//    public void nettyServiceCreateGroup() throws IOException {
//
//        //(1) 创建一个 ByteBuf，并且写入 9 个负整数
//        ByteBuf buf = Unpooled.buffer();
//        //(2) 创建一个EmbeddedChannel，并安装一个要测试的 AbsIntegerEncoder
//        EmbeddedChannel channel = new EmbeddedChannel(nettyServerMessageHandler);
//
//        HeartBeat heartBeat = createHeart();
//        heartBeat.setAction(NettyMessageActionEnum.CREATE_GROUP.getCode());
//        channel.writeInbound(heartBeat);
//        //assertTrue(isWrite);
//        assertTrue(channel.finish());
//        Object msg = channel.readOutbound();
//        assertTrue(msg instanceof HeartBeat);
//        HeartBeat msgHeart = (HeartBeat) msg;
//        assertEquals(msgHeart.getAction(),NettyMessageActionEnum.RECEIVE.getCode());
//        assertEquals(msgHeart.getResult(), NettyResultEnum.SUCCESS.getCode());
//
//
//
//
//    }
//
//
//    private HeartBeat createHeart() {
//        String groupId = IdWorkerUtils.getInstance().createGroupId();
//        //创建事务组信息
//        TxTransactionGroup txTransactionGroup = new TxTransactionGroup();
//        txTransactionGroup.setId(groupId);
//        List<TxTransactionItem> items = new ArrayList<>(2);
//        //tmManager 用redis hash 结构来存储 整个事务组的状态做为hash结构的第一条数据
//        TxTransactionItem groupItem = new TxTransactionItem();
//        //整个事务组状态为开始
//        groupItem.setStatus(TransactionStatusEnum.BEGIN.getCode());
//        //设置事务id为组的id  即为 hashKey
//        groupItem.setTransId(groupId);
//        groupItem.setTaskKey(groupId);
//        items.add(groupItem);
//        TxTransactionItem item = new TxTransactionItem();
//        item.setTaskKey(IdWorkerUtils.getInstance().createTaskKey());
//        item.setTransId(IdWorkerUtils.getInstance().createUUID());
//        item.setStatus(TransactionStatusEnum.BEGIN.getCode());
//        items.add(item);
//        txTransactionGroup.setItemList(items);
//
//
//        HeartBeat heartBeat = new HeartBeat();
//        heartBeat.setAction(NettyMessageActionEnum.HEART.getCode());
//        heartBeat.setTxTransactionGroup(txTransactionGroup);
//
//        return heartBeat;
//    }
//
//    private byte[] buildHeart() throws IOException {
//        KryoSerialize kryoSerialization = new KryoSerialize(KryoPoolFactory.getKryoPoolInstance());
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//
//        HeartBeat heartBeat = createHeart();
//        kryoSerialization.serialize(byteArrayOutputStream, heartBeat);
//
//
//        byte[] body = byteArrayOutputStream.toByteArray();
//
//        return body;
//    }
//
//}