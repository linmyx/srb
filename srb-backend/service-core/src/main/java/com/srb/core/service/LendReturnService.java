package com.srb.core.service;

import com.srb.common.result.Result;
import com.srb.core.pojo.entity.LendReturn;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 还款记录表 服务类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
public interface LendReturnService extends IService<LendReturn> {

    Result selectByLendId(Long lendId);

    String commitReturn(Long lendReturnId, Long userId);

    void notify(Map<String, Object> paramMap);
}
