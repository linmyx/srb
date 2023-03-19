package com.srb.core;

import com.srb.common.result.Result;
import com.srb.core.mapper.DictMapper;
import com.srb.core.pojo.entity.Dict;
import com.srb.core.service.DictService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private DictMapper dictMapper;

    @Resource
    private DictService dictService;
    @Test
    public void test(){
        Result dictParentById = dictService.getDictParentById(1);
        Map<String, Object> data = dictParentById.getData();
        List<Dict> row = (List) data.get("row");
        redisTemplate.opsForValue().set("dict",row,5, TimeUnit.MINUTES);
    }

}
