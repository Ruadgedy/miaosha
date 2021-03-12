package com.example.miaosha_7.controller;

import com.example.miaosha_7.domain.MiaoshaUser;
import com.example.miaosha_7.redis.RedisService;
import com.example.miaosha_7.service.MiaoshaUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

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
		return "goods_list";
	}
}
