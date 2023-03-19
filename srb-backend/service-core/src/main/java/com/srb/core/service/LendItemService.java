package com.srb.core.service;

import com.srb.common.result.Result;
import com.srb.core.pojo.entity.LendItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.srb.core.pojo.vo.InvestVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借记录表 服务类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
public interface LendItemService extends IService<LendItem> {

    Result commitInvest(InvestVO investVO);

    void notify(Map<String, Object> paramMap);

    List<LendItem> getLendItemInfo(Long lendId, Integer status);

    Result selectByLendId(Long lendId);
}
