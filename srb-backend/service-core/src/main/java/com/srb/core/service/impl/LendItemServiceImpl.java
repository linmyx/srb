package com.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.srb.common.exception.Assert;
import com.srb.common.result.ResponseEnum;
import com.srb.common.result.Result;
import com.srb.core.enums.LendStatusEnum;
import com.srb.core.enums.TransTypeEnum;
import com.srb.core.hfb.FormHelper;
import com.srb.core.hfb.HfbConst;
import com.srb.core.hfb.RequestHelper;
import com.srb.core.mapper.LendMapper;
import com.srb.core.mapper.UserAccountMapper;
import com.srb.core.pojo.bo.TransFlowBO;
import com.srb.core.pojo.entity.Lend;
import com.srb.core.pojo.entity.LendItem;
import com.srb.core.mapper.LendItemMapper;
import com.srb.core.pojo.entity.UserInfo;
import com.srb.core.pojo.vo.InvestVO;
import com.srb.core.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srb.core.utils.LendNoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借记录表 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@Service
@Slf4j
public class LendItemServiceImpl extends ServiceImpl<LendItemMapper, LendItem> implements LendItemService {



    @Resource
    private LendService lendService;

    @Resource
    private UserAccountService userAccountService;

    @Resource
    private UserBindService userBindService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private UserAccountMapper userAccountMapper;

    /**
     * 标底投资提交数据
     * @param investVO
     * @return
     */
    @Override
    public Result commitInvest(InvestVO investVO) {
        Long lendId = investVO.getLendId();
        Lend lend = lendService.getById(lendId);
        //标的状态必须为募资中
        //判断标底的状态
        Assert.isTrue(
                lend.getStatus().intValue() == LendStatusEnum.INVEST_RUN.getStatus().intValue(),
                ResponseEnum.LEND_INVEST_ERROR);
        //判断是否超卖：已投金额+当前投资金额>标底金额
        BigDecimal sum = lend.getInvestAmount().add(new BigDecimal(investVO.getInvestAmount()));
        Assert.isTrue(sum.doubleValue()<=lend.getAmount().doubleValue(),ResponseEnum.LEND_FULL_SCALE_ERROR);

        //用户余额：当前用户的金额<=用户的投资金额
        Long userId = investVO.getInvestUserId();
        Result account = userAccountService.getAccount(userId);
        Map<String, Object> data = account.getData();
        BigDecimal amount = (BigDecimal) data.get("row");
        Assert.isTrue(amount.doubleValue()>=Double.parseDouble(investVO.getInvestAmount()),ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);
        //判断用户类型是否是投资人
        UserInfo userInfo = userInfoService.getById(userId);
        Assert.isTrue(userInfo.getUserType()==1,ResponseEnum.USER_NO_INVEST);
        //判断投资金额是否是100的整数倍
        if(Integer.parseInt(investVO.getInvestAmount())==0 || Integer.parseInt(investVO.getInvestAmount())%100!=0){
            return Result.error().message("投资金额不是100的整数倍！！");
        }


        //获取投资人的bindCode
        String bindCode=userBindService.getBindCodeByUserId(userId);
        //获取借款人的bindCode
        String benefitBindCode=userBindService.getBindCodeByUserId(lend.getUserId());

        //生成标的下投资记录
        LendItem lendItem = new LendItem();
        lendItem.setInvestUserId(userId);//投资人id
        lendItem.setInvestName(investVO.getInvestName());//投资人名字
        String lendItemNo = LendNoUtils.getLendItemNo();
        lendItem.setLendItemNo(lendItemNo); //投资条目编号（一个Lend对应一个或多个LendItem）
        lendItem.setLendId(investVO.getLendId());//对应的标的id
        lendItem.setInvestAmount(new BigDecimal(investVO.getInvestAmount())); //此笔投资金额
        lendItem.setLendYearRate(lend.getLendYearRate());//年化
        lendItem.setInvestTime(LocalDateTime.now()); //投资时间
        lendItem.setLendStartDate(lend.getLendStartDate()); //开始时间
        lendItem.setLendEndDate(lend.getLendEndDate()); //结束时间

        //预期收益
        BigDecimal interestCount = lendService.getInterestCount(lend.getInvestAmount(), lend.getLendYearRate(), lend.getPeriod(), lend.getReturnMethod());
        lendItem.setExpectAmount(interestCount);
        //实际收益

        //实际收益
        lendItem.setRealAmount(new BigDecimal(0));
        //设置投资状态
        lendItem.setStatus(0);//默认状态：刚刚创建
        //对投资记录进行保存
        boolean save = save(lendItem);
        //判断投资记录是否添加成功
        if (!save){
            return Result.error().message("投资失败!!");
        }

        //封装提交至汇付宝的参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("voteBindCode", bindCode);
        paramMap.put("benefitBindCode",benefitBindCode);
        paramMap.put("agentProjectCode", lend.getLendNo());//项目标号
        paramMap.put("agentProjectName", lend.getTitle());

        //在资金托管平台上的投资订单的唯一编号，要和lendItemNo保持一致。
        paramMap.put("agentBillNo", lendItemNo);//订单编号
        paramMap.put("voteAmt", investVO.getInvestAmount());
        paramMap.put("votePrizeAmt", "0");
        paramMap.put("voteFeeAmt", "0");
        paramMap.put("projectAmt", lend.getAmount()); //标的总金额
        paramMap.put("note", "");
        paramMap.put("notifyUrl", HfbConst.INVEST_NOTIFY_URL); //检查常量是否正确
        paramMap.put("returnUrl", HfbConst.INVEST_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);
        String formStr = FormHelper.buildForm(HfbConst.INVEST_URL, paramMap);

        return Result.ok().data("formStr",formStr);
    }

    /**
     * 投标成功回调
     * @param paramMap
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notify(Map<String, Object> paramMap) {

        //获取投资编号
        String agentBillNo = (String)paramMap.get("agentBillNo");

        //回调幂等性返回
        boolean saveTransFlow = transFlowService.isSaveTransFlow(agentBillNo);
        if (saveTransFlow){
            log.info("幂等性返回");
            return;
        }

        //对投资人的余额进行同步：余额减去投资金额，增加冻结金额
        String voteBindCode = (String) paramMap.get("voteBindCode");
        String voteAmt = (String)paramMap.get("voteAmt");
        userAccountMapper.updateAccount(voteBindCode,new BigDecimal("-"+voteAmt),new BigDecimal(voteAmt));
        //修改投资状态
        LendItem lendItem = this.getByLendItemNo(agentBillNo);
        lendItem.setStatus(1);
        updateById(lendItem);
        //修改标底的已投金额和投资记录
        Long lendId = lendItem.getLendId();
        Lend lend = lendService.getById(lendId);
        lend.setInvestNum(lend.getInvestNum()+1);
        lend.setInvestAmount(lend.getInvestAmount().add(lendItem.getInvestAmount()));
        lendService.updateById(lend);
        //新增交易流水
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,
                voteBindCode,
                new BigDecimal(voteAmt),
                TransTypeEnum.INVEST_LOCK,
                "投资:项目编号:"+lend.getLendNo()+".项目名称"+lend.getTitle());
        transFlowService.saveTransFlow(transFlowBO);
    }

    /**
     * 查询投资列表
     * @param lendId
     * @param status
     * @return
     */
    @Override
    public List<LendItem> getLendItemInfo(Long lendId, Integer status) {
        QueryWrapper<LendItem> queryWrap = new QueryWrapper<>();
        queryWrap.eq("lend_id",lendId).eq("status",status);
        return list(queryWrap);
    }

    /**
     * 后端获取投资列表接口
     * @param lendId
     * @return
     */
    @Override
    public Result selectByLendId(Long lendId) {
        QueryWrapper<LendItem> queryWrap = new QueryWrapper<>();
        queryWrap.eq("lend_id", lendId);
        List<LendItem> list = list(queryWrap);
        return Result.ok().data("row",list);
    }

    /**
     * 根据流水号获取投资记录
     * @param agentBillNo
     * @return
     */
    private LendItem getByLendItemNo(String agentBillNo){
        QueryWrapper<LendItem> queryWrap=new QueryWrapper<>();
        queryWrap.eq("lend_item_no",agentBillNo);
        return getOne(queryWrap);
    }

}
