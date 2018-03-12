package com.artisan.redis.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.exceptions.JedisConnectionException;


/**  
 *    
 * @author Fire Monkey 
 * @date 2018/3/12 下午7:32
 * Reids工具通过Jedis实现与Redis交互
 * 
 */ 
@Component
public class RedisService {


    /**
     * 日志打印对象
     */
    private static Logger log = Logger.getLogger(RedisService.class);


    /**
     * Jedis对象池 所有Jedis对象均通过该池租赁获取
     */
    private static JedisSentinelPool sentinelPool;

    /**
     * 缓存数据成功
     */
    private final static String CACHE_INFO_SUCCESS = "OK";


    /**  
     *    
     * @author Fire Monkey 
     * @date 2018/3/12 下午7:34
     * 注入JedisSentinelPool
     *
     */ 
    @Autowired
    public  void setSentinelPool(JedisSentinelPool sentinelPool) {
        RedisService.sentinelPool = sentinelPool;
    }



    /**  
     *    
     * @author Fire Monkey 
     * @date 2018/3/12 下午7:35
     * @return redis.clients.jedis.Jedis
     * 获取到Jedis
     *
     */ 
    private static Jedis getJedis()  {
        Jedis jedis;
        try {
            jedis = sentinelPool.getResource();
            return  jedis;
        } catch (JedisConnectionException e) {
            log.error("获取Redis 异常", e);
            throw e;
        }
    }

    /**
     *
     * @author Fire Monkey
     * @date 2018/3/12 下午7:36
     * 缓存String类型的数据,数据不过期
     *
     */
    public static boolean setString(String key, String value) throws Exception {
        return setString(key, value, -1);
    }


    /**
     *
     * @author Fire Monkey
     * @date 2018/3/12 下午7:40
     * 缓存String类型的数据并设置超时时间
     *
     */
    public static boolean setString(String key, String value, int timeout) throws Exception {
        String result;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.set(key, value);
            if (timeout != -1) {
                jedis.expire(key, timeout);
            }
            if (CACHE_INFO_SUCCESS.equals(result)) {
                return true;
            } else {
                return  false;
            }
        } finally {
            releaseRedis(jedis);
        }
    }

    /**
     *
     * @author Fire Monkey
     * @date 2018/3/12 下午7:46
     * 获取String类型的数据
     *
     */
    public static  String getString(String key) throws Exception {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.get(key);
        } catch (Exception e) {
            throw e;
        } finally {
            releaseRedis(jedis);
        }
    }


    /**  
     *    
     * @author Fire Monkey 
     * @date 2018/3/12 下午7:49
     * @return void
     * 释放Jedis
     *
     */ 
    public static void releaseRedis(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }


    /**  
     *    
     * @author Fire Monkey 
     * @date 2018/3/12 下午7:55
     * @return boolean
     * 通过key删除缓存中数据
     *
     */ 
    public static boolean del(String key) throws Exception {
        Long num;
        Jedis jedis = null;
        Boolean result = false;
        try {
            jedis = getJedis();
            num = jedis.del(key);
            if (num.equals(1L)) {
                result = true;
            }
        }finally {
            releaseRedis(jedis);
        }
        return result;
    }
    
}
