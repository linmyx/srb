package com.srb.core.controller.admin;

import com.srb.common.result.Result;
import com.srb.core.pojo.vo.BorrowerApprovalVO;
import com.srb.core.service.BorrowerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
@Api(tags = "借款人管理接口")
@RestController
@RequestMapping("/admin/core/borrower")
public class AdminBorrowerController {

    @Resource
    private BorrowerService borrowerService;

    @ApiOperation("分页获取借款人审核列表")
    @PostMapping("/getBorrowerList/{page}/{limit}")
    public Result getBorrowerList(
                                  @ApiParam(value = "查询关键字",required = true)
                                  @PathVariable Long page,
                                  @ApiParam(value = "查询关键字",required = true)
                                  @PathVariable Long limit,
                                  @ApiParam(value = "查询关键字",required = false)
                                  @RequestParam String keyword){
        return borrowerService.getBorrowerList(page,limit,keyword);

    }
    @ApiOperation("根据id获取借款人信息")
    @GetMapping("/getBorrowerInfo/{id}")
    public Result getBorrowerInfo(@PathVariable Long id){
        return borrowerService.getBorrowerInfo(id);
    }

    @ApiOperation("借款额度审批")
    @PostMapping("/approval")
    public Result approval(@RequestBody BorrowerApprovalVO borrowerApprovalVO){
        return borrowerService.approval(borrowerApprovalVO);
    }


}
