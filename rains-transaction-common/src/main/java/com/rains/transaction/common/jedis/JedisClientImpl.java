/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.rains.transaction.common.jedis;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/10/26 14:50
 * @since JDK 1.8
 */
public class JedisClientImpl implements JedisClient {

    private RedisTemplate redisTemplate;


    public JedisClientImpl(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String set(String key, String value) {
        redisTemplate.opsForValue().set(key,value);
        return "OK";
    }

    @Override
    public String set(String key, byte[] value) {
        redisTemplate.opsForValue().set(key.getBytes(),value);

        return "OK";
    }

    @Override
    public Long del(String... keys) {
       return redisTemplate.delete(Arrays.asList(keys));


    }

    @Override
    public String get(String key) {
        return (String)redisTemplate.opsForValue().get(key);


    }

    @Override
    public byte[] get(byte[] key) {
       return(byte[]) redisTemplate.opsForValue().get(key);

    }

    @Override
    public Set<byte[]> keys(byte[] pattern) {
       return redisTemplate.keys(pattern);


    }

    @Override
    public Set<String> keys(String key) {
        return redisTemplate.keys(key);

    }

    @Override
    public Long hset(String key, String item, String value) {
        redisTemplate.opsForHash().put(key,item,value);

        return 1L;
    }

    @Override
    public String hget(String key, String item) {
        return (String) redisTemplate.opsForHash().get(key,item);
    }

    @Override
    public Long hdel(String key, String item) {
       return redisTemplate.opsForHash().delete(key,item);

    }

    @Override
    public Long incr(String key) {
       return redisTemplate.opsForValue().increment(key,1L);


    }

    @Override
    public Long decr(String key) {
        return  redisTemplate.opsForValue().increment(key,-1L);


    }

    @Override
    public Long expire(String key, int second) {
       Boolean b = redisTemplate.expire(key,second, TimeUnit.SECONDS);

        return b?1L:0L;
    }

    @Override
    public Set<String> zrange(String key, long start, long end) {
       return redisTemplate.opsForZSet().range(key,start,end);
    }


}
