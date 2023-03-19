package com.srb.core.service;

import com.srb.common.result.Result;
import com.srb.core.pojo.bo.TransFlowBO;
import com.srb.core.pojo.entity.TransFlow;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 交易流水表 服务类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
public interface TransFlowService extends IService<TransFlow> {
    void saveTransFlow(TransFlowBO transFlowBO);

    boolean isSaveTransFlow(String agentBillNo);

    Result selectByUserId(Long userId);
}
