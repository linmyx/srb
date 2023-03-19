package com.srb.core.controller.api;


import com.srb.base.utils.JwtUtils;
import com.srb.common.result.Result;
import com.srb.core.pojo.vo.BorrowerVO;
import com.srb.core.service.BorrowerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 借款人 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@Api(tags = "借款人")
@RestController
@RequestMapping("/api/core/borrower")
@Slf4j
public class BorrowerController {

    @Resource
    private BorrowerService borrowerService;

    @ApiOperation("保存借款人认证信息")
    @PostMapping("/save")
    public Result save(@RequestBody BorrowerVO borrowerVO, HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
       return borrowerService.saveBorrowerVoByUserId(borrowerVO,userId);
    }

    @ApiOperation("获取借款人认证状态")
    @GetMapping("/auth/getBorrowerStatus")
    public Result getBorrowerStatus(HttpServletRequest request){

        //获取当前用户的id
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        return borrowerService.getBorrowerStatus(userId);
    }



}

