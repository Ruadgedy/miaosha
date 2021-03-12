package com.example.miaosha_7.redis;

/**
 * @author yuhao
 * @date: 2021/3/8
 * @description:
 */
public class UserKey extends BasePrefix{
	private UserKey(String prefix) {
		super(prefix);
	}

	public static UserKey getById = new UserKey("id");

	public static UserKey getByName = new UserKey("name");
}
