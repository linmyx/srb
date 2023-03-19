package com.srb.sms.controller.api;

import com.srb.common.exception.Assert;
import com.srb.common.result.ResponseEnum;
import com.srb.common.result.Result;
import com.srb.common.utils.RandomUtils;
import com.srb.common.utils.RegexValidateUtils;
import com.srb.sms.client.CoreUserInfoClient;
import com.srb.sms.service.SmsService;
import com.srb.sms.util.SmsProperties;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/sms")
@Api(tags = "短息管理")
@Slf4j
public class ApiSmsController {

    @Resource
    private SmsService smsService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private CoreUserInfoClient coreUserInfoClient;

    @ApiOperation(value = "阿里云发送手机短息验证码")
    @GetMapping("/send/{mobile}")
    public Result send(@PathVariable String mobile){
        //校验手机号码
        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
        //判断手机号是否合法
        Assert.isTrue(RegexValidateUtils.checkCellphone(mobile),ResponseEnum.MOBILE_ERROR);

        boolean result = coreUserInfoClient.checkMobile(mobile);
       Assert.isTrue(result==false,ResponseEnum.MOBILE_EXIST_ERROR);

        //随机生成验证码
        String code = RandomUtils.getFourBitRandom();
        Map<String,Object> hashMap = new HashMap<>();
        hashMap.put("code", code);
        //发送验证码
        smsService.send(mobile, SmsProperties.TEMPLATE_CODE,hashMap);
        //将验证码存入到redis
        redisTemplate.opsForValue().set("srb:sms:code:"+mobile,code,5, TimeUnit.MINUTES);
        log.info("你的验证码为:{}",code);
        return Result.ok().message("验证码发送成功!");
    }

    @ApiOperation(value = "QQ邮箱发送验证码")
    @GetMapping("/sendEmail/{targetEmail}")
    public Result sendEmail(@PathVariable String targetEmail){

        Assert.notNull(targetEmail, ResponseEnum.QQ_EMAIL_ISNULL);

        //Assert.isTrue(RegexValidateUtils.checkEmail(targetEmail),ResponseEnum.QQ_EMAIL_CODE);

        //随机生成验证码
        String code = RandomUtils.getFourBitRandom();

        smsService.sendEmail(targetEmail, code);

        redisTemplate.opsForValue().set("srb:sms:email:"+targetEmail,code,5, TimeUnit.MINUTES);
        log.info("你的验证码为:{}",code);
        return Result.ok().message("邮箱验证码发送成功!");
    }

}
