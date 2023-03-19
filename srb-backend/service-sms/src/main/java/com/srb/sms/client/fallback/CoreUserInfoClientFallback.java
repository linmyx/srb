package com.srb.sms.client.fallback;

import com.srb.sms.client.CoreUserInfoClient;
import org.springframework.stereotype.Service;

@Service
public class CoreUserInfoClientFallback implements CoreUserInfoClient {
    /**
     * 对远程调用判断验证码进行远程熔断
     * @param mobile
     * @return
     */
    @Override
    public boolean checkMobile(String mobile) {
        return false;
    }
}
