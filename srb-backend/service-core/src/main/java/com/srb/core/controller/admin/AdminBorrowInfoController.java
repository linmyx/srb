package com.srb.core.controller.admin;

import com.srb.common.result.Result;
import com.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.srb.core.service.BorrowInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "借款管理")
@RestController
@RequestMapping("/admin/core/borrowerInfo")
public class AdminBorrowInfoController {

    @Resource
    private BorrowInfoService borrowInfoService;

    @ApiOperation("借款信息列表")
    @GetMapping("/getBorrowerInfoList")
    public Result getBorrowerInfoList(){
        return borrowInfoService.getBorrowerInfoList();
    }

    @ApiOperation("借款信息详情")
    @GetMapping("/getShowInfo/{id}")
    public Result getShowInfo(@PathVariable Long id){
        return borrowInfoService.getShowInfo(id);
    }

    @ApiOperation("审批借款信息")
    @PostMapping("/approval")
    public Result approval(@RequestBody BorrowInfoApprovalVO borrowInfoApprovalVO){
        return borrowInfoService.approval(borrowInfoApprovalVO);
    }

}
