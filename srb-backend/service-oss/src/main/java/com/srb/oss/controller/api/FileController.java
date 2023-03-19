package com.srb.oss.controller.api;


import com.srb.common.exception.BusinessException;
import com.srb.common.result.ResponseEnum;
import com.srb.common.result.Result;
import com.srb.oss.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/oss")
@Api(tags = "文件上传")
@Slf4j
public class FileController {

    @Resource
    private FileService fileService;

    @ApiOperation(value = "文件上传")
    @PostMapping ("/upload")
    public Result upload(@RequestBody MultipartFile file, @RequestParam("module") String module){

        try {

            String fileName = file.getOriginalFilename();
            InputStream inputStream = file.getInputStream();
            String url = fileService.upload(inputStream, module, fileName);

            return Result.ok().data("url",url).message("文件上传成功!!");
        } catch (IOException e) {
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR);
        }
    }

    @ApiOperation(value = "删除oss文件")
    @DeleteMapping("/remove")
    public Result remove(@RequestParam("url") String url){

        fileService.remove(url);
        return Result.ok().message("删除文件成功!!");
    }

}
