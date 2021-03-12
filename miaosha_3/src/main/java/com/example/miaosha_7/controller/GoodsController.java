package com.example.miaosha_7.controller;

import com.example.miaosha_7.domain.MiaoshaUser;
import com.example.miaosha_7.redis.RedisService;
import com.example.miaosha_7.service.GoodsService;
import com.example.miaosha_7.service.MiaoshaUserService;
import com.example.miaosha_7.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author yuhao
 * @date: 2021/3/8
 * @description:
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {

	@Autowired
	MiaoshaUserService userService;

	@Autowired
	RedisService redisService;

	@Autowired
	GoodsService goodsService;

	// 低级版，每次都要去获取cookie，然后根据cookie去redis中拿到用户数据
	// 代码有大量冗余
//	@RequestMapping("/to_list")
//	public String list(HttpServletResponse response, Model model,
//	                   @CookieValue(value = MiaoshaUserService.COOKIE_NAME_TOKEN,required = false)String cookieToken,
//	                   @RequestParam(value = MiaoshaUserService.COOKIE_NAME_TOKEN,required = false)String paramToken) {
//		if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)){ // 都为空，则说明用户未登录，先去登录
//			return "login";
//		}
//		String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
//		MiaoshaUser user = userService.getByToken(response, token);
//		model.addAttribute("user",user);
//		return "goods_list";
//	}

	@RequestMapping("/to_list")
	public String list(Model model, MiaoshaUser user){
		model.addAttribute("user",user);
		// 查询商品列表
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		model.addAttribute("goodsList",goodsList);
		return "goods_list";
	}

	@RequestMapping("/to_detail/{goodsId}")
	public String detail(Model model, MiaoshaUser user, @PathVariable("goodsId") long goodsId){
		model.addAttribute("user",user);

		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		model.addAttribute("goods", goods);

		long startAt = goods.getStartDate().getTime();
		long endAt = goods.getEndDate().getTime();
		long now = System.currentTimeMillis();

		int miaoshaStatus = 0;
		int remainSeconds = 0;

		if (now < startAt){ // 秒杀没开始，倒计时
			miaoshaStatus = 0;
			remainSeconds = (int) ((startAt - now) / 1000);
		}else if (now > endAt){ // 秒杀已经结束
			miaoshaStatus = 2;
			remainSeconds = -1;
		}else { // 秒杀正在进行中
			miaoshaStatus = 1;
			remainSeconds = 0;
		}

		model.addAttribute("miaoshaStatus", miaoshaStatus);
		model.addAttribute("remainSeconds", remainSeconds);
		return "goods_detail";
	}
}
