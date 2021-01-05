package com.nettyboot.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.config.LogicMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class LogicConfig {

	private String module;
	private String action;
	private int version;

	private LogicMethod method;

	private String[] parameters;

	private boolean peerid;

	private String[] ips;

	public LogicConfig(){

	}

	public LogicConfig(LogicAnnotation logicAnnotation){
		this.module = logicAnnotation.module();
		this.action = logicAnnotation.action();
		this.version = logicAnnotation.version();
		this.method = logicAnnotation.method();
		this.parameters = logicAnnotation.parameters();
		this.peerid = logicAnnotation.peerid();
		this.ips = logicAnnotation.ips();
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

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public LogicMethod getMethod() {
		return method;
	}

	public void setMethod(LogicMethod method) {
		this.method = method;
	}

	public String[] getParameters() {
		return parameters;
	}

	public void setParameters(String[] parameters) {
		this.parameters = parameters;
	}

	public boolean isPeerid() {
		return peerid;
	}

	public void setPeerid(boolean peerid) {
		this.peerid = peerid;
	}

	public String[] getIps() {
		return ips;
	}

	public void setIps(String[] ips) {
		this.ips = ips;
	}
}
