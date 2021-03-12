package com.example.miaosha_7.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author yuhao
 * @date: 2021/3/8
 * @description:
 */
@Component
public class RedisPoolFactory {

	@Bean
	public JedisPool jedisPoolFactory(){
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		JedisPool jp = new JedisPool(poolConfig, "localhost", 6379, 3000, null, 0);
		return jp;
	}
}
