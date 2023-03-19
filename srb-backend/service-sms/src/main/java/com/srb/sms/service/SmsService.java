package com.srb.sms.service;

import java.util.Map;

public interface SmsService {
    void send(String mobile, String templateCode, Map<String,Object> params);

    void sendEmail(String targetEmail,String authCode);

    void redisSend(String mobile, Map<String, Object> params);

}
