package com.srb.core.service;

import com.srb.common.result.Result;
import com.srb.core.pojo.entity.BorrowInfo;
import com.srb.core.pojo.entity.Lend;
import com.baomidou.mybatisplus.extension.service.IService;
import com.srb.core.pojo.vo.BorrowInfoApprovalVO;

import java.math.BigDecimal;

/**
 * <p>
 * 标的准备表 服务类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
public interface LendService extends IService<Lend> {

    void created(BorrowInfoApprovalVO borrowInfoApprovalVO, BorrowInfo borrowInfo);

    Result getLendList();

    Result getLendInfoById(Long id);

    BigDecimal getInterestCount(BigDecimal invest, BigDecimal yearRate, Integer totalmonth, Integer returnMethod);

    void makeLoan(Long id);
}
