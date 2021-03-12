package com.example.miaosha_7.redis;

/**
 * @author yuhao
 * @date: 2021/3/8
 * @description:
 */
public class MiaoshaUserKey extends BasePrefix {

	public static final int TOKEN_EXPIRE = 3600*24 * 2;

	private MiaoshaUserKey(int expireSeconds,String prefix){
		super(expireSeconds,prefix);
	}

	public static MiaoshaUserKey token = new MiaoshaUserKey(TOKEN_EXPIRE,"tk");

	// 对象缓存
	public static MiaoshaUserKey getById= new MiaoshaUserKey(0,"id");
}
