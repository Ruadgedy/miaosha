package com.example.miaosha_7.config;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

/**
 * @author yuhao
 * @date: 2021/3/11
 * @description:
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AccessLimit {
	int second();

	int maxCount();

	boolean needLogin() default true;
}
