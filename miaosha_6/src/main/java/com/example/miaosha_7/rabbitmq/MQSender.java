package com.example.miaosha_7.rabbitmq;

import com.example.miaosha_7.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.example.miaosha_7.rabbitmq.MQConfig.MIAOSHA_QUEUE;
import static com.example.miaosha_7.rabbitmq.MQConfig.QUEUE;

/**
 * @author yuhao
 * @date: 2021/3/10
 * @description:
 */
@Component
public class MQSender {

	@Autowired
	AmqpTemplate amqpTemplate;

	private static Logger logger = LoggerFactory.getLogger(MQSender.class);

	public void send(Object message){
		String msg = RedisService.beanToString(message);
		logger.info("send msg: " + msg);
		amqpTemplate.convertAndSend(QUEUE, msg);
	}

	public void sendMiaoshaMessage(MiaoshaMessage mm) {
		String msg = RedisService.beanToString(mm);
		logger.info("send message: " + msg);
		amqpTemplate.convertAndSend(MIAOSHA_QUEUE,msg);
	}
}
