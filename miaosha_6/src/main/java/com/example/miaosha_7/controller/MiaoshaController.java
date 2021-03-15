package com.example.miaosha_7.controller;

import com.example.miaosha_7.domain.MiaoshaOrder;
import com.example.miaosha_7.domain.MiaoshaUser;
import com.example.miaosha_7.rabbitmq.MQSender;
import com.example.miaosha_7.rabbitmq.MiaoshaMessage;
import com.example.miaosha_7.redis.GoodsKey;
import com.example.miaosha_7.redis.RedisService;
import com.example.miaosha_7.result.CodeMsg;
import com.example.miaosha_7.result.Result;
import com.example.miaosha_7.service.GoodsService;
import com.example.miaosha_7.service.MiaoshaService;
import com.example.miaosha_7.service.OrderService;
import com.example.miaosha_7.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yuhao
 * @date: 2021/3/9
 * @description:
 */
@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

	@Autowired
	GoodsService goodsService;

	@Autowired
	OrderService orderService;

	@Autowired
	MiaoshaService miaoshaService;

	@Autowired
	RedisService redisService;

	@Autowired
	MQSender mqSender;

	// 设置某一商品是否秒杀完。如果某一时间检测发现该商品库存为0，则put(goodsId, true);
	// 以后到来的请求只会先去检查该map。如果发现自己要秒杀的goodId没库存了，就不会进行以后的操作
	private Map<Long,Boolean> localOverMap = new HashMap<>();

	// 这里涉及到高并发下的一致性问题
	// 当启动1w个线程时，会出现库存为负的情况
	// 出现该问题的原因也很简单，当多个线程同时跑到判断商品库存的代码处时，会有多个线程判断库存==1，然后执行减库存的操作
	@RequestMapping(value = "/do_miaosha", method = RequestMethod.POST)
	@ResponseBody
	public Result<Integer> list(Model model, MiaoshaUser user, @RequestParam("goodsId") long goodsId) {
		model.addAttribute("user", user);
		if (user == null) return Result.error(CodeMsg.SESSION_ERROR);

		// * 优化之前
//		// 判断商品库存
//		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
//		int stock = goods.getStockCount();
//		if (stock <= 0){ // 库存小于0，则秒杀结束
//			return Result.error(CodeMsg.MIAO_SHA_OVER);
//		}
//		// 判断是否秒杀到了
//		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
//		if (order != null) {  // 已经存在秒杀订单信息，则说明重复秒杀
//			return Result.error(CodeMsg.REPEAT_MIAOSHA);
//		}
//		// 做秒杀业务（事务）
//		// 1. 减库存  2. 下订单  3. 写如秒杀订单
//		OrderInfo orderInfo = miaoshaService.miaosha(user,goods);
//		return Result.success(orderInfo);

		// *优化过程
		// *内存标记，减少对Redis的访问
		boolean over = localOverMap.get(goodsId);
		if (over){
			return Result.error(CodeMsg.MIAO_SHA_OVER);
		}

		// 预减库存
		// ! 这里有一个问题：如果用户预减库存后发现自己已经秒杀过了，此时直接返回失败，但预减的库存会凭空消失。这可能会导致错误
		long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, goodsId + "");
		if (stock < 0) {
			localOverMap.put(goodsId,true);
			return Result.error(CodeMsg.MIAO_SHA_OVER);
		}
		// 判断是否已经秒杀过了
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
		if (order != null) {
			return Result.error(CodeMsg.REPEAT_MIAOSHA);
		}
		// 入队
		MiaoshaMessage mm = new MiaoshaMessage();
		mm.setUser(user);
		mm.setGoodsId(goodsId);
		mqSender.sendMiaoshaMessage(mm);
		return Result.success(0); // 0代表排队中
	}

	/**
	 * 系统初始化之后执行的操作
	 *
	 * @throws Exception
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		if (goodsList == null) {
			return;
		}
		for (GoodsVo goods : goodsList) {
			redisService.set(GoodsKey.getMiaoshaGoodsStock, goods.getId() + "", goods.getStockCount());
			localOverMap.put(goods.getId(),false);
		}
	}

	/**
	 * @param model
	 * @param user
	 * @param goodsId
	 * @return  orderId -> 成功   -1 -> 秒杀失败      0 -> 排队中
	 */
	@RequestMapping(value = "/result", method = RequestMethod.GET)
	@ResponseBody
	public Result<Long> miaoshaResult(Model model, MiaoshaUser user, @RequestParam("goodsId") long goodsId) {
		model.addAttribute("user", user);
		if (user == null) return Result.error(CodeMsg.SESSION_ERROR);
		long result = miaoshaService.getMiaoshaResult(user,goodsId);
		return Result.success(result);
	}
}
