package com.nettyboot.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequestInfo implements Serializable {


	protected Integer version;
	protected String module;
	protected String action;

	protected String clientIP;
	protected String clientDevice;
	protected String clientVersion;

	protected String peerid;
	protected String sessionid;

	protected JSONObject headers;
	protected JSONObject parameters;

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getClientIP() {
		return clientIP;
	}

	public void setClientIP(String clientIP) {
		this.clientIP = clientIP;
	}

	public String getClientDevice() {
		return clientDevice;
	}

	public void setClientDevice(String clientDevice) {
		this.clientDevice = clientDevice;
	}

	public String getClientVersion() {
		return clientVersion;
	}

	public void setClientVersion(String clientVersion) {
		this.clientVersion = clientVersion;
	}

	public String getPeerid() {
		return peerid;
	}

	public void setPeerid(String peerid) {
		this.peerid = peerid;
	}

	public String getSessionid() {
		return sessionid;
	}

	public void setSessionid(String sessionid) {
		this.sessionid = sessionid;
	}

	public JSONObject getHeaders() {
		return headers;
	}

	public void setHeaders(JSONObject headers) {
		this.headers = headers;
	}

	public void addHeader(String key, String value) {
		if(headers == null){
			headers = new JSONObject();
		}
		headers.put(key, value);
	}

	public String getHeaderValue(String key) {
		if(headers != null){
			return headers.getString(key);
		}
		return null;
	}

	public JSONObject getParameters() {
		return parameters;
	}

	public void setParameters(JSONObject parameters) {
		this.parameters = parameters;
	}

	public void addParameters(Map<String, List<String>> params){
		if(this.parameters == null){
			this.parameters = new JSONObject();
		}
		if(params !=null && params.size() > 0) {
			List<String> tpv = null;
			for (String pn : params.keySet()) {
				tpv = params.get(pn);
				if (tpv.size() > 1) {
					this.parameters.put(pn, tpv);
				} else if (tpv.size() == 1) {
					this.parameters.put(pn, tpv.get(0));
				} else {
					this.parameters.put(pn, "");
				}
			}
		}
	}

	public void addParameters2(Map<String,String[]> params){
		if(this.parameters == null){
			this.parameters = new JSONObject();
		}
		if(params !=null && params.size() > 0){
			String[] tpv = null;
			for (String pn : params.keySet()) {
				tpv = params.get(pn);
				if (tpv.length > 1) {
					this.parameters.put(pn, tpv);
				} else if (tpv.length == 1) {
					this.parameters.put(pn, tpv[0]);
				} else {
					this.parameters.put(pn, "");
				}
			}
		}
	}

	public void addParameters(JSONObject data){
		if(this.parameters == null){
			this.parameters = new JSONObject();
		}
		this.parameters.putAll(data);
	}

	public String checkRequiredParameters(final String[] requires){
		if(requires != null && requires.length > 0){
			String pn = null;
			List<String> lackedParams = new ArrayList<String>();
			for(int i=0,n=requires.length; i<n; i++){
				pn = requires[i];
				if(!parameters.containsKey(pn)){
					lackedParams.add(pn);
				}
			}
			if(lackedParams.size() > 0){
				return lackedParams.toString();
			}
		}
		return null;
	}

	public boolean checkAllowedIP(String[] ipList){
		boolean isForbidden = false;
		if(ipList.length > 0){
			isForbidden = true;
			for(int i=0,n=ipList.length; i<n; i++){
				if(ipList[i].equals(clientIP)){
					isForbidden = false;
					break;
				}
			}
			if(isForbidden){
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString(){
		return String.format("/v%d/%s/%s, parameters=%s", getVersion(), getModule(), getAction(), getParameters().toString());
	}
}
