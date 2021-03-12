package com.example.miaosha_7.redis;

/**
 * @author yuhao
 * @date: 2021/3/10
 * @description:
 */
public class MiaoshaKey extends BasePrefix {

	public static KeyPrefix getMiaoshaPath = new MiaoshaKey(60,"mp");

	public MiaoshaKey(String prefix) {
		super(prefix);
	}

	public MiaoshaKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}

	public static MiaoshaKey isGoodsOver = new MiaoshaKey("go");
	public static MiaoshaKey getMiaoshaVerifyCode = new MiaoshaKey(300, "vc");
}
