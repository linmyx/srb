package com.srb.core.service;

import com.srb.core.pojo.entity.BorrowerAttach;
import com.baomidou.mybatisplus.extension.service.IService;
import com.srb.core.pojo.vo.BorrowerAttachVO;

import java.util.List;

/**
 * <p>
 * 借款人上传资源表 服务类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
public interface BorrowerAttachService extends IService<BorrowerAttach> {

    List<BorrowerAttachVO> selectBorrowerAttachVOList(Long id);
}
