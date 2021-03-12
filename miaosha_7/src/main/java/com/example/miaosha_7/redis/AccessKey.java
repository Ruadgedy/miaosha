package com.example.miaosha_7.redis;

/**
 * @author yuhao
 * @date: 2021/3/11
 * @description:
 */
public class AccessKey extends BasePrefix {
	public AccessKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}

	public static AccessKey accessKey = new AccessKey(5,"ak");

	public static AccessKey withExpire(int expire){
		return new AccessKey(expire,"ak");
	}
}
