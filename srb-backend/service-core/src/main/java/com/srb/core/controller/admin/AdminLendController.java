package com.srb.core.controller.admin;


import com.srb.common.result.Result;
import com.srb.core.service.LendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 标的准备表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@Api(tags = "标底管理接口")
@RestController
@RequestMapping("/admin/core/lend")
public class AdminLendController {

    @Resource
    private LendService lendService;

    @ApiOperation("获取标底列表")
    @GetMapping("/getLendList")
    public Result getLendList(){
        return lendService.getLendList();
    }

    @ApiOperation("获取标的信息")
    @GetMapping("/getLendInfoById/{id}")
    public Result getLendInfoById(@PathVariable("id") Long id){
        return lendService.getLendInfoById(id);
    }

    @ApiOperation("放款")
    @GetMapping("/makeLoan/{id}")
    public Result makeLoan(
            @ApiParam(value = "标的id", required = true)
            @PathVariable("id") Long id) {
        lendService.makeLoan(id);
        return Result.ok().message("放款成功");
    }



}

