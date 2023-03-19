package com.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.srb.base.Dto.SmsDTO;
import com.srb.common.exception.Assert;
import com.srb.common.result.ResponseEnum;
import com.srb.common.result.Result;
import com.srb.core.enums.TransTypeEnum;
import com.srb.core.hfb.FormHelper;
import com.srb.core.hfb.HfbConst;
import com.srb.core.hfb.RequestHelper;
import com.srb.core.pojo.bo.TransFlowBO;
import com.srb.core.pojo.entity.UserAccount;
import com.srb.core.mapper.UserAccountMapper;
import com.srb.core.pojo.entity.UserInfo;
import com.srb.core.service.TransFlowService;
import com.srb.core.service.UserAccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srb.core.service.UserBindService;
import com.srb.core.service.UserInfoService;
import com.srb.core.utils.LendNoUtils;
import com.srb.mq.constant.MQConst;
import com.srb.mq.service.MQService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户账户 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@Service
@Slf4j
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private UserBindService userBindService;
    @Resource
    private MQService mqService;

    /**
     * 充值接口
     * @param chargeAmt
     * @param userId
     * @return
     */
    @Override
    public Result commitCharge(BigDecimal chargeAmt, Long userId) {

        UserInfo userInfo = userInfoService.getById(userId);
        String bindCode = userInfo.getBindCode();
        //判断账户绑定状态
        Assert.notEmpty(bindCode, ResponseEnum.USER_NO_BIND_ERROR);
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentBillNo", com.srb.core.utils.LendNoUtils.getNo());
        paramMap.put("bindCode", bindCode);
        paramMap.put("chargeAmt", chargeAmt);
        paramMap.put("feeAmt", new BigDecimal("0"));
        paramMap.put("notifyUrl", HfbConst.RECHARGE_NOTIFY_URL);//检查常量是否正确
        paramMap.put("returnUrl", HfbConst.RECHARGE_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        String formStr = FormHelper.buildForm(HfbConst.RECHARGE_URL, paramMap);

        return Result.ok().data("row",formStr);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String notify(Map<String, Object> map) {

        //判断幂等性，避免重复充值
        String agentBillNo = (String)map.get("agentBillNo"); //商户充值订单号
        boolean saveTransFlow = transFlowService.isSaveTransFlow(agentBillNo);
        if (saveTransFlow){
            log.info("幂等性返回");
            return "success";
        }
        String bindCode = (String)map.get("bindCode"); //充值人绑定协议号
        String chargeAmt = (String)map.get("chargeAmt"); //充值金额
        //优化
        baseMapper.updateAccount(bindCode, new BigDecimal(chargeAmt), new BigDecimal(0));
        //记录账户流水
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,
                bindCode,
                new BigDecimal(chargeAmt),
                TransTypeEnum.RECHARGE, "充值");
        transFlowService.saveTransFlow(transFlowBO);

        //通过MQ发送消息
        String mobile = userInfoService.getMobileByBindCode(bindCode);
        SmsDTO smsDTO=new SmsDTO();

        smsDTO.setMobile(mobile);
        smsDTO.setMessage("尊敬的用户，您在"+new Date()+"在尚融宝充值了:"+chargeAmt+"元，请注意查收!!");
        mqService.sendMessage(MQConst.EXCHANGE_TOPIC_SMS,MQConst.ROUTING_SMS_ITEM,smsDTO);

        return "success";
    }

    /**
     * 根据用户id，查询用户余额
     * @param userId
     * @return
     */
    @Override
    public Result getAccount(Long userId) {
        QueryWrapper<UserAccount> query = new QueryWrapper<>();
        query.eq("user_id",userId);
        UserAccount userAccount = getOne(query);
        UserInfo userInfo = userInfoService.getById(userId);
        if(userInfo == null){
            return Result.error().message("账户信息错误!");
        }
        if (userAccount == null){
            return Result.ok().message("账号暂未开通账户!!");
        }
        return Result.ok().data("row",userAccount.getAmount());
    }

    /**
     * 用户提现
     * @param fetchAmt
     * @param userId
     * @return
     */
    @Override
    public Result commitWithdraw(BigDecimal fetchAmt, Long userId) {
        //获取账户余额
        Result account = this.getAccount(userId);
        Map<String, Object> data = account.getData();
        BigDecimal amount = (BigDecimal) data.get("row");
        Assert.isTrue(amount.doubleValue() >= fetchAmt.doubleValue(),
                ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);

        String bindCode = userBindService.getBindCodeByUserId(userId);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentBillNo", LendNoUtils.getWithdrawNo());
        paramMap.put("bindCode", bindCode);
        paramMap.put("fetchAmt", fetchAmt);
        paramMap.put("feeAmt", new BigDecimal(0));
        paramMap.put("notifyUrl", HfbConst.WITHDRAW_NOTIFY_URL);
        paramMap.put("returnUrl", HfbConst.WITHDRAW_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        //构建自动提交表单
        String formStr = FormHelper.buildForm(HfbConst.WITHDRAW_URL, paramMap);
        return Result.ok().data("formStr",formStr);
    }

    /**
     * 提现接口回调
     * @param paramMap
     */
    @Override
    public void notifyWithdraw(Map<String, Object> paramMap) {
        //幂等判断
        log.info("提现成功");
        String agentBillNo = (String)paramMap.get("agentBillNo");
        boolean result = transFlowService.isSaveTransFlow(agentBillNo);
        if(result){
            log.warn("幂等性返回");
            return;
        }
        //账户同步
        String bindCode = (String) paramMap.get("bindCode");
        String fetchAmt = (String) paramMap.get("fetchAmt");
        baseMapper.updateAccount(bindCode,new BigDecimal("-"+fetchAmt),new BigDecimal(0));
        //交易流水
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,
                bindCode,
                new BigDecimal(fetchAmt),
                TransTypeEnum.WITHDRAW, "账户提现");
        transFlowService.saveTransFlow(transFlowBO);
    }
}
