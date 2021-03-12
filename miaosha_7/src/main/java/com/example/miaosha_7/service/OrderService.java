package com.example.miaosha_7.service;

import com.example.miaosha_7.dao.OrderDao;
import com.example.miaosha_7.domain.MiaoshaOrder;
import com.example.miaosha_7.domain.MiaoshaUser;
import com.example.miaosha_7.domain.OrderInfo;
import com.example.miaosha_7.redis.OrderKey;
import com.example.miaosha_7.redis.RedisService;
import com.example.miaosha_7.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author yuhao
 * @date: 2021/3/9
 * @description:
 */
@Service
public class OrderService {

	@Autowired
	OrderDao orderDao;

	@Autowired
	RedisService redisService;

	public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(long userId, long goodsId) {
		return redisService.get(OrderKey.getMiaoshaOrderByUidGid, "" + userId + "_" + goodsId, MiaoshaOrder.class);
	}

	@Transactional
	public OrderInfo createOrder(MiaoshaUser user, GoodsVo goods) {
		OrderInfo orderInfo = new OrderInfo();
		orderInfo.setCreateDate(new Date());
		orderInfo.setDeliveryAddrId(0L);
		orderInfo.setGoodsCount(1);
		orderInfo.setGoodsId(goods.getId());
		orderInfo.setGoodsName(goods.getGoodsName());
		orderInfo.setGoodsPrice(goods.getMiaoshaPrice());
		orderInfo.setOrderChannel(1);
		orderInfo.setStatus(0); // 0代表未支付
		orderInfo.setUserId(user.getId());
		orderDao.insert(orderInfo);
		MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
		miaoshaOrder.setGoodsId(goods.getId());
		miaoshaOrder.setOrderId(orderInfo.getId());
		miaoshaOrder.setUserId(user.getId());
		orderDao.insertMiaoshaOrder(miaoshaOrder);

		// 下单成功后会将秒杀订单写入到缓存中。这样当判断用户是否重复下单时就不需要去数据库中寻找
		redisService.set(OrderKey.getMiaoshaOrderByUidGid,"" + user.getId() + "_" + goods.getId(), miaoshaOrder);
		return orderInfo;
	}

	public OrderInfo getOrderById(long orderId) {
		return orderDao.getOrderById(orderId);
	}
}
