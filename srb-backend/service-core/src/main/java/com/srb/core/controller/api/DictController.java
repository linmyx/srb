package com.srb.core.controller.api;


import com.srb.common.result.Result;
import com.srb.core.service.DictService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 数据字典 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@Api(tags = "数据字典")
@RestController
@RequestMapping("/api/core/dict")
@Slf4j
public class DictController {
    @Resource
    private DictService dictService;

    @GetMapping("/findByDictCode/{dictCode}")
    public Result findByDictCode(@PathVariable String dictCode) {
        return dictService.findByDictCode(dictCode);
    }

}

