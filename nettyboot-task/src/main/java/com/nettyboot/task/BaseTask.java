package com.nettyboot.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.LongAdder;

public abstract class BaseTask implements Runnable, Cloneable {
	
	protected final static Logger logger = LoggerFactory.getLogger(BaseTask.class);
	
	private LongAdder cloneCount = new LongAdder();

	protected String data = null;

	protected abstract void execute();
	
	public Object clone() {
		try{
			cloneCount.increment();
			return super.clone();
		}catch(CloneNotSupportedException e){
		}
		return null;
	}
	
	public BaseTask setData(String data){
		this.data = data;
		return this;
	}
	
	public JSONObject getJSONObject() {
		try {
			return JSONObject.parseObject(this.data);
		}catch(Exception e) {	
		}
		return null;
	}
	
	public JSONArray getJSONArray() {
		try {
			return JSONObject.parseArray(this.data);
		}catch(Exception e) {	
		}
		return null;
	}
	
	public long getCloneCount() {
		return cloneCount.longValue();
	}

	protected void runBefore(){

	}

	protected void runException(){

	}

	protected void runFinally(){

	}

	@Override
	public void run() {
		try{
			this.runBefore();

			logger.info("Task Begin: {}", this.getClass().getSimpleName());
			this.execute();
			logger.info("Task End: {}", this.getClass().getSimpleName());
		}catch(Exception e){
			this.runException();
			logger.error("Task.Exception", e);
		}finally{
			this.runFinally();
		}
	}

}
