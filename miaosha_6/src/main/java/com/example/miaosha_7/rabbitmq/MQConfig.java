package com.example.miaosha_7.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
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

	public static final String TOPIC_QUEUE1 = "topic.queue1";
	public static final String TOPIC_QUEUE2 = "topic.queue2";
	public static final String TOPIC_EXCHANGE = "topic.exchange";
	public static final String MIAOSHA_QUEUE = "miaosha.queue";

	/**
	 * Direct模式
	 * @return
	 */
	@Bean
	public Queue queue(){
		return new Queue(MIAOSHA_QUEUE,true);
	}

	@Bean
	public Queue topicQueue1(){
		return new Queue(TOPIC_QUEUE1,true);
	}

	@Bean
	public Queue topicQueue2(){
		return new Queue(TOPIC_QUEUE2,true);
	}

	@Bean
	TopicExchange topicExchange(){
		return new TopicExchange(TOPIC_EXCHANGE);
	}

	@Bean
	Binding topicBinding1(){
		return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with("topic.key1");
	}

	@Bean
	Binding topicBinding2(){
		return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with("topic.#");
	}
}
