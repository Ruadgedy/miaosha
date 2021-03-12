package com.example.miaosha_7.redis;

/**
 * @author yuhao
 * @date: 2021/3/10
 * @description:
 */
public class MiaoshaKey extends BasePrefix {

	public MiaoshaKey(String prefix) {
		super(prefix);
	}

	public static MiaoshaKey isGoodsOver = new MiaoshaKey("go");
}
