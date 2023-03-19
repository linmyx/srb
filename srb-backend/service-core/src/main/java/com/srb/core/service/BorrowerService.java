package com.srb.core.service;

import com.srb.common.result.Result;
import com.srb.core.pojo.entity.Borrower;
import com.baomidou.mybatisplus.extension.service.IService;
import com.srb.core.pojo.vo.BorrowerApprovalVO;
import com.srb.core.pojo.vo.BorrowerVO;

/**
 * <p>
 * 借款人 服务类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
public interface BorrowerService extends IService<Borrower> {

    Result saveBorrowerVoByUserId(BorrowerVO borrowerVO, Long userId);

    Result getBorrowerStatus(Long userId);

    Result getBorrowerList(Long page, Long limit, String keyword);

    Result getBorrowerInfo(Long id);

    Result approval(BorrowerApprovalVO borrowerApprovalVO);
}
