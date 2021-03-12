package com.example.miaosha_7.dao;

import com.example.miaosha_7.domain.MiaoshaGoods;
import com.example.miaosha_7.vo.GoodsVo;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author yuhao
 * @date: 2021/3/8
 * @description:
 */
@Mapper
public interface GoodsDao {

	@Select("select g.*,mg.miaosha_price,mg.stock_count,mg.start_date,mg.end_date from miaosha_goods mg left join goods g on mg.goods_id = g.id")
	@ResultType(GoodsVo.class)
	@Results(
			{@Result(property = "id",column = "id"),
			@Result(property = "goodsName",column = "goods_name"),
			@Result(property = "goodsTitle",column = "goods_title"),
			@Result(property = "goodsImg",column = "goods_img"),
			@Result(property = "goodsDetail",column = "goods_detail"),
			@Result(property = "goodsPrice",column = "goods_price"),
			@Result(property = "goodsStock",column = "goods_stock"),
			@Result(property = "miaoshaPrice",column = "miaosha_price"),
			@Result(property = "stockCount",column = "stock_count"),
			@Result(property = "startDate",column = "start_date"),
			@Result(property = "endDate",column = "end_date")}
	)
	public List<GoodsVo> listGoodsVo();

	@Select("select g.*,mg.miaosha_price,mg.stock_count,mg.start_date,mg.end_date from miaosha_goods mg left join goods g on mg.goods_id = g.id where g.id=#{goodsId} ")
	@Results(
			{@Result(property = "id",column = "id"),
					@Result(property = "goodsName",column = "goods_name"),
					@Result(property = "goodsTitle",column = "goods_title"),
					@Result(property = "goodsImg",column = "goods_img"),
					@Result(property = "goodsDetail",column = "goods_detail"),
					@Result(property = "goodsPrice",column = "goods_price"),
					@Result(property = "goodsStock",column = "goods_stock"),
					@Result(property = "miaoshaPrice",column = "miaosha_price"),
					@Result(property = "stockCount",column = "stock_count"),
					@Result(property = "startDate",column = "start_date"),
					@Result(property = "endDate",column = "end_date")}
	)
	GoodsVo getGoodsVoByGoodsId(@Param("goodsId") long goodsId);

	// 第一句sql会有并发问题。假设库存只剩一个了，此时有多个线程到来访问。则会同时将库存减一，减为负数
//	@Update("update miaosha_goods set stock_count=stock_count-1 where goods_id=#{goodsId}  ")
	// 下面的sql会在减库存时再执行库存检查
	@Update("update miaosha_goods set stock_count=stock_count-1 where goods_id=#{goodsId} and stock_count > 0")
	public int reduceStock(MiaoshaGoods g);
}
