package com.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.srb.core.pojo.entity.BorrowerAttach;
import com.srb.core.mapper.BorrowerAttachMapper;
import com.srb.core.pojo.vo.BorrowerAttachVO;
import com.srb.core.service.BorrowerAttachService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 借款人上传资源表 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@Service
public class BorrowerAttachServiceImpl extends ServiceImpl<BorrowerAttachMapper, BorrowerAttach> implements BorrowerAttachService {

    @Override
    public List<BorrowerAttachVO> selectBorrowerAttachVOList(Long id) {
        QueryWrapper<BorrowerAttach> queryWrapper = new QueryWrapper();
        queryWrapper.eq("borrower_id",id);
        List<BorrowerAttach> list = list(queryWrapper);
        List<BorrowerAttachVO> borrowerAttachVOList=new ArrayList<>();
        list.forEach(borrowerAttach -> {
            BorrowerAttachVO borrowerAttachVO = new BorrowerAttachVO();
            borrowerAttachVO.setImageType(borrowerAttach.getImageType());
            borrowerAttachVO.setImageUrl(borrowerAttach.getImageUrl());
            borrowerAttachVOList.add(borrowerAttachVO);
        });
        return borrowerAttachVOList;
    }
}
