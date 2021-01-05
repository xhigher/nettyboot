package com.nettyboot.tomcat;

import java.util.List;
import java.util.Properties;

import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.server.BaseServer;
import com.nettyboot.util.FileUtil;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TomcatServer extends BaseServer {
	
	private static Logger logger = LoggerFactory.getLogger(TomcatServer.class);
	
	private final String defaultHost;
	private final int defaultPort;

	private final Tomcat tomcat;
	
	public TomcatServer(Properties properties) {
		
		defaultHost = properties.getProperty("server.host").trim();
		defaultPort = Integer.parseInt(properties.getProperty("server.port").trim());

		tomcat = new Tomcat();
        tomcat.setBaseDir(".");
        tomcat.setHostname(defaultHost);
        tomcat.setPort(defaultPort);
        tomcat.getConnector();

		init(properties);
	}

	@Override
	protected void init(Properties properties) {
		WebConfig.init(properties);
		initServlets(properties.getProperty("logic.package").trim());
	}

	public void initServlets(String logicPackage){
		List<Class> classList = FileUtil.getClassesFromPackage(logicPackage);
		if(classList == null){
			logger.error("initServlets.classList.null");
			return;
		}
		logger.error("initServlets:classList.size={}", classList.size());
		try{
			Context context = tomcat.addContext("", null);
			String logicIdentifier = null;
			Class<?> clazz = null;
			String urlPattern = null;
			LogicAnnotation logicAnnotation = null;
			for (int i=0; i<classList.size(); i++){
				clazz = classList.get(i);
				logicAnnotation = clazz.getAnnotation(LogicAnnotation.class);
				if(logicAnnotation != null){
					logicIdentifier = getLogicIdentifier(logicAnnotation);
					urlPattern = String.format("/v%d/%s/%s", logicAnnotation.version(), logicAnnotation.module(), logicAnnotation.action());
					logger.error("initServlets:urlPattern={}", urlPattern);
					LogicServlet servlet = (LogicServlet)clazz.newInstance();
					servlet.setConfig(logicAnnotation);
					tomcat.addServlet(context, logicIdentifier, servlet);
					context.addServletMappingDecoded(urlPattern, logicIdentifier);
				}
			}
		}catch (Exception e){
			logger.error("Exception", e);
		}
	}

	private String getLogicIdentifier(String action, int version) {
		return action + "@" + version;
	}

	private String getLogicIdentifier(LogicAnnotation logicAnnotation) {
		return getLogicIdentifier(logicAnnotation.action(), logicAnnotation.version());
	}

	@Override
	public void start() {
		try {

            tomcat.start();
            tomcat.getServer().await();

		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		try {
			tomcat.stop();
			tomcat.destroy();
		} catch (LifecycleException e) {
			logger.error("stop.exception", e);
		}
	}

}
