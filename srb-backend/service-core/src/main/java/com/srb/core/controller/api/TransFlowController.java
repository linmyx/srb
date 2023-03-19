package com.srb.core.controller.api;


import com.srb.base.utils.JwtUtils;
import com.srb.common.result.Result;
import com.srb.core.pojo.entity.TransFlow;
import com.srb.core.service.TransFlowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 交易流水表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@Api(tags = "资金记录")
@RestController
@RequestMapping("/api/core/transFlow")
public class TransFlowController {
    @Resource
    private TransFlowService transFlowService;

    @ApiOperation("获取列表")
    @GetMapping("/list")
    public Result list(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        return transFlowService.selectByUserId(userId);
    }
}

