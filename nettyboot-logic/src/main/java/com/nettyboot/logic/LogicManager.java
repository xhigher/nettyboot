package com.nettyboot.logic;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.config.*;
import com.nettyboot.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LogicManager {

	private static final Logger logger = LoggerFactory.getLogger(LogicManager.class);

	private static final int statisticDelayTime = 3600;
	private static final ScheduledExecutorService statisticExecutor = Executors.newSingleThreadScheduledExecutor();
	
	private static final Map<String, Map<String, LogicAnnotation>> LOGIC_CONFIGS = new HashMap<String, Map<String, LogicAnnotation>>();
	private static final Map<String, Map<String, BaseLogic>> LOGIC_SOURCES = new HashMap<String, Map<String, BaseLogic>>();

    public static void init(String logicPackage){
		initLogics(logicPackage);

		initStatistic();

    }

	public static List<LogicConfig> getLogicConfigList(){
		Map<String, LogicAnnotation> configList = null;
		LogicConfig logicConfig = null;
		List<LogicConfig> logicConfigList = new ArrayList();
		for(String module : LOGIC_CONFIGS.keySet()){
			configList = LOGIC_CONFIGS.get(module);
			for (LogicAnnotation logicAnnotation : configList.values()) {
				logicConfig = new LogicConfig(logicAnnotation);
				logicConfigList.add(logicConfig);
			}
		}
		return logicConfigList;
	}

	public static LogicAnnotation getLogicConfig(String module, String action, int version) {
		if(LOGIC_CONFIGS.containsKey(module)){
			return LOGIC_CONFIGS.get(module).get(getLogicIdentifier(action, version));
		}
		return null;
	}
	
	public static String executeLogic(RequestInfo requestInfo){
		try{
			if(LOGIC_SOURCES.containsKey(requestInfo.getModule())){
				BaseLogic logic = LOGIC_SOURCES.get(requestInfo.getModule()).get(getLogicIdentifier(requestInfo.getAction(), requestInfo.getVersion()));
				if(logic != null){
					logic = logic.clone();
					logger.debug("executeLogic: requestInfo={}", requestInfo.toString());
					return logic.handle(requestInfo);
				}
			}
		} catch(Exception e){
			logger.error("executeLogic.Exception: requestInfo={}", requestInfo.toString());
		}finally{
			
		}
		return LogicResultHelper.errorRequest();
	}
	
	private static String getLogicIdentifier(String action, int version) {
		return action + "@" + version;
	}
	
	private static String getLogicIdentifier(LogicAnnotation logicAnnotation) {
		return getLogicIdentifier(logicAnnotation.action(), logicAnnotation.version());
	}
	
	public static void initLogics(String logicPackage){
		List<Class> classList = FileUtil.getClassesFromPackage(logicPackage);
		if(classList == null || classList.size() == 0){
			logger.error("initLogics.classList.null");
			return;
		}
		try{
			String module = null;
			String logicIdentifier = null;
			Class<?> clazz = null;
			LogicAnnotation logicAnnotation = null;
			for (int i=0; i<classList.size(); i++){
				clazz = classList.get(i);
				logicAnnotation = clazz.getAnnotation(LogicAnnotation.class);
				if(logicAnnotation != null){
					module = logicAnnotation.module();
					if(!LOGIC_CONFIGS.containsKey(module)){
						LOGIC_CONFIGS.put(module, new HashMap<String, LogicAnnotation>());
					}
					if(!LOGIC_SOURCES.containsKey(module)){
						LOGIC_SOURCES.put(module, new HashMap<String, BaseLogic>());
					}
					logicIdentifier = getLogicIdentifier(logicAnnotation);
					LOGIC_CONFIGS.get(module).put(logicIdentifier, logicAnnotation);
					LOGIC_SOURCES.get(module).put(logicIdentifier, (BaseLogic)clazz.newInstance());
				}
			}
		}catch (Exception e){
			logger.error("Exception", e);
		}
	}

	private static void initStatistic(){
		statisticExecutor.scheduleWithFixedDelay(new Runnable(){
			@Override
			public void run() {
				JSONObject statisticData = new JSONObject();
				Map<String, BaseLogic> actionList = null;
				for(String module : LOGIC_SOURCES.keySet()) {
					actionList = LOGIC_SOURCES.get(module);
					for(String action : actionList.keySet()) {
						statisticData.put(module+"#"+action, actionList.get(action).getCloneCount());
					}
				}
				logger.info("initStatistic: {}", statisticData.toJSONString());
			}
		}, 600, statisticDelayTime, TimeUnit.SECONDS);
	}

}
