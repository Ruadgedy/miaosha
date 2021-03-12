package com.example.miaosha_7.service;

import com.example.miaosha_7.dao.GoodsDao;
import com.example.miaosha_7.domain.MiaoshaGoods;
import com.example.miaosha_7.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yuhao
 * @date: 2021/3/8
 * @description:
 */
@Service
public class GoodsService {

	@Autowired
	GoodsDao goodsDao;

	public List<GoodsVo> listGoodsVo(){
		return goodsDao.listGoodsVo();
	}

	public GoodsVo getGoodsVoByGoodsId(long goodsId) {
		return goodsDao.getGoodsVoByGoodsId(goodsId);
	}

	public void reduceStock(GoodsVo goods){
		// 1. 减库存 2. 下订单  3. 写入秒杀订单
		MiaoshaGoods g = new MiaoshaGoods();
		g.setGoodsId(goods.getId());
		goodsDao.reduceStock(g);
	}
}
