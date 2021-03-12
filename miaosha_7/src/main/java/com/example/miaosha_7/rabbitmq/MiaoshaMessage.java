package com.example.miaosha_7.rabbitmq;

import com.example.miaosha_7.domain.MiaoshaUser;

/**
 * @author yuhao
 * @date: 2021/3/10
 * @description:
 */
public class MiaoshaMessage {

	private MiaoshaUser user;

	private long goodsId;

	public MiaoshaUser getUser() {
		return user;
	}

	public void setUser(MiaoshaUser user) {
		this.user = user;
	}

	public long getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(long goodsId) {
		this.goodsId = goodsId;
	}
}
