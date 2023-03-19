package com.srb.core.controller.admin;


import com.alibaba.fastjson.JSON;
import com.srb.base.utils.JwtUtils;
import com.srb.common.result.Result;
import com.srb.core.hfb.RequestHelper;
import com.srb.core.pojo.vo.InvestVO;
import com.srb.core.service.LendItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 标的出借记录表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@RestController
@RequestMapping("/admin/core/lendItem")
@Api(tags = "标底的投资管理")
@Slf4j
public class AdminLendItemController {

    @Resource
    private LendItemService lendItemService;

    @ApiOperation("获取列表")
    @GetMapping("/list/{lendId}")
    public Result list(
            @ApiParam(value = "标的id", required = true)
            @PathVariable Long lendId) {
        return lendItemService.selectByLendId(lendId);
    }

}

