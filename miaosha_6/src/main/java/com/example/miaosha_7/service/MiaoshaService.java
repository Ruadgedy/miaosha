package com.example.miaosha_7.service;

import com.example.miaosha_7.domain.MiaoshaOrder;
import com.example.miaosha_7.domain.MiaoshaUser;
import com.example.miaosha_7.domain.OrderInfo;
import com.example.miaosha_7.redis.MiaoshaKey;
import com.example.miaosha_7.redis.RedisService;
import com.example.miaosha_7.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author yuhao
 * @date: 2021/3/9
 * @description:
 */
@Service
public class MiaoshaService {

	@Autowired
	GoodsService goodsService;

	@Autowired
	OrderService orderService;

	@Autowired
	RedisService redisService;

	@Transactional
	public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
		// 1. 减库存 2. 下订单  3. 写入秒杀订单
		boolean success = goodsService.reduceStock(goods);
		if (success){
			return orderService.createOrder(user,goods);
		}else {
			// 设置一个标志，表示商品已经被秒杀完了
			setGoodsOver(goods.getId());
			return null;
		}
	}

	/**
	 * 获取秒杀结果
	 * @param user
	 * @param goodsId
	 * @return
	 */
	public long getMiaoshaResult(MiaoshaUser user, long goodsId) {
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
		if (order != null){ // 秒杀成功
			return order.getId();
		}else{
			// 分两种情况： 1. 请求还在队列中未被处理完  2. 秒杀失败
			boolean isOver = getGoodsOver(goodsId);
			if (isOver){ // 如果商品已经被卖完了但是还没抢到，则没抢到
				return -1;
			}else{
				return 0; // 说明请求在队列中还在处理，返回轮询
			}
		}
	}

	// 设置商品被卖完了
	private void setGoodsOver(Long goodsId) {
		redisService.set(MiaoshaKey.isGoodsOver,""+goodsId, true);
	}

	// 获取商品是否被卖完了  如果key存在，则商品被卖完了
	private boolean getGoodsOver(long goodsId) {
		return redisService.exists(MiaoshaKey.isGoodsOver,""+goodsId);
	}
}
