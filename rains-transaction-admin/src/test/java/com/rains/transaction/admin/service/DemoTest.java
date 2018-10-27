package com.rains.transaction.admin.service;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

/**
 * @author dourx
 * 2018年 10 月  27日  14:29
 * @version V1.0
 * TODO
 */
@RunWith(SpringRunner.class)
@DataRedisTest
public class DemoTest {
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    public void testDemo(){
        assertNotNull(redisTemplate);
        redisTemplate.opsForValue().set("test","test");
        String value =redisTemplate.opsForValue().get("test");
        assertNotNull(value);
    }
}
