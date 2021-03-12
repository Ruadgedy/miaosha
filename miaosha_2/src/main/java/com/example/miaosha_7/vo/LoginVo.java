package com.example.miaosha_7.vo;

import com.example.miaosha_7.validator.IsMobile;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author yuhao
 * @date: 2021/3/8
 * @description:
 */
public class LoginVo {


	@Override
	public String toString() {
		return "LoginVo{" +
				"mobile='" + mobile + '\'' +
				", password='" + password + '\'' +
				'}';
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@NotNull
	@IsMobile
	private String mobile;

	@NotNull
	@Length(min = 32)
	private String password;
}
