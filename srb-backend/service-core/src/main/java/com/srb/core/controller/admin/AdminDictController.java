package com.srb.core.controller.admin;

import com.alibaba.excel.EasyExcel;
import com.srb.common.exception.BusinessException;
import com.srb.common.result.ResponseEnum;
import com.srb.common.result.Result;
import com.srb.core.pojo.dto.ExcelDictDTO;
import com.srb.core.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

@Api(tags = "数据字典管理")
@RestController
@RequestMapping("/admin/core/dict")

public class AdminDictController {

    @Resource
    private DictService dictService;

    @ApiOperation(value = "Excel文件数据导入接口")
    @PostMapping("/import")
    public Result batchImport(@RequestBody MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            dictService.importDate(inputStream);
            return Result.ok().message("数据导入成功!!");
        } catch (Exception e) {
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR,e);
        }
    }

    @ApiOperation(value = "Excel数据导出")
    @GetMapping("/download")
    public void download(HttpServletResponse response) throws IOException {
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("mydict", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), ExcelDictDTO.class).sheet("数据字典").doWrite(dictService.getDictData());
    }


    @ApiOperation(value = "根据上级id获取下级信息")
    @GetMapping("/getDictParentById/{parentId}")
    public Result getDictParentById(@PathVariable long parentId){
        return dictService.getDictParentById(parentId);
    }
}
