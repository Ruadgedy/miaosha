package com.example.miaosha_7.exception;

import com.example.miaosha_7.result.CodeMsg;

/**
 * @author yuhao
 * @date: 2021/3/8
 * @description:
 */
public class GlobalException extends RuntimeException {

	private CodeMsg cm;

	public CodeMsg getCm() {
		return cm;
	}

	public GlobalException(CodeMsg cm){
		super(cm.toString());
		this.cm = cm;
	}
}
