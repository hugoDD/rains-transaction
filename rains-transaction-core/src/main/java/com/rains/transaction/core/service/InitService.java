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
package com.rains.transaction.core.service;

import com.rains.transaction.common.config.TxConfig;

/**
 * @author dourx
 * @version V1.0
 * 创建日期 2018/11/5
 */
public interface InitService {

    /**
     * 框架的初始化
     *
     * @param txConfig 配置信息{@linkplain TxConfig}
     */
    void initialization(TxConfig txConfig);
}
