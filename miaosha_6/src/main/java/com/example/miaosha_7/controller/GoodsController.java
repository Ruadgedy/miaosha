package com.example.miaosha_7.controller;

import com.example.miaosha_7.domain.MiaoshaUser;
import com.example.miaosha_7.redis.GoodsKey;
import com.example.miaosha_7.redis.RedisService;
import com.example.miaosha_7.result.Result;
import com.example.miaosha_7.service.GoodsService;
import com.example.miaosha_7.service.MiaoshaUserService;
import com.example.miaosha_7.vo.GoodsDetailVo;
import com.example.miaosha_7.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author yuhao
 * @date: 2021/3/8
 * @description:
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {

	@Autowired
	MiaoshaUserService userService;

	@Autowired
	RedisService redisService;

	@Autowired
	GoodsService goodsService;

	@Autowired
	ThymeleafViewResolver thymeleafViewResolver;

	@Autowired
	ApplicationContext applicationContext;

	// 低级版，每次都要去获取cookie，然后根据cookie去redis中拿到用户数据
	// 代码有大量冗余
//	@RequestMapping("/to_list")
//	public String list(HttpServletResponse response, Model model,
//	                   @CookieValue(value = MiaoshaUserService.COOKIE_NAME_TOKEN,required = false)String cookieToken,
//	                   @RequestParam(value = MiaoshaUserService.COOKIE_NAME_TOKEN,required = false)String paramToken) {
//		if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)){ // 都为空，则说明用户未登录，先去登录
//			return "login";
//		}
//		String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
//		MiaoshaUser user = userService.getByToken(response, token);
//		model.addAttribute("user",user);
//		return "goods_list";
//	}

	/**
	 * 页面缓存优化：
	 * 初始版本是将用户数据绑定到Model，传递到页面上
	 *
	 * 新增produces="text/html"
	 * 也就是说改进后是直接返回网页的源代码
	 */
	@RequestMapping(value = "/to_list", produces = "text/html")
	@ResponseBody  // 手动渲染的时候，必须使用@ResponseBody，不能单纯使用@Controller
	public String list(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user){
		model.addAttribute("user",user);

		// 首先从缓存中取
		String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
		if (!StringUtils.isEmpty(html)){
			// 不为空直接返回页面
			return html;
		}

		// 查询商品列表
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		model.addAttribute("goodsList",goodsList);

		// 缓存中娶不到，手动渲染
		SpringWebContext ctx = new SpringWebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap(), applicationContext);
		html = thymeleafViewResolver.getTemplateEngine().process("goods_list",ctx);
		// 手动渲染完成后，将html结果保存在缓存中去
		if (!StringUtils.isEmpty(html)){
			redisService.set(GoodsKey.getGoodsList,"", html);
		}
		return html;
	}

	@RequestMapping(value = "/to_detail/{goodsId}",produces = "text/html")
	@ResponseBody  // 手动渲染必须加上@ResponseBody注解
	public String detail(HttpServletRequest request,HttpServletResponse response,Model model, MiaoshaUser user, @PathVariable("goodsId") long goodsId){
		model.addAttribute("user",user);

		// 取缓存
		String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
		if(!StringUtils.isEmpty(html)){
			return html;
		}

		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		model.addAttribute("goods", goods);

		long startAt = goods.getStartDate().getTime();
		long endAt = goods.getEndDate().getTime();
		long now = System.currentTimeMillis();

		int miaoshaStatus = 0;
		int remainSeconds = 0;

		if (now < startAt){ // 秒杀没开始，倒计时
			miaoshaStatus = 0;
			remainSeconds = (int) ((startAt - now) / 1000);
		}else if (now > endAt){ // 秒杀已经结束
			miaoshaStatus = 2;
			remainSeconds = -1;
		}else { // 秒杀正在进行中
			miaoshaStatus = 1;
			remainSeconds = 0;
		}

		model.addAttribute("miaoshaStatus", miaoshaStatus);
		model.addAttribute("remainSeconds", remainSeconds);

		// 手动渲染
		SpringWebContext context = new SpringWebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap(), applicationContext);
		html = thymeleafViewResolver.getTemplateEngine().process("goods_detail",context);
		if (!StringUtils.isEmpty(html)){
			redisService.set(GoodsKey.getGoodsDetail,""+goodsId,html);
		}
		return html;
	}

	@RequestMapping(value="/detail/{goodsId}")
	@ResponseBody
	public Result<GoodsDetailVo> detail2(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user,
	                                    @PathVariable("goodsId")long goodsId) {
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		long startAt = goods.getStartDate().getTime();
		long endAt = goods.getEndDate().getTime();
		long now = System.currentTimeMillis();
		int miaoshaStatus = 0;
		int remainSeconds = 0;
		if(now < startAt ) {//秒杀还没开始，倒计时
			miaoshaStatus = 0;
			remainSeconds = (int)((startAt - now )/1000);
		}else  if(now > endAt){//秒杀已经结束
			miaoshaStatus = 2;
			remainSeconds = -1;
		}else {//秒杀进行中
			miaoshaStatus = 1;
			remainSeconds = 0;
		}
		GoodsDetailVo vo = new GoodsDetailVo();
		vo.setGoods(goods);
		vo.setUser(user);
		vo.setRemainSeconds(remainSeconds);
		vo.setMiaoshaStatus(miaoshaStatus);
		return Result.success(vo);
	}
}
