package com.srb.core.service;

import com.srb.common.result.Result;
import com.srb.core.pojo.entity.BorrowInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.srb.core.pojo.vo.BorrowInfoApprovalVO;

/**
 * <p>
 * 借款信息表 服务类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
public interface BorrowInfoService extends IService<BorrowInfo> {

    Result getBorrowAmount(Long userId);

    Result saveBorrowInfo(Long userId, BorrowInfo borrowInfo);

    Result getUserBorrowStatus(Long userId);

    Result getBorrowerInfoList();

    Result getShowInfo(Long id);

    Result approval(BorrowInfoApprovalVO borrowInfoApprovalVO);
}
