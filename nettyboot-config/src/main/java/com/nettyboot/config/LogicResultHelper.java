package com.nettyboot.config;

import java.util.HashMap;

/*
 * @copyright (c) xhigher 2015 
 * @author xhigher    2015-3-26 
 */
public abstract class LogicResultHelper {

	public static String success(){
		return staticOutput(ErrorCode.OK, null, null);
	}

	public static String success(Object data){
		return staticOutput(ErrorCode.OK, null, data);
	}

	public static String error(){
		return staticOutput(ErrorCode.NOK, null, null);
	}

	public static String errorInternal(){
		return staticOutput(ErrorCode.INTERNAL_ERROR, "INTERNAL_ERROR", null);
	}
	
	public static String errorRequest(){
		return staticOutput(ErrorCode.REQEUST_ERROR, "REQUEST_ERROR", null);
	}
	
	public static String errorMethod(){
		return staticOutput(ErrorCode.METHOD_ERROR, "METHOD_ERROR", null);
	}
	
	public static String errorParameter(String info){
		return staticOutput(ErrorCode.PARAMETER_ERROR, info, null);
	}

	public static String errorValidation(){
		return staticOutput(ErrorCode.VALIDATION_ERROR, "VALIDATION_ERROR", null);
	}

	public static String errorSession(){
		return staticOutput(ErrorCode.SESSION_INVALID, "SESSION_INVALID", null);
	}

	public static String errorVerificode(){
		return staticOutput(ErrorCode.VERIFICODE_ERROR, "VERIFICODE_ERROR", null);
	}

	public static String errorFrequently(){
		return staticOutput(ErrorCode.REQEUST_FREQUENTLY, "REQEUST_FREQUENTLY", null);
	}

	public static String staticOutput(int code,String info, Object data){
		if(info == null){
			info = "";
		}
		if(data == null){
			data = new HashMap<>();
		}
		LogicResult result = new LogicResult();
		result.setErrcode(code);
		result.setErrinfo(info);
		result.setData(data);
		return result.toString();
	}

}
