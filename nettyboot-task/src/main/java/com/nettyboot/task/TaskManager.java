package com.nettyboot.task;

import com.nettyboot.config.TaskAnnotation;
import com.nettyboot.config.TaskType;
import com.nettyboot.util.FileUtil;
import com.nettyboot.util.StringUtil;
import com.nettyboot.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/*
 * @copyright (c) xhigher 2015 
 * @author xhigher    2015-3-26 
 */
public final class TaskManager {
	
	private static Logger logger = LoggerFactory.getLogger(TaskManager.class);

	private static  Map<String, TaskAnnotation> TASK_CONFIGS = new HashMap<String, TaskAnnotation>();
	private static Map<String, BaseTask> TASK_SOURCES = new HashMap<String, BaseTask>();

	private static List<String> SUBSCRIBER_CHANNELS = new ArrayList<>();

	private static String taskPackage = "";

	private static TaskManager instance = null;
	
	private ScheduledExecutorService scheduledService;
	
	private ExecutorService workService;

	private static int SCHEDULED_SERVICE_POOL_SIZE = 4;
	private static int WORK_SERVICE_POOL_SIZE = 16;

	public static void init(Properties properties){
		if(instance == null){
			synchronized (TaskManager.class) {
				if(properties.containsKey("task.pool.size.scheduled")){
					SCHEDULED_SERVICE_POOL_SIZE = Integer.parseInt(properties.getProperty("task.pool.size.scheduled"));
				}
				if(properties.containsKey("task.pool.size.work")){
					WORK_SERVICE_POOL_SIZE = Integer.parseInt(properties.getProperty("task.pool.size.work"));
				}
				if(properties.containsKey("task.package")){
					taskPackage = properties.getProperty("task.package");
				}

				if (instance == null) {
					instance = new TaskManager();
				}
			}
		}
	}

	public static TaskManager getInstance(){
		if(instance == null){
			synchronized (TaskManager.class) {
                if (instance == null) {
                	instance = new TaskManager();
                }
            }
		}
		return instance;
	}
	
	private TaskManager(){
		this.scheduledService = Executors.newScheduledThreadPool(SCHEDULED_SERVICE_POOL_SIZE);
		this.workService = Executors.newScheduledThreadPool(WORK_SERVICE_POOL_SIZE);
	}

	private static void initTasks(){
		List<Class> classList = FileUtil.getClassesFromPackage(taskPackage);
		if(classList == null){
			logger.error("initTasks.classList.null");
			return;
		}
		try{
			Class<?> clazz = null;
			TaskAnnotation taskConfig = null;
			for (int i=0; i<classList.size(); i++){
				clazz = classList.get(i);
				taskConfig = clazz.getAnnotation(TaskAnnotation.class);
				if(taskConfig != null) {
					if(taskConfig.type() == TaskType.subscriber){
						SUBSCRIBER_CHANNELS.add(taskConfig.name());
					}
					if(taskConfig.test()) {
						TASK_CONFIGS.put(taskConfig.name(), taskConfig);
						TASK_SOURCES.put(taskConfig.name(), (BaseTask)clazz.newInstance());
					}
				}
			}
		}catch (Exception e){
			logger.error("Exception", e);
		}
	}

	public void startTimerTask(){
		for(TaskAnnotation config : TASK_CONFIGS.values()) {
			if(config.type() == TaskType.timer) {
				long initialDelay = config.delay();
				if(!config.starttime().isEmpty()) {
					initialDelay = TimeUtil.getSecondsFromNow(config.starttime());
				}
				logger.info("scheduleWithFixedDelay name: {}, initialDelay: {}", config.name(), initialDelay);
				scheduledService.scheduleWithFixedDelay(TASK_SOURCES.get(config.name()), initialDelay, config.interval(), config.timeunit());
			}
		}
	}
	
	public void addTimerTask(BaseTask task, long initialDelay, long delay){
		scheduledService.scheduleWithFixedDelay(task, initialDelay, delay, TimeUnit.SECONDS);
	}

	public void submitTask(String name, String data){
		BaseTask baseTask = TASK_SOURCES.get(name);
		if(baseTask != null){
			BaseTask task = (BaseTask) baseTask.clone();
			workService.submit(task.setData(data));
		}else {
			logger.error("Task.NotFound: name=" + name);
		}
	}
	
	public BaseTask getTask(String name, String data){
		BaseTask baseTask = TASK_SOURCES.get(name);
		if(baseTask != null){
			BaseTask task = (BaseTask) baseTask.clone();
			return task.setData(data);
		}else {
			logger.error("Task.NotFound: name=" + name);
		}
		return null;
	}
	
	public void submitTask(BaseTask task){
		if(task != null){
			workService.submit(task);
		}else {
			logger.error("Task.Null");
		}
	}
	
	public void release() {
		if (scheduledService != null) {
			scheduledService.shutdown();
		}
		if (workService != null) {
			workService.shutdown();
		}
	}
}
