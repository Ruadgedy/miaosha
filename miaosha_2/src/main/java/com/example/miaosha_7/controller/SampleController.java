package com.example.miaosha_7.controller;

import com.example.miaosha_7.domain.User;
import com.example.miaosha_7.redis.RedisService;
import com.example.miaosha_7.redis.UserKey;
import com.example.miaosha_7.result.Result;
import com.example.miaosha_7.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yuhao
 * @date: 2021/3/8
 * @description:
 */
@RestController
@RequestMapping("/demo")
public class SampleController {

	@Autowired
	UserService userService;

	@Autowired
	RedisService redisService;

	@GetMapping("/db/get")
	public Result<User> doGet(){
		User user = userService.getById(1);
		return Result.success(user);
	}

	@RequestMapping("/redis/get")
	public Result<User> redisGet() {
		User  user  = redisService.get(UserKey.getById, ""+2, User.class);
		return Result.success(user);
	}

	@RequestMapping("/redis/set")
	public Result<Boolean> redisSet() {
		User user  = new User();
		user.setId(1);
		user.setName("1111");
		redisService.set(UserKey.getById, ""+2, user);//UserKey:id2
		return Result.success(true);
	}

}
