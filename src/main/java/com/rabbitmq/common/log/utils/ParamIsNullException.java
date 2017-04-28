package com.rabbitmq.common.log.utils;

/**
 * Created by MiaoJia on 2016/12/2.
 */
public class ParamIsNullException extends Exception {
	public ParamIsNullException() {
		super();
	}

	public ParamIsNullException(String message) {
		super(message);
	}
}
