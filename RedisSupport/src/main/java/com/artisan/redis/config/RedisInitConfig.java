package com.artisan.redis.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import java.util.HashSet;
import java.util.Set;

/**  
 *    
 * @author Fire Monkey 
 * @date 2018/3/12 下午6:28
 * redis初始化配置
 * 
 */ 
@Configuration
public class RedisInitConfig {

    /**
     * 日志打印对象
     */
    private Logger log = LoggerFactory.getLogger(RedisInitConfig.class);

    /**
     * 注入配置文件信息
     */
    @Autowired
    private RedisConfig redisConfig;

    
    /**  
     *    
     * @author Fire Monkey 
     * @date 2018/3/12 下午6:53
     * @return redis.clients.jedis.JedisPoolConfig
     * 初始化连接池配置对象
     *
     */ 
    @Bean(value = "jedisPoolConfig")
    public JedisPoolConfig initJedisPoolConfig(){
        log.info("JedisPool initialize start ...");
        JedisPoolConfig config = new JedisPoolConfig();

        //最大总量
        config.setMaxTotal(redisConfig.getMaxTotal());
        //设置最大空闲数量
        config.setMaxIdle(redisConfig.getMaxIdle());
        //设置最小空闲数量
        config.setMinIdle(redisConfig.getMinIdle());
        //常规配置
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        log.info("JedisPool initialize end ...");
        return config;
    }

    /**
     *
     * @author Fire Monkey
     * @date 下午7:20
     * @return redis.clients.jedis.JedisSentinelPool
     * 生成JedisSentinelPool并且放入Spring容器
     *
     */
    @Bean(value = "sentinelPool")
    public JedisSentinelPool initJedisPool(@Qualifier(value = "jedisPoolConfig") JedisPoolConfig jedisPoolConfig){

        Set<String>  nodeSet = new HashSet<>();
        //获取到节点信息
        String nodeString = redisConfig.getNodes();
        //判断字符串是否为空
        if(nodeString == null || "".equals(nodeString)){
            log.error("RedisSentinelConfiguration initialize error nodeString is null");
            throw new RuntimeException("RedisSentinelConfiguration initialize error nodeString is null");
        }
        String[] nodeArray = nodeString.split(",");
        //判断是否为空
        if(nodeArray == null || nodeArray.length == 0){
            log.error("RedisSentinelConfiguration initialize error nodeArray is null");
            throw new RuntimeException("RedisSentinelConfiguration initialize error nodeArray is null");
        }
        //循环注入至Set中
        for(String node : nodeArray){
            log.info("Read node : {}。" , node);
            nodeSet.add(node);
        }
        //创建连接池对象
        JedisSentinelPool jedisPool = new JedisSentinelPool(redisConfig.getMasterName() ,nodeSet ,jedisPoolConfig ,redisConfig.getTimeout() , redisConfig.getPassword());

        return jedisPool;

    }

    
}
