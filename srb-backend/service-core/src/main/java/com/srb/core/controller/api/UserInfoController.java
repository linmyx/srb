package com.srb.core.controller.api;


import com.srb.base.utils.JwtUtils;
import com.srb.common.exception.Assert;
import com.srb.common.result.ResponseEnum;
import com.srb.common.result.Result;
import com.srb.core.pojo.vo.LoginVo;
import com.srb.core.pojo.vo.RegisterVO;
import com.srb.core.pojo.vo.UserIndexVO;
import com.srb.core.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户基本信息 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@Api(tags = "会员管理接口")
@RestController
@RequestMapping("/api/core/userInfo")
public class UserInfoController {

    @Resource
    private UserInfoService userInfoService;

    @ApiOperation(value = "会员注册")
    @PostMapping("/register")
    public Result register(@RequestBody RegisterVO registerVO){
        return userInfoService.register(registerVO);
    }

    @ApiOperation(value = "用户登录")
    @PostMapping("/login")
    public Result login(@RequestBody LoginVo loginVo, HttpServletRequest request){
        return userInfoService.login(loginVo,request);
    }

    @ApiOperation(value = "校验令牌")
    @GetMapping("/checkToken")
    public Result checkToken(HttpServletRequest request){
        String token = request.getHeader("token");
        boolean result = JwtUtils.checkToken(token);
        if ( !result) {
            return Result.setResult(ResponseEnum.LOGIN_AUTH_ERROR);
        }else {
            return Result.ok();
        }
    }

    @ApiOperation(value = "校验用户是否注册")
    @GetMapping("/checkMobile/{mobile}")
    public boolean checkMobile(@PathVariable String mobile){

        return  userInfoService.checkMobile(mobile);
    }

    @ApiOperation("获取个人空间用户信息")
    @GetMapping("/auth/getIndexUserInfo")
    public Result getIndexUserInfo(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        UserIndexVO userIndexVO = userInfoService.getIndexUserInfo(userId);
        return Result.ok().data("userIndexVO", userIndexVO);
    }

}

