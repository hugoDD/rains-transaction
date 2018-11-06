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
package com.rains.transaction.tx.manager.spi.repository;

import com.google.common.collect.Lists;
import com.rains.transaction.common.bean.TransactionRecover;
import com.rains.transaction.common.config.TxConfig;
import com.rains.transaction.common.enums.CompensationCacheTypeEnum;
import com.rains.transaction.common.exception.TransactionRuntimeException;
import com.rains.transaction.common.holder.RepositoryPathUtils;
import com.rains.transaction.common.holder.TransactionRecoverUtils;
import com.rains.transaction.common.serializer.ObjectSerializer;
import com.rains.transaction.tx.manager.spi.TransactionRecoverRepository;
import org.springframework.stereotype.Repository;


import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/*
 * 文 件 名:  FileTransactionRecoverRepository
 * 版    权:  Copyright (c) 2018 com.rains.hugosz
 * 描    述:  本地补偿事务文件存储
 * 创 建 人:  hugosz
 * 创建时间:  2018/3/26  16:40
 */
public class FileTransactionRecoverRepository implements TransactionRecoverRepository {


    private volatile static boolean initialized;
    private String filePath;
    private ObjectSerializer serializer;



    /**
     * 创建本地事务对象
     *
     * @param transactionRecover 事务对象
     * @return rows
     */
    @Override
    public int create(TransactionRecover transactionRecover) {
        writeFile(transactionRecover);
        return 1;
    }

    /**
     * 删除对象
     *
     * @param id 事务对象id
     * @return rows
     */
    @Override
    public int remove(String id) {
        String fullFileName = getFullFileName(id);
        File file = new File(fullFileName);
        if (file.exists()) {
            file.delete();
        }
        return 1;
    }

    /**
     * 更新数据
     *
     * @param transactionRecover 事务对象
     * @return rows 1 成功 0 失败 失败需要抛异常
     */
    @Override
    public int update(TransactionRecover transactionRecover) throws TransactionRuntimeException {
        transactionRecover.setLastTime(new Date());
        transactionRecover.setVersion(transactionRecover.getVersion() + 1);
        transactionRecover.setRetriedCount(transactionRecover.getRetriedCount() + 1);
        try {
            writeFile(transactionRecover);
        } catch (Exception e) {
            throw new TransactionRuntimeException("更新数据异常！");
        }
        return 1;
    }


    /**
     * 根据id获取对象
     *
     * @param id 主键id
     * @return TransactionRecover
     */
    @Override
    public TransactionRecover findById(String id) {
        String fullFileName = getFullFileName(id);
        File file = new File(fullFileName);

        try {
            return readTransaction(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取需要提交的事务
     *
     * @return List<TransactionRecover>
     */
    @Override
    public List<TransactionRecover> listAll() {
        List<TransactionRecover> transactionRecoverList = Lists.newArrayList();
        File path = new File(filePath);
        File[] files = path.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                try {
                    transactionRecoverList.add(readTransaction(file));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        return transactionRecoverList;
    }

    /**
     * 获取延迟多长时间后的事务信息,只要为了防止并发的时候，刚新增的数据被执行
     *
     * @param date 延迟后的时间
     * @return List<TransactionRecover>
     */
    @Override
    public List<TransactionRecover> listAllByDelay(Date date) {
        final List<TransactionRecover> transactionRecovers = listAll();
        return transactionRecovers.stream()
                .filter(recover -> recover.getLastTime().compareTo(date) < 0)
                .collect(Collectors.toList());
    }


    @Override
    public void init(String modelName, TxConfig txConfig) {
        filePath = RepositoryPathUtils.buildFilePath(modelName);
        File file = new File(filePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.mkdirs();
        }
    }


    /**
     * 设置scheme
     *
     * @return scheme 命名
     */
    @Override
    public String getScheme() {
        return CompensationCacheTypeEnum.FILE.getCompensationCacheType();
    }

    private void writeFile(TransactionRecover transaction) {
        makeDirIfNecessory();

        String file = getFullFileName(transaction.getId());

        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "rw");
            try (FileChannel channel = raf.getChannel()) {
                byte[] content = TransactionRecoverUtils.convert(transaction, serializer);
                ByteBuffer buffer = ByteBuffer.allocate(content.length);
                buffer.put(content);
                buffer.flip();
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
                channel.force(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFullFileName(String id) {
        return String.format("%s/%s", filePath, id);
    }

    private TransactionRecover readTransaction(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] content = new byte[(int) file.length()];
            fis.read(content);
            return TransactionRecoverUtils.transformBean(content, serializer);
        }

    }

    private void makeDirIfNecessory() {
        if (!initialized) {
            synchronized (FileTransactionRecoverRepository.class) {
                if (!initialized) {
                    File rootPathFile = new File(filePath);
                    if (!rootPathFile.exists()) {

                        boolean result = rootPathFile.mkdir();

                        if (!result) {
                            throw new TransactionRuntimeException("cannot create root path, the path to create is:" + filePath);
                        }

                        initialized = true;
                    } else if (!rootPathFile.isDirectory()) {
                        throw new TransactionRuntimeException("rootPath is not directory");
                    }
                }
            }
        }
    }

}
