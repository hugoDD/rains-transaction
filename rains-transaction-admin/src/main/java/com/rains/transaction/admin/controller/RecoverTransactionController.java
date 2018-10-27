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

package com.rains.transaction.admin.controller;


import com.rains.transaction.admin.annotation.Permission;
import com.rains.transaction.admin.configuration.AdminTxProperties;
import com.rains.transaction.admin.dto.RecoverDTO;
import com.rains.transaction.admin.page.CommonPager;
import com.rains.transaction.admin.query.RecoverTransactionQuery;
import com.rains.transaction.admin.service.RecoverTransactionService;
import com.rains.transaction.admin.vo.TransactionRecoverVO;
import com.rains.transaction.common.holder.httpclient.AjaxResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>Description: .</p>
 * 事务恢复controller
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/10/18 10:31
 * @since JDK 1.8
 */
@RestController
@RequestMapping("/recover")
public class RecoverTransactionController {


    private final RecoverTransactionService recoverTransactionService;



    private AdminTxProperties adminTxProperties;

    private Integer recoverRetryMax;

    @Autowired
    public RecoverTransactionController(RecoverTransactionService recoverTransactionService,  AdminTxProperties properties) {
        this.recoverTransactionService = recoverTransactionService;
        this.recoverRetryMax = properties.getRecover().getRetry();
        this.adminTxProperties = properties;
    }

    @Permission
    @PostMapping(value = "/listPage")
    public AjaxResponse listPage(@RequestBody RecoverTransactionQuery recoverQuery) {
        final CommonPager<TransactionRecoverVO> pager =
                recoverTransactionService.listByPage(recoverQuery);
        return AjaxResponse.success(pager);
    }


    @PostMapping(value = "/batchRemove")
    @Permission
    public AjaxResponse batchRemove(@RequestBody RecoverDTO recoverDTO) {

        final Boolean success = recoverTransactionService.batchRemove(recoverDTO.getIds(), recoverDTO.getApplicationName());
        return AjaxResponse.success(success);

    }

    @PostMapping(value = "/update")
    @Permission
    public AjaxResponse update(@RequestBody RecoverDTO recoverDTO) {
        if (recoverRetryMax < recoverDTO.getRetry()) {
            return AjaxResponse.error("重试次数超过最大设置，请您重新设置！");
        }
        final Boolean success = recoverTransactionService.updateRetry(recoverDTO.getId(),
                recoverDTO.getRetry(), recoverDTO.getApplicationName());
        return AjaxResponse.success(success);

    }

    @PostMapping(value = "/listAppName")
    @Permission
    public AjaxResponse listAppName() {
        final List<String> list = adminTxProperties.getRecover().getApplication();
        return AjaxResponse.success(list);
    }


}
