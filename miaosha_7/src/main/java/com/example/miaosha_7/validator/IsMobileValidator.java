package com.example.miaosha_7.validator;

import com.example.miaosha_7.util.ValidatorUtil;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author yuhao
 * @date: 2021/3/8
 * @description:
 */
public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {

	private boolean required = false;

	@Override
	public void initialize(IsMobile constraintAnnotation) {
		required = constraintAnnotation.required();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (required){
			return ValidatorUtil.isMobile(value);
		}else {
			if (StringUtils.isEmpty(value)){
				return true;
			}else {
				return ValidatorUtil.isMobile(value);
			}
		}
	}
}
