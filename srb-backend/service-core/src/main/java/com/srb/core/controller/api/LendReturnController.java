package com.srb.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.srb.base.utils.JwtUtils;
import com.srb.common.result.Result;
import com.srb.core.hfb.RequestHelper;
import com.srb.core.service.LendReturnService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 还款记录表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@RestController
@RequestMapping("/api/core/lendReturn")
@Api(tags = "还款记录接口")
@Slf4j
public class LendReturnController {
    @Resource
    private LendReturnService lendReturnService;

    @ApiOperation("获取列表")
    @GetMapping("/list/{lendId}")
    public Result list(@PathVariable Long lendId){
        return lendReturnService.selectByLendId(lendId);
    }
    @ApiOperation("用户还款")
    @PostMapping("/auth/commitReturn/{lendReturnId}")
    public Result commitReturn(
            @ApiParam(value = "还款计划id", required = true)
            @PathVariable Long lendReturnId, HttpServletRequest request) {

        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        String formStr = lendReturnService.commitReturn(lendReturnId, userId);
        return Result.ok().data("formStr", formStr);
    }

    @ApiOperation("还款异步回调")
    @PostMapping("/notifyUrl")
    public String notifyUrl(HttpServletRequest request) {

        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        log.info("还款异步回调：" + JSON.toJSONString(paramMap));

        //校验签名
        if(RequestHelper.isSignEquals(paramMap)) {
            if("0001".equals(paramMap.get("resultCode"))) {
                lendReturnService.notify(paramMap);
            } else {
                log.info("还款异步回调失败：" + JSON.toJSONString(paramMap));
                return "fail";
            }
        } else {
            log.info("还款异步回调签名错误：" + JSON.toJSONString(paramMap));
            return "fail";
        }
        return "success";
    }

}

