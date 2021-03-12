package com.example.miaosha_7.util;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yuhao
 * @date: 2021/3/8
 * @description:
 */
public class ValidatorUtil {

	private static final Pattern mobile_pattern = Pattern.compile("1\\d{10}");

	public static boolean isMobile(String src){
		if (StringUtils.isEmpty(src)){
			return false;
		}
		Matcher matcher = mobile_pattern.matcher(src);
		return matcher.matches();
	}
}
