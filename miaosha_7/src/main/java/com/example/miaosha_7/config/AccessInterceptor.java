package com.example.miaosha_7.config;

import com.alibaba.fastjson.JSON;
import com.example.miaosha_7.domain.MiaoshaUser;
import com.example.miaosha_7.redis.AccessKey;
import com.example.miaosha_7.redis.RedisService;
import com.example.miaosha_7.result.CodeMsg;
import com.example.miaosha_7.result.Result;
import com.example.miaosha_7.service.MiaoshaUserService;
import com.example.miaosha_7.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author yuhao
 * @date: 2021/3/11
 * @description:
 */
@Component
public class AccessInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	MiaoshaUserService userService;

	@Autowired
	RedisService redisService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (handler instanceof HandlerMethod){
			MiaoshaUser user = getUser(request, response);
			UserContext.setUser(user);

			HandlerMethod hm = (HandlerMethod) handler;
			AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
			if (accessLimit == null){
				return true;
			}
			int second = accessLimit.second();
			int maxCount = accessLimit.maxCount();
			boolean needLogin = accessLimit.needLogin();
			String key = request.getRequestURI();
			if (needLogin){
				if (user == null){
					render(response, CodeMsg.SESSION_ERROR);
					return false;
				}
				key += "_" + user.getId();
			}else {
				// do nothing
			}

			// 对每一个用户生成访问key，设置时间。在固定时间间隔内用户的访问次数是受限的
			AccessKey ak = AccessKey.withExpire(second);
			Integer count = redisService.get(ak, key, Integer.class);
			if (count == null){
				redisService.set(ak,key,1);
			}else if (count < maxCount){
				redisService.incr(ak,key);
			}else {
				render(response,CodeMsg.ACCESS_LIMIT);
				return false;
			}
		}
		return super.preHandle(request, response, handler);
	}

	private void render(HttpServletResponse response, CodeMsg sessionError) throws IOException {
		response.setContentType("application/json;charSet=UTF-8");
		ServletOutputStream out = response.getOutputStream();
		String str = JSON.toJSONString(Result.error(sessionError));
		out.write(str.getBytes());
		out.flush();
		out.close();
	}

	private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response){
		String paramToken = request.getParameter(MiaoshaUserService.COOKIE_NAME_TOKEN);
		String cookieToken = getCookieValue(request,MiaoshaUserService.COOKIE_NAME_TOKEN);
		if (StringUtils.isEmpty(paramToken) && StringUtils.isEmpty(cookieToken)){
			return null;
		}
		String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
		return userService.getByToken(response,token);
	}

	private String getCookieValue(HttpServletRequest request, String cookieNameToken) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) return null;
		for (Cookie cookie : cookies){
			if (cookie.getName().equals(cookieNameToken)){
				return cookie.getValue();
			}
		}
		return null;
	}
}
