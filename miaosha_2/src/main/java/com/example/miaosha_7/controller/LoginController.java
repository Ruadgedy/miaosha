package com.example.miaosha_7.controller;

import com.example.miaosha_7.redis.RedisService;
import com.example.miaosha_7.result.Result;
import com.example.miaosha_7.service.MiaoshaUserService;
import com.example.miaosha_7.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * @author yuhao
 * @date: 2021/3/8
 * @description:
 */
@Controller
@RequestMapping("/login")
public class LoginController {

	@Autowired
	MiaoshaUserService userService;

	@Autowired
	RedisService redisService;

	/**
	 * 跳转到跳转页面
	 * @return
	 */
	@RequestMapping("/to_login")
	public String toLogin() {
		return "login";
	}

	@RequestMapping("/do_login")
	@ResponseBody
	public Result<Boolean> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
		//登录
		userService.login(response, loginVo);
		return Result.success(true);
	}
}
