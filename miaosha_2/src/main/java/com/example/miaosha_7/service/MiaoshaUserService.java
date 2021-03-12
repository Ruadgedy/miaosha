package com.example.miaosha_7.service;

import com.example.miaosha_7.dao.MiaoshaUserDao;
import com.example.miaosha_7.domain.MiaoshaUser;
import com.example.miaosha_7.exception.GlobalException;
import com.example.miaosha_7.redis.MiaoshaUserKey;
import com.example.miaosha_7.redis.RedisService;
import com.example.miaosha_7.result.CodeMsg;
import com.example.miaosha_7.util.MD5Util;
import com.example.miaosha_7.util.UUIDUtil;
import com.example.miaosha_7.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author yuhao
 * @date: 2021/3/8
 * @description:
 */
@Service
public class MiaoshaUserService {

	public static final String COOKIE_NAME_TOKEN = "token";

	@Autowired
	MiaoshaUserDao miaoshaUserDao;

	@Autowired
	RedisService redisService;

	public MiaoshaUser getById(long id) {
		return miaoshaUserDao.getById(id);
	}

	public boolean login(HttpServletResponse response, LoginVo loginVo) {
		if (loginVo == null)
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		String mobile = loginVo.getMobile();
		String password = loginVo.getPassword();
		// 判断手机号是否存在
		MiaoshaUser user = getById(Long.parseLong(mobile));
		if (user == null)
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		// 验证密码
		String dbPass = user.getPassword();
		String saltDb = user.getSalt();
		String calcPass = MD5Util.formPassToDBPass(password, saltDb);
		if (!calcPass.equals(dbPass)){
			throw new GlobalException(CodeMsg.PASSWORD_ERROR);
		}
		// 生成cookie
		String token = UUIDUtil.uuid();
		addCookie(response,token,user);
		return true;
	}

	private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
		redisService.set(MiaoshaUserKey.token,token,user);
		Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
		cookie.setMaxAge(MiaoshaUserKey.TOKEN_EXPIRE);
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	public MiaoshaUser getByToken(HttpServletResponse response, String token){
		if (StringUtils.isEmpty(token)) return null;
		MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
		// 延长有效期
		if (user != null){
			addCookie(response,token,user);
		}
		return user;
	}
}
