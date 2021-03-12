package com.example.miaosha_7.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yuhao
 * @date: 2021/3/10
 * @description:
 */
@Configuration
public class MQConfig {

	public static final String QUEUE = "queue";

	public static final String MIAOSHA_QUEUE = "miaosha.queue";

	@Bean
	public Queue queue(){
		return new Queue(MIAOSHA_QUEUE,true);
	}
}
