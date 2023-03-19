package com.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.srb.common.result.Result;
import com.srb.core.pojo.bo.TransFlowBO;
import com.srb.core.pojo.entity.TransFlow;
import com.srb.core.mapper.TransFlowMapper;
import com.srb.core.pojo.entity.UserInfo;
import com.srb.core.service.TransFlowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srb.core.service.UserInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 交易流水表 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@Service
public class TransFlowServiceImpl extends ServiceImpl<TransFlowMapper, TransFlow> implements TransFlowService {

    @Resource
    private UserInfoService userInfoService;

    /**
     * 交易流水
     * @param transFlowBO
     */
    @Override
    public void saveTransFlow(TransFlowBO transFlowBO) {
        TransFlow transFlow = new TransFlow();
        transFlow.setTransAmount(transFlowBO.getAmount());
        transFlow.setMemo(transFlowBO.getMemo());
        transFlow.setTransTypeName(transFlowBO.getTransTypeEnum().getTransTypeName());
        transFlow.setTransType(transFlowBO.getTransTypeEnum().getTransType());
        transFlow.setTransNo(transFlowBO.getAgentBillNo());

        QueryWrapper<UserInfo> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("bind_code",transFlowBO.getBindCode());
        UserInfo userInfo = userInfoService.getOne(queryWrapper);
        transFlow.setUserId(userInfo.getId());
        transFlow.setUserName(userInfo.getName());

        save(transFlow);
    }

    /**
     * 根据流水号，查找流水
     * @param agentBillNo
     * @return
     */
    @Override
    public boolean isSaveTransFlow(String agentBillNo) {
        QueryWrapper<TransFlow> queryWrapper = new QueryWrapper();
        queryWrapper.eq("trans_no", agentBillNo);
        int count = count(queryWrapper);
        if (count > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Result selectByUserId(Long userId) {
        QueryWrapper<TransFlow> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", userId).orderByDesc("id");
        List<TransFlow> list = list(queryWrapper);
        return Result.ok().data("row",list);
    }
}
