package com.example.miaosha_7.redis;

/**
 * @author yuhao
 * @date: 2021/3/8
 * @description:
 */
public abstract class BasePrefix implements KeyPrefix {
	private int expireSeconds; // 过期时间
	private String prefix;  // 前缀

	// 0代表永不过期
	public BasePrefix(String prefix){
		this(0, prefix);
	}

	public BasePrefix(int expireSeconds, String prefix){
		this.expireSeconds = expireSeconds;
		this.prefix = prefix;
	}

	@Override
	public int expireSeconds() {
		return expireSeconds;
	}

	/**
	 * 为了区分各个板块，前缀采用   类名+prefix  的方式
	 * @return
	 */
	@Override
	public String getPrefix() {
		return getClass().getSimpleName() + ":" + prefix;
	}
}
