package com.srb.core.controller.admin;

import com.srb.common.result.Result;
import com.srb.core.service.UserLoginRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/admin/core/userLoginRecord")

@Api(tags = "会员登录日志")
public class AdminUserLoginRecordController {

    @Resource
    private UserLoginRecordService loginRecordService;

    @ApiOperation(value = "获取用户登录前50日志")
    @GetMapping("/getUserLoginRecord/{userId}")
    public Result getLoginRecord(@PathVariable Long userId){
        return loginRecordService.getUserLoginRecord(userId);
    }

}
