package com.srb.sms;

import com.srb.sms.util.SmsProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SmsTest {

    @Test
    public void testSms(){
        System.out.println(SmsProperties.KEY_ID);
        System.out.println(SmsProperties.SIGN_NAME);
        System.out.println(SmsProperties.KEY_SECRET);
        System.out.println(SmsProperties.REGION_Id);
        System.out.println(SmsProperties.TEMPLATE_CODE);
    }

}
