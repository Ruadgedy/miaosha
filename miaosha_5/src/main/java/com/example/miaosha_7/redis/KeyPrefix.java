package com.example.miaosha_7.redis;

/**
 * @author yuhao
 * @date: 2021/3/8
 * @description:
 */
public interface KeyPrefix {

	public int expireSeconds();

	public String getPrefix();
}
