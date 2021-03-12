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

	@Update("update miaosha_goods set stock_count=stock_count-1 where goods_id=#{goodsId}  ")
	public int reduceStock(MiaoshaGoods g);
}
