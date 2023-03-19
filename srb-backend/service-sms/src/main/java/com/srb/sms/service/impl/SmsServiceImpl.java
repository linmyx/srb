package com.srb.sms.service.impl;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;
import com.srb.common.exception.Assert;
import com.srb.common.exception.BusinessException;
import com.srb.common.result.ResponseEnum;
import com.srb.sms.service.SmsService;
import com.srb.sms.util.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public void send(String mobile, String templateCode, Map<String, Object> params) {

        //创建远程连接客户端对象
        DefaultProfile profile = DefaultProfile.getProfile(
                SmsProperties.REGION_Id,
                SmsProperties.KEY_ID,
                SmsProperties.KEY_SECRET);

        IAcsClient client = new DefaultAcsClient(profile);

        //创建远程连接的请求参数

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", SmsProperties.REGION_Id);
        request.putQueryParameter("PhoneNumbers", mobile);
        request.putQueryParameter("SignName", SmsProperties.SIGN_NAME);
        request.putQueryParameter("TemplateCode", templateCode);

        Gson gson = new Gson();
        String json = gson.toJson(params);
        request.putQueryParameter("TemplateParam", json);

        try {
            //使用客户端对象携带请求对象发送请求并得到响应结果
            CommonResponse response = client.getCommonResponse(request);
            boolean success = response.getHttpResponse().isSuccess();
            //ALIYUN_RESPONSE_FAIL(-501, "阿里云响应失败"),
            Assert.isTrue(success, ResponseEnum.ALIYUN_RESPONSE_FAIL);

            String data = response.getData();
            HashMap<String, String> resultMap = gson.fromJson(data, HashMap.class);
            String code = resultMap.get("Code");
            String message = resultMap.get("Message");
            log.info("阿里云短信发送响应结果：");
            log.info("code：" + code);
            log.info("message：" + message);

            //ALIYUN_SMS_LIMIT_CONTROL_ERROR(-502, "短信发送过于频繁"),//业务限流
            Assert.notEquals("isv.BUSINESS_LIMIT_CONTROL", code, ResponseEnum.ALIYUN_SMS_LIMIT_CONTROL_ERROR);
            //ALIYUN_SMS_ERROR(-503, "短信发送失败"),//其他失败
            Assert.equals("OK", code, ResponseEnum.ALIYUN_SMS_ERROR);

        } catch (ServerException e) {
            log.error("阿里云短信发送SDK调用失败：");
            log.error("ErrorCode=" + e.getErrCode());
            log.error("ErrorMessage=" + e.getErrMsg());
            throw new BusinessException(ResponseEnum.ALIYUN_SMS_ERROR , e);
        } catch (ClientException e) {
            log.error("阿里云短信发送SDK调用失败：");
            log.error("ErrorCode=" + e.getErrCode());
            log.error("ErrorMessage=" + e.getErrMsg());
            throw new BusinessException(ResponseEnum.ALIYUN_SMS_ERROR , e);
        }
    }

    /**
     * 使用QQ邮箱获取验证码
     * @param targetEmail
     * @param authCode
     */
    @Override
    public void sendEmail(String targetEmail, String authCode) {
        try {
            SimpleEmail mail=new SimpleEmail();
            mail.setHostName("smtp.qq.com");
            mail.setAuthentication("linmengbk@qq.com","rtvuxpsmwetjgcfe");
            mail.setFrom("linmengbk@qq.com","凌梦验证码");
            mail.setSSLOnConnect(true);
            mail.addTo(targetEmail);
            mail.setSubject("尚融宝注册验证码");
            mail.setMsg("您的注册验证码为:"+authCode+"在一分钟之内有效");
            mail.send();
        } catch (EmailException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将充值成功短信保存到redis当中
     */
    public void redisSend(String mobile, Map<String, Object> params){
        String message = (String) params.get("code");
        redisTemplate.opsForValue().set("rm:mobile",message,5, TimeUnit.MINUTES);
    }
}
