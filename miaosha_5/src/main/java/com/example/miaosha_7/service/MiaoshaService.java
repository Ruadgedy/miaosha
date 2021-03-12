package com.example.miaosha_7.service;

import com.example.miaosha_7.domain.MiaoshaUser;
import com.example.miaosha_7.domain.OrderInfo;
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

	@Transactional
	public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
		// 1. 减库存 2. 下订单  3. 写入秒杀订单
		goodsService.reduceStock(goods);
		return orderService.createOrder(user,goods);
	}
}
