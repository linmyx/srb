package com.srb.core.controller.api;


import com.srb.base.utils.JwtUtils;
import com.srb.common.result.Result;
import com.srb.core.pojo.entity.BorrowInfo;
import com.srb.core.pojo.entity.UserInfo;
import com.srb.core.service.BorrowInfoService;
import com.srb.core.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 借款信息表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@Api(tags = "借款信息")
@RestController
@RequestMapping("/api/core/borrowInfo")
@Slf4j
public class BorrowInfoController {

    @Resource
    private BorrowInfoService borrowInfoService;

    @ApiOperation("获取借款额度信息")
    @GetMapping("/auth/getBorrowAmount")
    public Result getBorrowAmount(HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        return borrowInfoService.getBorrowAmount(userId);
    }

    @ApiOperation("保存借款申请")
    @PostMapping("/saveBorrowInfo")
    public Result saveBorrowInfo(@RequestBody BorrowInfo borrowInfo,HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        return  borrowInfoService.saveBorrowInfo(userId, borrowInfo);
    }

    @ApiOperation("借款申请状态查询")
    @GetMapping("/getUserBorrowStatus")
    public Result getUserBorrowStatus(HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        return  borrowInfoService.getUserBorrowStatus(userId);
    }

}

