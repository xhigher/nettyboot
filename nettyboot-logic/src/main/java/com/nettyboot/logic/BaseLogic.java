package com.nettyboot.logic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.ClientPeer;
import com.nettyboot.config.ErrorCode;
import com.nettyboot.config.LogicResultHelper;
import com.nettyboot.config.RequestInfo;
import com.nettyboot.util.ClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.LongAdder;

/*
 * @copyright (c) xhigher 2015 
 * @author xhigher    2015-3-26 
 */
public abstract class BaseLogic implements Cloneable {

	protected static final Logger logger = LoggerFactory.getLogger(BaseLogic.class);

	private RequestInfo requestInfo;

	private final LongAdder cloneCount = new LongAdder();

	public String handle(RequestInfo requestInfo){
		this.requestInfo = requestInfo;
		return this.handle();
	}

	public String getPeerid() {
		return requestInfo.getPeerid();
	}

	public ClientPeer getClientPeer(){
		return ClientUtil.checkPeerid(requestInfo.getPeerid());
	}

	public String getSessionid() {
		return requestInfo.getSessionid();
	}

	public String getClientIP(){
		return requestInfo.getClientIP();
	}

	public String getClientDevice(){
		return requestInfo.getClientDevice();
	}

	public String getClientVersion(){
		return requestInfo.getClientVersion();
	}

	public boolean hasParameter(String name){
		return requestInfo.getParameters().containsKey(name);
	}

	public String getStringParameter(String name){
		return requestInfo.getParameters().getString(name);
	}

	public Integer getIntegerParameter(String name){
		return requestInfo.getParameters().getIntValue(name);
	}

	public Long getLongParameter(String name){
		return requestInfo.getParameters().getLongValue(name);
	}

	public Double getDoubleParameter(String name){
		return requestInfo.getParameters().getDoubleValue(name);
	}

	public <T extends Enum<T>> T getEnumParameter(Class<T> enumType, String name) {
		try {
			String param = getStringParameter(name);
			if(param != null) {
				return Enum.valueOf(enumType, getStringParameter(name));
			}
		}catch(IllegalArgumentException e){}
		return null;
	}

	public JSONArray getJSONArrayParameter(String name){
		return requestInfo.getParameters().getJSONArray(name);
	}

	public JSONObject getJSONObjectParameter(String name){
		return requestInfo.getParameters().getJSONObject(name);
	}

	public Integer getVersion() {
		return requestInfo.getVersion();
	}

	public String getModule() {
		return requestInfo.getModule();
	}

	public String getAction() {
		return requestInfo.getAction();
	}

	public String getHeaderValue(String key) {
		return requestInfo.getHeaderValue(key);
	}

	protected void beforeOutputResult(int code){

	}

	private String outputResult(int code, String info, Object obj){
		this.beforeOutputResult(code);
		return LogicResultHelper.staticOutput(code, info, obj);
	}

	public String errorInternalResult(){
		this.beforeOutputResult(ErrorCode.INTERNAL_ERROR);
		return LogicResultHelper.errorInternal();
	}

	public String errorParameterResult(String info){
		this.beforeOutputResult(ErrorCode.PARAMETER_ERROR);
		return LogicResultHelper.errorParameter(info);
	}

	public String errorSessionResult(){
		this.beforeOutputResult(ErrorCode.SESSION_INVALID);
		return LogicResultHelper.errorSession();
	}

	public String errorVerificodeResult(){
		this.beforeOutputResult(ErrorCode.VERIFICODE_ERROR);
		return LogicResultHelper.errorVerificode();
	}

	public String errorFrequentlyResult(){
		this.beforeOutputResult(ErrorCode.REQEUST_FREQUENTLY);
		return LogicResultHelper.errorFrequently();
	}

	public String successResult(){
		return outputResult(ErrorCode.OK, null, null);
	}

	public String successResult(JSONObject data){
		return outputResult(ErrorCode.OK, null, data);
	}

	public String successResult(JSONArray data){
		return outputResult(ErrorCode.OK, null, data);
	}

	public String successResult(Object data){
		return outputResult(ErrorCode.OK, null, data);
	}

	public String errorResult(int code, JSONObject data){
		return outputResult(code, null, data);
	}

	public String errorResult(int code, String info){
		return outputResult(code, info, null);
	}

	public String errorResult(String info, JSONObject data){
		return outputResult(ErrorCode.NOK, info, data);
	}

	public String errorResult(String info){
		return outputResult(ErrorCode.NOK, info, null);
	}

	public String errorResult(){
		return outputResult(ErrorCode.NOK, null, null);
	}

	protected boolean requireSession(){
		return true;
	}

	protected boolean requireAccountBound(){
		return false;
	}

	protected abstract String prepare();

	protected abstract String execute();

	protected void beforeExecute(){

	}

	protected String checkSession(){
		return null;
	}

	private String handle(){
		long start = System.currentTimeMillis();
		String result = null;
		try{
			if(this.requireSession()){
				String sessionResult = this.checkSession();
				if(sessionResult != null){
					return sessionResult;
				}
			}

			String prepareResult = this.prepare();
			if(prepareResult != null){
				return prepareResult;
			}

			this.beforeExecute();

			return this.execute();
		}catch(Exception e){
			logger.error(this.getClass().getSimpleName(), e);
			return errorInternalResult();
		}finally{
			logger.info("handle runtime: {}", (System.currentTimeMillis() - start));
		}
	}

	public long getCloneCount() {
		return cloneCount.longValue();
	}

	@Override
	public BaseLogic clone() {
		try{
			cloneCount.increment();
			return (BaseLogic) super.clone();
		}catch(CloneNotSupportedException e){
		}
		return null;
	}

}
