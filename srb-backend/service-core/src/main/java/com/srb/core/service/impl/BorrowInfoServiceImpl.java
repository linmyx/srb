package com.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.srb.common.exception.Assert;
import com.srb.common.result.ResponseEnum;
import com.srb.common.result.Result;
import com.srb.core.enums.BorrowInfoStatusEnum;
import com.srb.core.enums.BorrowerStatusEnum;
import com.srb.core.enums.UserBindEnum;
import com.srb.core.pojo.entity.*;
import com.srb.core.mapper.BorrowInfoMapper;
import com.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.srb.core.pojo.vo.BorrowInfoVo;
import com.srb.core.pojo.vo.BorrowerDetailVO;
import com.srb.core.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 借款信息表 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@Service
public class BorrowInfoServiceImpl extends ServiceImpl<BorrowInfoMapper, BorrowInfo> implements BorrowInfoService {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private IntegralGradeService integralGradeService;

    @Resource
    private DictService dictService;

    @Resource
    private BorrowerService borrowerService;
    @Resource
    private LendService lendService;

    @Override
    public Result getBorrowAmount(Long userId) {

        //获取用户积分
        UserInfo userInfo = userInfoService.getById(userId);
        Assert.notNull(userInfo, ResponseEnum.LOGIN_AUTH_ERROR);
        Integer integral = userInfo.getIntegral();

        //根据用户积分来判断借款额度
        QueryWrapper<IntegralGrade> queryWrapper=new QueryWrapper<>();
        queryWrapper.le("integral_start", integral);
        queryWrapper.ge("integral_end", integral);
        IntegralGrade integralGrade = integralGradeService.getOne(queryWrapper);
        if(integralGrade == null){
            return Result.ok().data("row",new BigDecimal("0"));
        }
        return Result.ok().data("row",integralGrade.getBorrowAmount());
    }

    /**
     * 保存借款人信息
     * @param userId
     * @param borrowInfo
     * @return
     */
    @Override
    public Result saveBorrowInfo(Long userId, BorrowInfo borrowInfo) {

        //判断借款额度是否正确
        Result borrowAmount = this.getBorrowAmount(userId);
        Map<String, Object> data = borrowAmount.getData();
        BigDecimal maxAmount = (BigDecimal) data.get("row");
        Assert.isTrue(
                borrowInfo.getAmount().doubleValue() <= maxAmount.doubleValue(),
                ResponseEnum.USER_AMOUNT_LESS_ERROR);

        borrowInfo.setUserId(userId);
        //获取用户信息
        UserInfo userInfo = userInfoService.getById(userId);
        //判断用户绑定状态
        Assert.isTrue(
                userInfo.getBindStatus().intValue() == UserBindEnum.BIND_OK.getStatus().intValue(),
                ResponseEnum.USER_NO_BIND_ERROR);
        //判断借款人状态
        Assert.isTrue(userInfo.getBorrowAuthStatus().intValue()== BorrowerStatusEnum.AUTH_OK.getStatus().intValue(),
                ResponseEnum.USER_NO_AMOUNT_ERROR);

        borrowInfo.setBorrowYearRate( borrowInfo.getBorrowYearRate().divide(new BigDecimal(100)));
        borrowInfo.setStatus(BorrowInfoStatusEnum.CHECK_RUN.getStatus());
        boolean save = save(borrowInfo);
        if (!save){
           return Result.error().message("借款申请提交失败,请联系管理员！！");
        }
        return Result.ok().message("借款申请提交成功!!");
    }

    /**
     * 查询借款人信息审核状态
     * @param userId
     * @return
     */
    @Override
    public Result getUserBorrowStatus(Long userId) {
        QueryWrapper<BorrowInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId).select("status");
        List<Object> objects = listObjs(wrapper);
        if (objects.size() == 0) {
            return Result.ok().data("row",BorrowInfoStatusEnum.NO_AUTH.getStatus());
        }
        Integer status = (Integer) objects.get(0);
        return Result.ok().data("row",status);
    }

    /**
     * 获取借款信息列表
     * @return
     */
    @Override
    public Result getBorrowerInfoList() {
        List<BorrowInfoVo> borrowInfoVos=new ArrayList<>();
        List<BorrowInfo> borrowInfoList = list(null);
        Map<String, Object> objectObjectHashMap = new HashMap<>();
        borrowInfoList.forEach(borrowInfo -> {
            BorrowInfoVo borrowInfoVo = new BorrowInfoVo();
            BeanUtils.copyProperties(borrowInfo, borrowInfoVo);
            String returnMethod = dictService.getDictCodeAndValue("returnMethod", borrowInfo.getReturnMethod());
            String moneyUse = dictService.getDictCodeAndValue("moneyUse", borrowInfo.getMoneyUse());
            String msgByStatus = BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus());
            UserInfo userInfo = userInfoService.getById(borrowInfo.getUserId());
            borrowInfoVo.setName(userInfo.getName());
            borrowInfoVo.setMobile(userInfo.getMobile());
            borrowInfoVo.setReturnMethod(returnMethod);
            borrowInfoVo.setMoneyUse(moneyUse);
            objectObjectHashMap.put("status",msgByStatus);
            borrowInfoVo.setParam(objectObjectHashMap);
            borrowInfoVos.add(borrowInfoVo);
        });

        return Result.ok().data("row",borrowInfoVos);
    }

    /**
     * 借款人信息详情
     * @param id
     * @return
     */
    @Override
    public Result getShowInfo(Long id) {
        Map<String,Object> map=new HashMap<>();
        BorrowInfoVo borrowInfoVo = new BorrowInfoVo();
        Map<String, Object> objectObjectHashMap = new HashMap<>();
        //查询借款对象
        BorrowInfo borrowInfo = getById(id);
        BeanUtils.copyProperties(borrowInfo, borrowInfoVo);
        String returnMethod = dictService.getDictCodeAndValue("returnMethod", borrowInfo.getReturnMethod());
        String moneyUse = dictService.getDictCodeAndValue("moneyUse", borrowInfo.getMoneyUse());
        String msgByStatus = BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus());
        borrowInfoVo.setReturnMethod(returnMethod);
        borrowInfoVo.setMoneyUse(moneyUse);
        objectObjectHashMap.put("status",msgByStatus);
        borrowInfoVo.setParam(objectObjectHashMap);
        map.put("borrowInfo",borrowInfoVo);
        //查询借款人对象
        QueryWrapper<Borrower> borrowerQueryWrapper=new QueryWrapper<>();
        borrowerQueryWrapper.eq("user_id",borrowInfo.getUserId());
        Borrower borrower = borrowerService.getOne(borrowerQueryWrapper);
        Result borrowerInfo = borrowerService.getBorrowerInfo(borrower.getId());
        Map<String, Object> data = borrowerInfo.getData();
        BorrowerDetailVO borrowerDetailVO = (BorrowerDetailVO) data.get("row");
        map.put("borrowDetail",borrowerDetailVO);
        return Result.ok().data(map);
    }

    /**
     * 审批借款信息
     * @param borrowInfoApprovalVO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result approval(BorrowInfoApprovalVO borrowInfoApprovalVO) {
        //修改借款信息状态
        Long id = borrowInfoApprovalVO.getId();
        BorrowInfo borrowInfo = getById(id);
        borrowInfo.setStatus(borrowInfoApprovalVO.getStatus());
        updateById(borrowInfo);
        //审批通过的，创建标底
        if (borrowInfoApprovalVO.getStatus().intValue()== BorrowInfoStatusEnum.CHECK_OK.getStatus().intValue()){
            lendService.created(borrowInfoApprovalVO,borrowInfo);
        }
        return Result.ok().message("审核成功!!");
    }
}
