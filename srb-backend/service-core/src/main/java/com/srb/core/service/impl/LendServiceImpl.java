package com.srb.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.srb.common.exception.BusinessException;
import com.srb.common.result.Result;
import com.srb.core.enums.LendStatusEnum;
import com.srb.core.enums.ReturnMethodEnum;
import com.srb.core.enums.TransTypeEnum;
import com.srb.core.hfb.HfbConst;
import com.srb.core.hfb.RequestHelper;
import com.srb.core.mapper.UserAccountMapper;
import com.srb.core.mapper.UserInfoMapper;
import com.srb.core.pojo.bo.TransFlowBO;
import com.srb.core.pojo.entity.*;
import com.srb.core.mapper.LendMapper;
import com.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.srb.core.pojo.vo.BorrowerDetailVO;
import com.srb.core.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srb.core.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 标的准备表 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@Service
@Slf4j
public class LendServiceImpl extends ServiceImpl<LendMapper, Lend> implements LendService {

    @Resource
    private DictService dictService;
    @Resource
    private BorrowerService borrowerService;


    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private LendItemService lendItemService;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private LendReturnService lendReturnService;

    @Resource
    private LendItemReturnService lendItemReturnService;

    /**
     * 根据审批借款信息，生成标底
     * @param borrowInfoApprovalVO
     * @param borrowInfo
     */
    @Override
    public void created(BorrowInfoApprovalVO borrowInfoApprovalVO, BorrowInfo borrowInfo) {
        Lend lend=new Lend();
        lend.setUserId(borrowInfo.getUserId());
        lend.setBorrowInfoId(borrowInfo.getId());
        lend.setLendNo(com.srb.core.utils.LendNoUtils.getLendNo());//生成编号
        lend.setTitle(borrowInfoApprovalVO.getTitle());
        lend.setAmount(borrowInfo.getAmount());
        lend.setPeriod(borrowInfo.getPeriod());
        lend.setLendYearRate(borrowInfoApprovalVO.getLendYearRate().divide(new BigDecimal(100)));//从审批对象中获取
        lend.setServiceRate(borrowInfoApprovalVO.getServiceRate().divide(new BigDecimal(100)));//从审批对象中获取
        lend.setReturnMethod(borrowInfo.getReturnMethod());
        lend.setLowestAmount(new BigDecimal(100)); //最低投资金额
        lend.setInvestAmount(new BigDecimal(0));   //已投金额
        lend.setInvestNum(0);  //已投人数
        lend.setPublishDate(LocalDateTime.now());  //标底发布时间

        //起息日期
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate lendStartDate = LocalDate.parse(borrowInfoApprovalVO.getLendStartDate(), dtf);
        lend.setLendStartDate(lendStartDate);
        //结束日期
        LocalDate lendEndDate = lendStartDate.plusMonths(borrowInfo.getPeriod());
        lend.setLendEndDate(lendEndDate);

        lend.setLendInfo(borrowInfoApprovalVO.getLendInfo());//描述

        //平台预期收益  = 标的金额*（年化 / 12 * 期数）
        //计算收益
        //月年化
        BigDecimal mothRate = lend.getServiceRate().divide(new BigDecimal(12), 8, BigDecimal.ROUND_DOWN);
        BigDecimal expectAmount = lend.getAmount().multiply(mothRate.multiply(new BigDecimal(lend.getPeriod())));
        lend.setExpectAmount(expectAmount);
        //实际收益
        lend.setRealAmount(new BigDecimal(0));
        lend.setStatus(LendStatusEnum.INVEST_RUN.getStatus());
        lend.setCreateTime(LocalDateTime.now()); //审核时间
        lend.setCheckAdminId(1L); //审核人
        save(lend);
    }

    /**
     * 获取标底列表
     * @return
     */
    @Override
    public Result getLendList() {
        List<Lend> list = list(null);
        list.forEach(lend -> {
            String returnMethod = dictService.getDictCodeAndValue("returnMethod", lend.getReturnMethod());
            String status = LendStatusEnum.getMsgByStatus(lend.getStatus());
            lend.getParam().put("returnMethod", returnMethod);
            lend.getParam().put("status", status);
        });

        return Result.ok().data("row",list);
    }

    /**
     * 根据id查看标底详情
     * @param id
     * @return
     */
    @Override
    public Result getLendInfoById(Long id) {

        Lend lend = getById(id);
        Long userId = lend.getUserId();
        //组装数据
        String returnMethod = dictService.getDictCodeAndValue("returnMethod", lend.getReturnMethod());
        String status = LendStatusEnum.getMsgByStatus(lend.getStatus());
        lend.getParam().put("returnMethod", returnMethod);
        lend.getParam().put("status", status);

        //根据user_id获取借款人对象
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<Borrower>();
        borrowerQueryWrapper.eq("user_id", userId);
        Borrower borrower = borrowerService.getOne(borrowerQueryWrapper);


        //组装借款人对象
        Result borrowerInfo = borrowerService.getBorrowerInfo(borrower.getId());
        BorrowerDetailVO borrowerDetailVO= (BorrowerDetailVO) borrowerInfo.getData().get("row");

        //组装数据
        Map<String, Object> result = new HashMap<>();
        result.put("lend", lend);
        result.put("borrower", borrowerDetailVO);


        return Result.ok().data(result);
    }

    /**
     * 计算投资收益
     * @param invest
     * @param yearRate
     * @param totalmonth
     * @param returnMethod
     * @return
     */
    @Override
    public BigDecimal getInterestCount(BigDecimal invest, BigDecimal yearRate, Integer totalmonth, Integer returnMethod) {

        BigDecimal interestCount;
        
        if (returnMethod.intValue()== ReturnMethodEnum.ONE.getMethod()){
            interestCount = Amount1Helper.getInterestCount(invest, yearRate, totalmonth);
        } else if (returnMethod.intValue()==ReturnMethodEnum.TWO.getMethod()) {
            interestCount = Amount2Helper.getInterestCount(invest, yearRate, totalmonth);
        }else if (returnMethod.intValue() == ReturnMethodEnum.THREE.getMethod()) {
            interestCount = Amount3Helper.getInterestCount(invest, yearRate, totalmonth);
        } else{
            interestCount = Amount4Helper.getInterestCount(invest, yearRate, totalmonth);
        }

        return interestCount;
    }

    /**
     * 标的满标放款
     * @param id
     */
    @Override
    @Transactional
    public void makeLoan(Long id) {
        //获取标底信息
        Lend lend = getById(id);
        //调用汇付宝汇款接口
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("agentId", HfbConst.AGENT_ID);
        hashMap.put("agentProjectCode",lend.getLendNo());
        hashMap.put("agentBillNo", LendNoUtils.getLoanNo());

        //平台收益，放款扣除，借款人借款实际金额=借款金额-平台收益
        //月年化
        BigDecimal monthRate = lend.getServiceRate().divide(new BigDecimal(12),8,BigDecimal.ROUND_DOWN);
        //平台实际收益 = 已投金额 * 月年化 * 标的期数
        BigDecimal realAmount = lend.getInvestAmount().multiply(monthRate).multiply(new BigDecimal(lend.getPeriod()));
        hashMap.put("mchFee",realAmount);
        hashMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(hashMap);
        hashMap.put("sign", sign);
        //远程提交请求
        JSONObject result = RequestHelper.sendRequest(hashMap, HfbConst.MAKE_LOAD_URL);

        //放款失败
        if (!"0000".equals(result.getString("resultCode"))) {
            throw new BusinessException(result.getString("resultMsg"));
        }
        //放款成功
        //更新标的信息
        lend.setRealAmount(realAmount);//平台收益
        lend.setStatus(LendStatusEnum.PAY_RUN.getStatus());
        lend.setPaymentTime(LocalDateTime.now());
        updateById(lend);
        //给借款人账号转入金额
        Long userId = lend.getUserId();
        UserInfo userInfo = userInfoMapper.selectById(userId);
        String bindCode = userInfo.getBindCode();
        String voteAmt = result.getString("voteAmt");
        //转账
        userAccountMapper.updateAccount(bindCode,new BigDecimal(voteAmt),new BigDecimal(0));

        //给借款人增加流水
        TransFlowBO transFlowBO = new TransFlowBO(
                result.getString("agentBillNo"),
                bindCode,
                new BigDecimal(voteAmt),
                TransTypeEnum.BORROW_BACK,
                "项目放款,项目编号:"+lend.getLendNo()+",项目名称:"+lend.getTitle()
        );
        transFlowService.saveTransFlow(transFlowBO);
        //解冻并扣除投资人金额
        //获取标底下的投资列表
        List<LendItem> lendItemInfo = lendItemService.getLendItemInfo(id, 1);
        lendItemInfo.stream().forEach(item->{
            Long investUserId = item.getInvestUserId();
            UserInfo investUserInfo = userInfoMapper.selectById(investUserId);
            String investBindCode = investUserInfo.getBindCode();
            userAccountMapper.updateAccount(investBindCode,new BigDecimal(0),item.getInvestAmount().negate());

            //增加投资交易流水
            TransFlowBO investTransFlowBO = new TransFlowBO(
                    LendNoUtils.getTransNo(),
                    investBindCode,
                    item.getInvestAmount(),
                    TransTypeEnum.INVEST_UNLOCK,
                    "项目放款,冻结资金转出,项目编号:"+lend.getLendNo()+",项目名称:"+lend.getTitle()
            );
            transFlowService.saveTransFlow(investTransFlowBO);
        });
        //放款成功生成借款人还款计划和投资人回款计划
        this.repaymentPlan(lend);
    }


    /**
     * 还款计划
     * @param lend
     */
    public void repaymentPlan(Lend lend){
        //还款计划列表
        List<LendReturn> lendReturnList=new ArrayList<>();
        //按还款时间生成还款期数
        int period = lend.getPeriod().intValue();
        for (int i = 1; i <= period; i++) {
            //创建还款计划对象
            LendReturn lendReturn=new LendReturn();
            //填充基本属性
            //创建还款计划对象
            lendReturn.setReturnNo(LendNoUtils.getReturnNo());  //流水号
            lendReturn.setLendId(lend.getId());  //标的id
            lendReturn.setBorrowInfoId(lend.getBorrowInfoId());  //借款id
            lendReturn.setUserId(lend.getUserId());  //借款人id
            lendReturn.setAmount(lend.getAmount());  //借款金额
            lendReturn.setBaseAmount(lend.getInvestAmount()); //已投金额
            lendReturn.setLendYearRate(lend.getLendYearRate());  //年化
            lendReturn.setCurrentPeriod(i);//当前期数
            lendReturn.setReturnMethod(lend.getReturnMethod());  //还款方式
            //说明：还款计划中的这三项 = 回款计划中对应的这三项和：因此需要先生成对应的回款计划
            //			lendReturn.setPrincipal();
            //			lendReturn.setInterest();
            //			lendReturn.setTotal();
            lendReturn.setFee(new BigDecimal(0)); //手续费
            lendReturn.setReturnDate(lend.getLendStartDate().plusMonths(i)); //第二个月开始还款
            lendReturn.setOverdue(false); //是否逾期

            //判断是否是最后一次还款
            if (i == period){
                lendReturn.setLast(true);
            }else {
                lendReturn.setLast(false);
            }
            //设置还款状态
            lendReturn.setStatus(0);
            //将还款对象加入到还款计划当中
            lendReturnList.add(lendReturn);
        }
        //批量保存还款计划
        lendReturnService.saveBatch(lendReturnList);

        /////////////////////////////////////////////////////////////
        //生成期数和还款记录的id对应的还款键值对
        Map<Integer, Long> lendReturnMap = lendReturnList.stream().collect(
                Collectors.toMap(LendReturn::getCurrentPeriod, LendReturn::getId)
        );

        //创建所有投资的所有回款记录列表
        List<LendItemReturn> lendItemReturnArrayList=new ArrayList<>();

        //获取当前所有标底下的所有已支付的投资
        List<LendItem> lendItemList=lendItemService.getLendItemInfo(lend.getId(),1);
        //遍历所有已支付的投资列表
        for (LendItem lendItem :lendItemList) {
            //根据投资记录的id调用回款计划生成的方法，得到当前这笔投资的回款计划列表
            List<LendItemReturn> lendItemReturnList = this.returnInvest(lendItem.getId(), lendReturnMap, lend);
            //将当前这笔投资的回款计划列表，放入所有投资的所有回款记录列表
            lendItemReturnArrayList.addAll(lendItemReturnList);
        }

        //遍历还款记录列表
        for (LendReturn lendReturn :lendReturnList) {

            //通过filter、map、reduce将相关的期数回款数据过滤出来

            //将当前期数的所有投资人的数据相加，就是当前期数的所有投资人的回款数据(本金、利息、总金额)
            //本金
            BigDecimal sunPrincipal = lendItemReturnArrayList.stream()
                    .filter(itrm -> itrm.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getPrincipal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            //利息
            BigDecimal sunInterest = lendItemReturnArrayList.stream()
                    .filter(itrm -> itrm.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getInterest)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            //总金额
            BigDecimal sunTotal = lendItemReturnArrayList.stream()
                    .filter(itrm -> itrm.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            //将计算出的数据填入到还款计划记录：设置本金、利息、总金额
            lendReturn.setPrincipal(sunPrincipal);
            lendReturn.setInterest(sunInterest);
            lendReturn.setTotal(sunTotal);
        }

        //批量更新
        lendReturnService.updateBatchById(lendReturnList);

    }

    /**
     * 回款计划
     * 针对某一笔投资的回款
     * @param lendItemId
     * @param lendReturnMap 还款期数与还款计划id对应map
     * @param lend
     * @return
     */
    public List<LendItemReturn> returnInvest(Long lendItemId, Map<Integer, Long> lendReturnMap, Lend lend) {
        //获取当前投资记录的信息
        LendItem lendItem = lendItemService.getById(lendItemId);
        //调用工具类计算还款本金和利息，存储为集合
        //投资金额
        BigDecimal amount = lendItem.getInvestAmount();
        //年化利率
        BigDecimal yearRate = lendItem.getLendYearRate();
        //投资期数
        Integer totalMonth = lend.getPeriod();
        Map<Integer, BigDecimal> mapInterest = null;  //还款期数 -> 利息
        Map<Integer, BigDecimal> mapPrincipal = null; //还款期数 -> 本金
        //根据还款方式计算本金和利息
        if (lend.getReturnMethod().intValue() == ReturnMethodEnum.ONE.getMethod()) {
            //利息
            mapInterest = Amount1Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            //本金
            mapPrincipal = Amount1Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.TWO.getMethod()) {
            mapInterest = Amount2Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount2Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.THREE.getMethod()) {
            mapInterest = Amount3Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount3Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else {
            mapInterest = Amount4Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount4Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        }

        //创建回款计划列表
        List<LendItemReturn> lendItemReturnList=new ArrayList<>();
        //遍历集合得到期数
        for (Map.Entry<Integer,BigDecimal> entry:mapPrincipal.entrySet()){
            //获取当前期数
            Integer currentPeriod = entry.getKey();
            //根据当前期数,获取还款记录的id
            Long lendReturnId = lendReturnMap.get(currentPeriod);
            //创建回款计划记录
            LendItemReturn lendItemReturn=new LendItemReturn();
            //设置回款记录的基本属性
            lendItemReturn.setLendReturnId(lendReturnId);
            lendItemReturn.setLendItemId(lendItemId);
            lendItemReturn.setInvestUserId(lendItem.getInvestUserId());
            lendItemReturn.setLendId(lendItem.getLendId());
            lendItemReturn.setInvestAmount(lendItem.getInvestAmount());
            lendItemReturn.setLendYearRate(lend.getLendYearRate());
            lendItemReturn.setCurrentPeriod(currentPeriod);
            lendItemReturn.setReturnMethod(lend.getReturnMethod());

            //计算回款本金，利息和总额（注意最后一次还款）
            if (currentPeriod.intValue()== lend.getPeriod().intValue()){
                //最后一期
                //本金
                BigDecimal sunPrincipal = lendItemReturnList.stream().map(LendItemReturn::getPrincipal).reduce(BigDecimal.ZERO, BigDecimal::add);
                //最后一起的回款金额
                BigDecimal lastPrincipal = lendItem.getInvestAmount().subtract(sunPrincipal);
                lendItemReturn.setPrincipal(lastPrincipal);

                //利息
                BigDecimal sunInterest = lendItemReturnList.stream()
                        .map(LendItemReturn::getInterest).reduce(BigDecimal.ZERO, BigDecimal::add);
                //最后一起的回款金额
                BigDecimal lastInterest = lendItem.getExpectAmount().subtract(sunInterest);
                lendItemReturn.setInterest(lastInterest);
            }else {
                //非最后一期
                lendItemReturn.setPrincipal(mapPrincipal.get(currentPeriod));
                lendItemReturn.setInterest(mapInterest.get(currentPeriod));
            }
            //设置回款总金额
            lendItemReturn.setTotal(lendItemReturn.getPrincipal().add(lendItemReturn.getInterest()));
            //手续费
            lendItemReturn.setFee(new BigDecimal("0"));
            //设置还款日期
            lendItemReturn.setReturnDate(lend.getLendStartDate().plusMonths(currentPeriod));
            //是否预期
            lendItemReturn.setOverdue(false);
            //设置状态
            lendItemReturn.setStatus(0);

            //将回款记录放入到回款列表中
            lendItemReturnList.add(lendItemReturn);
        }
        //批量保存
        lendItemReturnService.saveBatch(lendItemReturnList);
        return lendItemReturnList;
    }


}
