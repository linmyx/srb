package com.srb.sms.client;

import com.srb.common.result.Result;
import com.srb.sms.client.fallback.CoreUserInfoClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 远程调用服务组件
 */
@FeignClient(value = "service-core",fallback = CoreUserInfoClientFallback.class)
public interface CoreUserInfoClient{
    @GetMapping("/api/core/userInfo/checkMobile/{mobile}")
    boolean checkMobile(@PathVariable String mobile);
}
