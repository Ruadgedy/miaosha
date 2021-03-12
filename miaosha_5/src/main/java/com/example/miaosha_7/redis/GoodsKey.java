package com.example.miaosha_7.redis;

/**
 * @author yuhao
 * @date: 2021/3/9
 * @description:
 */
public class GoodsKey extends BasePrefix {

	// 给页面设置60秒的缓存时间
	public static GoodsKey getGoodsList = new GoodsKey(60,"gl");

	public static GoodsKey getGoodsDetail = new GoodsKey(60, "gd");

	public GoodsKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}
}
