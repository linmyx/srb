package com.srb.core.controller.api;


import com.srb.common.result.Result;
import com.srb.core.pojo.entity.Lend;
import com.srb.core.service.LendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * <p>
 * 标的准备表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@RestController
@RequestMapping("/api/core/lend")
@Api(tags = "标底管理接口")
public class LendController {
    @Resource
    private LendService lendService;

    @ApiOperation("获取标的列表")
    @GetMapping("/list")
    public Result list() {
        return lendService.getLendList();
    }

    @ApiOperation("查询标底信息")
    @GetMapping("/showInfo/{id}")
    public Result showInfo(@PathVariable Long id){
       return lendService.getLendInfoById(id);
    }

    @ApiOperation("计算投资收益")
    @GetMapping("/getInterestCount/{invest}/{yearRate}/{totalmonth}/{returnMethod}")
    public Result getInterestCount(
            @ApiParam(value = "投资金额", required = true)
            @PathVariable("invest") BigDecimal invest,

            @ApiParam(value = "年化收益", required = true)
            @PathVariable("yearRate")BigDecimal yearRate,

            @ApiParam(value = "期数", required = true)
            @PathVariable("totalmonth")Integer totalmonth,

            @ApiParam(value = "还款方式", required = true)
            @PathVariable("returnMethod")Integer returnMethod) {

        BigDecimal  interestCount = lendService.getInterestCount(invest, yearRate, totalmonth, returnMethod);
        return Result.ok().data("interestCount", interestCount);
    }




}

