package com.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.srb.common.result.Result;
import com.srb.core.enums.BorrowerStatusEnum;
import com.srb.core.enums.IntegralEnum;
import com.srb.core.pojo.entity.Borrower;
import com.srb.core.mapper.BorrowerMapper;
import com.srb.core.pojo.entity.BorrowerAttach;
import com.srb.core.pojo.entity.UserInfo;
import com.srb.core.pojo.entity.UserIntegral;
import com.srb.core.pojo.vo.BorrowerApprovalVO;
import com.srb.core.pojo.vo.BorrowerAttachVO;
import com.srb.core.pojo.vo.BorrowerDetailVO;
import com.srb.core.pojo.vo.BorrowerVO;
import com.srb.core.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 借款人 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@Service
public class BorrowerServiceImpl extends ServiceImpl<BorrowerMapper, Borrower> implements BorrowerService {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private BorrowerAttachService borrowerAttachService;

    @Resource
    private DictService dictService;

    @Resource
    private UserIntegralService userIntegralService;

    @Transactional
    @Override
    public Result saveBorrowerVoByUserId(BorrowerVO borrowerVO, Long userId) {

        //先保存借款人信息
        Borrower borrower = new Borrower();
        BeanUtils.copyProperties(borrowerVO, borrower);
        borrower.setUserId(userId);
        UserInfo userInfo = userInfoService.getById(userId);
        borrower.setName(userInfo.getName());
        borrower.setIdCard(userInfo.getIdCard());
        borrower.setMobile(userInfo.getMobile());
        borrower.setStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());
        //向数据库保存借款人信息
        save(borrower);

        //保存图片
        List<BorrowerAttach> borrowerAttachList = borrowerVO.getBorrowerAttachList();
        borrowerAttachList.forEach(borrowerAttach -> {
            borrowerAttach.setBorrowerId(borrower.getId());
            borrowerAttachService.save(borrowerAttach);
        });

        //修改会员认证状态
        userInfo.setBorrowAuthStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());
        userInfoService.updateById(userInfo);
        return Result.ok().message("审核提交成功!!");
    }

    @Override
    public Result getBorrowerStatus(Long userId) {

        QueryWrapper<Borrower> queryWrap=new QueryWrapper<>();
        queryWrap.select("status").eq("user_id", userId);
        List<Object> objects = listObjs(queryWrap);

        if (objects.size() == 0) {
            //借款人尚未提交信息
            Integer status = BorrowerStatusEnum.NO_AUTH.getStatus();
            return Result.ok().data("row",status);
        }
        Integer status = (Integer)objects.get(0);
        return Result.ok().data("row",status);
    }


    /**
     * 分页获取借款人列表
     * @param page
     * @param limit
     * @param keyword
     * @return
     */
    @Override
    public Result getBorrowerList(Long page, Long limit, String keyword) {
        Page<Borrower> pageInfo = new Page<Borrower>(page,limit);
        if (StringUtils.isEmpty(keyword)){
            page(pageInfo,null);
            return Result.ok().data("row",pageInfo);
        }
        QueryWrapper<Borrower> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name",keyword).or().like("mobile",keyword)
                .or().like("id_card",keyword)
                .orderByDesc("id");
          page(pageInfo,queryWrapper);
        return Result.ok().data("row",pageInfo);
    }

    /**
     * 根据id获取借款人信息
     * @param id
     * @return
     */
    @Override
    public Result getBorrowerInfo(Long id) {

        BorrowerDetailVO borrowerDetailVO=new BorrowerDetailVO();
        Borrower borrower = getById(id);
        BeanUtils.copyProperties(borrower,borrowerDetailVO);
        //婚否
        borrowerDetailVO.setMarry(borrower.getMarry()?"是":"否");
        //性别
        borrowerDetailVO.setSex(borrower.getSex()==1?"男":"女");

        //计算下拉列表选中内容
        String education = dictService.getDictCodeAndValue("education", borrower.getEducation());
        String industry = dictService.getDictCodeAndValue("moneyUse", borrower.getIndustry());
        String income = dictService.getDictCodeAndValue("income", borrower.getIncome());
        String returnSource = dictService.getDictCodeAndValue("returnSource", borrower.getReturnSource());
        String contactsRelation = dictService.getDictCodeAndValue("relation", borrower.getContactsRelation());

        //下拉列表
        borrowerDetailVO.setEducation(education);
        borrowerDetailVO.setIndustry(industry);
        borrowerDetailVO.setIncome(income);
        borrowerDetailVO.setReturnSource(returnSource);
        borrowerDetailVO.setContactsRelation(contactsRelation);

        //审批状态
        String status = BorrowerStatusEnum.getMsgByStatus(borrower.getStatus());
        borrowerDetailVO.setStatus(status);

        //获取附件VO列表
        List<BorrowerAttachVO> borrowerAttachVOS = borrowerAttachService.selectBorrowerAttachVOList(id);
        borrowerDetailVO.setBorrowerAttachVOList(borrowerAttachVOS);
        return Result.ok().data("row",borrowerDetailVO);
    }

    @Transactional
    @Override
    public Result approval(BorrowerApprovalVO borrowerApprovalVO) {

        //修改借款人信息表
        Borrower borrower = getById(borrowerApprovalVO.getBorrowerId());
        borrower.setStatus(BorrowerStatusEnum.AUTH_OK.getStatus());
        updateById(borrower);
        //获取用户id
        Long userId = borrower.getUserId();
        //用户用户信息
        UserInfo userInfo = userInfoService.getById(userId);
        //修改用户状态
        userInfo.setBorrowAuthStatus(BorrowerStatusEnum.AUTH_OK.getStatus());

        //添加积分获取记录
        UserIntegral userIntegral = new UserIntegral();

        //添加基本信息获取的积分
        userIntegral.setUserId(userId);
        userIntegral.setIntegral(borrowerApprovalVO.getInfoIntegral());
        userIntegral.setContent("借款人基本信息");
        userIntegralService.save(userIntegral);

        //计算总积分
        int curIntegral = userInfo.getIntegral() + borrowerApprovalVO.getInfoIntegral();
        //计算身份认证通过获取的积分
        if (borrowerApprovalVO.getIsIdCardOk()){
            curIntegral+= IntegralEnum.BORROWER_IDCARD.getIntegral();
            userIntegral = new UserIntegral();
            userIntegral.setIntegral(IntegralEnum.BORROWER_IDCARD.getIntegral());
            userIntegral.setUserId(userId);
            userIntegral.setContent(IntegralEnum.BORROWER_IDCARD.getMsg());
            userIntegralService.save(userIntegral);
        }
        //计算房子认证通过获取的积分
        if (borrowerApprovalVO.getIsHouseOk()){
            curIntegral+= IntegralEnum.BORROWER_HOUSE.getIntegral();
            userIntegral = new UserIntegral();
            userIntegral.setIntegral(IntegralEnum.BORROWER_HOUSE.getIntegral());
            userIntegral.setUserId(userId);
            userIntegral.setContent(IntegralEnum.BORROWER_HOUSE.getMsg());
            userIntegralService.save(userIntegral);
        }
        //计算车子认证通过获取的积分
        if (borrowerApprovalVO.getIsCarOk()){
            curIntegral+= IntegralEnum.BORROWER_CAR.getIntegral();
            userIntegral = new UserIntegral();
            userIntegral.setIntegral(IntegralEnum.BORROWER_CAR.getIntegral());
            userIntegral.setUserId(userId);
            userIntegral.setContent(IntegralEnum.BORROWER_CAR.getMsg());
            userIntegralService.save(userIntegral);
        }
        userInfo.setIntegral(curIntegral);
        userInfoService.updateById(userInfo);
        return Result.ok().message("审核成功!!");
    }
}
