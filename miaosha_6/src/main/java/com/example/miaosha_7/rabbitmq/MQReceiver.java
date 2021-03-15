package com.example.miaosha_7.rabbitmq;

import com.example.miaosha_7.domain.MiaoshaOrder;
import com.example.miaosha_7.domain.MiaoshaUser;
import com.example.miaosha_7.domain.OrderInfo;
import com.example.miaosha_7.redis.RedisService;
import com.example.miaosha_7.service.GoodsService;
import com.example.miaosha_7.service.MiaoshaService;
import com.example.miaosha_7.service.OrderService;
import com.example.miaosha_7.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuhao
 * @date: 2021/3/10
 * @description:
 */
@Component
public class MQReceiver {

	@Autowired
	GoodsService goodsService;

	@Autowired
	OrderService orderService;

	@Autowired
	MiaoshaService miaoshaService;

	private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

	@RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
	public void receive(String message){
		log.info("receive msg: " + message);
		MiaoshaMessage mm = RedisService.stringToBean(message, MiaoshaMessage.class);
		MiaoshaUser user = mm.getUser();
		long goodsId = mm.getGoodsId();

		// 查库存
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		int stock = goods.getStockCount();
		if (stock <= 0){
			return;
		}
		// 判断是否秒杀到了
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
		if (order != null) {  // 已经存在秒杀订单信息，则说明重复秒杀
			return ;
		}
		// 减库存，下订单，写入秒杀订单
		OrderInfo orderInfo = miaoshaService.miaosha(user,goods);
	}

	@RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
	public void receiveTopic1(String msg){
		log.info("topic queue1:" + msg);
	}

	@RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
	public void receiveTopic2(String msg){
		log.info("topic queue2:" + msg);
	}
}
