package com.srb.core.controller.admin;

import com.srb.common.exception.Assert;

import com.srb.common.result.ResponseEnum;
import com.srb.common.result.Result;
import com.srb.core.pojo.entity.IntegralGrade;
import com.srb.core.service.IntegralGradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/admin/core/integralGrade")
@Api(tags = "积分等级管理")
public class AdminIntegralGradeController {

    @Resource
    private IntegralGradeService integralGradeService;

    /**
     * 查询所有的积分等级列表
     * @return
     */
    @GetMapping("/getIntegralGradeList")
    @ApiOperation("积分等级列表")
    public Result getIntegralGradeList() {
        List<IntegralGrade> list = integralGradeService.list();
        return Result.ok().data("list",list);
    }


    /**
     * 根据id删除积分等级列表
     * @param id
     * @return
     */
    @DeleteMapping("/removeById/{id}")
    @ApiOperation(value = "根据id删除积分等级", notes = "逻辑删除")
    public Result removeById(
            @ApiParam(value = "数据id", required = true, example = "100")
            @PathVariable long id){
        boolean b = integralGradeService.removeById(id);
        return b ? Result.ok().message("删除成功!!") :Result.error().message("删除失败!!");
    }

    /**
     * 新增积分等级列表
     */
    @PostMapping("/save")
    @ApiOperation(value = "新增积分等级")
    public Result save(@RequestBody IntegralGrade integralGrade){


        //使用断言的方式来判断是否为空
        Assert.notNull(integralGrade.getBorrowAmount(),ResponseEnum.BORROW_AMOUNT_NULL_ERROR);
        boolean save = integralGradeService.save(integralGrade);
        return save ? Result.ok().message("添加成功!!") :Result.error().message("添加失败!!");
    }

    /**
     * 根据id查询积分等级信息
     */
    @ApiOperation(value = "根据id查询积分等级信息")
    @GetMapping("/getInfoById/{id}")
    public Result getInfoById(@PathVariable long id){
        IntegralGrade integralGrade = integralGradeService.getById(id);
        return Result.ok().data("info",integralGrade);
    }

    /**
     * 根据id修改积分等级信息
     */
    @ApiOperation(value = "修改积分等级信息")
    @PutMapping("/updateIntegralGrade")
    public Result update(@RequestBody IntegralGrade integralGrade) {
        boolean b = integralGradeService.updateById(integralGrade);
        return b ? Result.ok().message("修改成功!!") :Result.error().message("修改失败!!");
    }

}
