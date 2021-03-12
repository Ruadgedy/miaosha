package com.example.miaosha_7.config;

import com.example.miaosha_7.domain.MiaoshaUser;

/**
 * @author yuhao
 * @date: 2021/3/11
 * @description:
 */
public class UserContext {

	private static ThreadLocal<MiaoshaUser> userHolder = new ThreadLocal<>();

	public static void setUser(MiaoshaUser user){
		userHolder.set(user);
	}

	public static MiaoshaUser getUser(){
		return userHolder.get();
	}
}
