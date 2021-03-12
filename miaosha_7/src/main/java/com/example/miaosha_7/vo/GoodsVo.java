package com.example.miaosha_7.vo;

import com.example.miaosha_7.domain.Goods;

import java.util.Date;

/**
 * @author yuhao
 * @date: 2021/3/8
 * @description:
 */
public class GoodsVo extends Goods {

	public double getMiaoshaPrice() {
		return miaoshaPrice;
	}

	public void setMiaoshaPrice(double miaoshaPrice) {
		this.miaoshaPrice = miaoshaPrice;
	}

	private double miaoshaPrice;
	private Integer stockCount;
	private Date startDate;
	private Date endDate;

	public Integer getStockCount() {
		return stockCount;
	}

	public void setStockCount(Integer stockCount) {
		this.stockCount = stockCount;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
