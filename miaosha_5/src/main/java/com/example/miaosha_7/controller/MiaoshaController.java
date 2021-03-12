package com.example.miaosha_7.controller;

import com.example.miaosha_7.domain.MiaoshaOrder;
import com.example.miaosha_7.domain.MiaoshaUser;
import com.example.miaosha_7.domain.OrderInfo;
import com.example.miaosha_7.result.CodeMsg;
import com.example.miaosha_7.result.Result;
import com.example.miaosha_7.service.GoodsService;
import com.example.miaosha_7.service.MiaoshaService;
import com.example.miaosha_7.service.OrderService;
import com.example.miaosha_7.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * @author yuhao
 * @date: 2021/3/9
 * @description:
 */
@Controller
@RequestMapping("/miaosha")
public class MiaoshaController {

	@Autowired
	GoodsService goodsService;

	@Autowired
	OrderService orderService;

	@Autowired
	MiaoshaService miaoshaService;

	// 这里涉及到高并发下的一致性问题
	// 当启动1w个线程时，会出现库存为负的情况
	// 出现该问题的原因也很简单，当多个线程同时跑到判断商品库存的代码处时，会有多个线程判断库存==1，然后执行减库存的操作
	@RequestMapping(value = "/do_miaosha",method = RequestMethod.POST)
	@ResponseBody
	public Result<OrderInfo> list(Model model, MiaoshaUser user, @RequestParam("goodsId") long goodsId){
		model.addAttribute("user",user);
		if (user == null) return Result.error(CodeMsg.SESSION_ERROR);
		// 判断商品库存
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		int stock = goods.getStockCount();
		if (stock <= 0){ // 库存小于0，则秒杀结束
			return Result.error(CodeMsg.MIAO_SHA_OVER);
		}
		// 判断是否秒杀到了
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
		if (order != null) {  // 已经存在秒杀订单信息，则说明重复秒杀
			return Result.error(CodeMsg.REPEAT_MIAOSHA);
		}
		// 做秒杀业务（事务）
		// 1. 减库存  2. 下订单  3. 写如秒杀订单
		OrderInfo orderInfo = miaoshaService.miaosha(user,goods);
		return Result.success(orderInfo);
	}
}
