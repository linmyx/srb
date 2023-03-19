package com.srb.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.srb.base.utils.JwtUtils;
import com.srb.common.result.Result;
import com.srb.core.hfb.RequestHelper;
import com.srb.core.service.UserAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 用户账户 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@RestController
@RequestMapping("/api/core/userAccount")
@Api(tags = "会员账户")
@Slf4j
public class UserAccountController {

    @Resource
     private UserAccountService userAccountService;

    @ApiOperation("充值接口")
    @PostMapping("/auth/commitCharge/{chargeAmt}")
    public Result commitCharge(@PathVariable BigDecimal chargeAmt, HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        return userAccountService.commitCharge(chargeAmt,userId);
    }

    @ApiOperation("充值成功之后回调")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request){
        //汇付宝向尚融宝回调时携带的请求参数
        Map<String, Object> map = RequestHelper.switchMap(request.getParameterMap());
        //校验签名
        if(RequestHelper.isSignEquals(map)) {
            //充值成功交易
            if("0001".equals(map.get("resultCode"))) {
                return userAccountService.notify(map);
            } else {
                return "success";
            }
        } else {
            return "fail";
        }
    }

    @ApiOperation("查询账户余额")
    @GetMapping("/auth/getAccount")
    public Result getAccount(HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        return userAccountService.getAccount(userId);
    }

    @ApiOperation("提现接口")
    @PostMapping("/auth/commitWithdraw/{fetchAmt}")
    public Result commitWithdraw(@PathVariable BigDecimal fetchAmt, HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        return userAccountService.commitWithdraw(fetchAmt,userId);
    }

    @ApiOperation("用户提现异步回调")
    @PostMapping("/notifyWithdraw")
    public String notifyWithdraw(HttpServletRequest request) {
        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        log.info("提现异步回调：" + JSON.toJSONString(paramMap));

        //校验签名
        if(RequestHelper.isSignEquals(paramMap)) {
            //提现成功交易
            if("0001".equals(paramMap.get("resultCode"))) {
                userAccountService.notifyWithdraw(paramMap);
            } else {
                log.info("提现异步回调充值失败：" + JSON.toJSONString(paramMap));
                return "fail";
            }
        } else {
            log.info("提现异步回调签名错误：" + JSON.toJSONString(paramMap));
            return "fail";
        }
        return "success";
    }

}

