package com.srb.core.service;

import com.srb.common.result.Result;
import com.srb.core.pojo.entity.UserAccount;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 用户账户 服务类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
public interface UserAccountService extends IService<UserAccount> {

    Result commitCharge(BigDecimal chargeAmt, Long userId);

    String notify(Map<String, Object> map);

    Result getAccount(Long userId);

    Result commitWithdraw(BigDecimal fetchAmt, Long userId);

    void notifyWithdraw(Map<String, Object> paramMap);
}
