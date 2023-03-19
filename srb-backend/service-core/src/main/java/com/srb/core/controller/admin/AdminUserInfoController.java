package com.srb.core.controller.admin;


import com.srb.common.result.Result;
import com.srb.core.pojo.query.UserInfoQuery;
import com.srb.core.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/admin/core/userInfo")

@Api(tags = "会员管理接口")
public class AdminUserInfoController {

    @Resource
    private UserInfoService userInfoService;

    @ApiOperation(value = "分页获取会员列表")
    @GetMapping("/getUserInfoPageList/{page}/{limit}")
    public Result getUserInfoPageList(
            @ApiParam(value = "查询对象", required = false)
            UserInfoQuery userInfoQuery,
            @PathVariable Long page,
            @PathVariable Long limit){
        return userInfoService.getUserInfoPageList(userInfoQuery,page,limit);
    }

    @ApiOperation("锁定和解锁")
    @PutMapping("/lock/{id}/{status}")
    public Result lock(@PathVariable Long id, @PathVariable Integer status){
        return userInfoService.lock(id,status);
    }

}
