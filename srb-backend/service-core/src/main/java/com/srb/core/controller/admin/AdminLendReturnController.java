package com.srb.core.controller.admin;


import com.srb.common.result.Result;
import com.srb.core.service.LendReturnService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 还款记录表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@RestController
@RequestMapping("/admin/core/lendReturn")
@Api(tags = "还款记录接口")
public class AdminLendReturnController {
    @Resource
    private LendReturnService lendReturnService;

    @ApiOperation("获取列表")
    @GetMapping("/list/{lendId}")
    public Result list(@PathVariable Long lendId){
        return lendReturnService.selectByLendId(lendId);
    }
}

