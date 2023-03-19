package com.srb.sms.receiver;

import com.srb.base.Dto.SmsDTO;
import com.srb.mq.constant.MQConst;
import com.srb.mq.service.MQService;
import com.srb.sms.service.SmsService;
import com.srb.sms.util.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;

@Component
@Slf4j
public class SmsReceiver {

    @Resource
    private SmsService smsService;


    /**
     * MQ的消息监听
     * @param smsDTO
     */
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(value = MQConst.QUEUE_SMS_ITEM),
//            exchange = @Exchange(value = MQConst.EXCHANGE_TOPIC_SMS),
//            key = {MQConst.ROUTING_SMS_ITEM}
//    ))
//    public void send(SmsDTO smsDTO){
//        log.info("消息监听");
//        HashMap<String,Object> hashMap = new HashMap<>();
//        hashMap.put("code",smsDTO.getMessage());
//        smsService.send(smsDTO.getMobile(), SmsProperties.TEMPLATE_CODE,hashMap);
//    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConst.QUEUE_SMS_ITEM,durable = "true"),
            exchange = @Exchange(value = MQConst.EXCHANGE_TOPIC_SMS),
            key = {MQConst.ROUTING_SMS_ITEM}
    ))
    public void redisSend(SmsDTO smsDTO){
        log.info("redis消息监听");
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("code",smsDTO.getMessage());
        smsService.redisSend(smsDTO.getMobile(),hashMap);
    }

}
