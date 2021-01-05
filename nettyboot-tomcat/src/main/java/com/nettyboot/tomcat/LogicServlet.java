package com.nettyboot.tomcat;


import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.ClientPeer;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicResultHelper;
import com.nettyboot.config.RequestInfo;
import com.nettyboot.logic.BaseLogic;
import com.nettyboot.util.ClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
 * @copyright (c) xhigher 2020
 * @author xhigher    2020-5-1
 */
public abstract class LogicServlet extends HttpServlet {

	private static final Logger logger = LoggerFactory.getLogger(LogicServlet.class);

	private LogicAnnotation logicAnnotation;

	private BaseLogic baseLogic;

	public LogicServlet(){
		baseLogic = getLogic();
	}

	protected void setConfig(LogicAnnotation logicAnnotation){
		this.logicAnnotation = logicAnnotation;
	}

	protected abstract BaseLogic getLogic();

	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String requestMethod = request.getMethod().toUpperCase();
		if(!this.logicAnnotation.method().toString().equals(requestMethod)){
			response.getWriter().write(LogicResultHelper.errorMethod());
			return;
		}

		RequestInfo requestInfo = new RequestInfo();
		WebConfig.parseHeaders(request, requestInfo);
		String clientIP = requestInfo.getClientIP();
        if (clientIP == null) {
			clientIP = request.getRemoteAddr();
			requestInfo.setClientIP(clientIP);
		}

		if(!requestInfo.checkAllowedIP(logicAnnotation.ips())){
			response.getWriter().write(LogicResultHelper.errorRequest());
		}

		requestInfo.addParameters2(request.getParameterMap());

		if(logicAnnotation.parameters().length > 0){
			String errinfo = requestInfo.checkRequiredParameters(logicAnnotation.parameters());
			if(errinfo != null){
				response.getWriter().write(LogicResultHelper.errorParameter(errinfo));
				return;
			}
		}

		String peerid = requestInfo.getPeerid();
		if(peerid == null){
			peerid = requestInfo.getParameters().getString(BaseDataKey.peerid);
			if(peerid == null || peerid.isEmpty()){
				response.getWriter().write(LogicResultHelper.errorValidation());
				return;
			}
			requestInfo.setPeerid(peerid);
		}

		ClientPeer clientPeer = ClientUtil.checkPeerid(peerid);
		if(logicAnnotation.peerid()){
			if(clientPeer.error()){
				response.getWriter().write(LogicResultHelper.errorValidation());
				return;
			}
		}

		String sessionid = requestInfo.getSessionid();
		if(sessionid == null){
			sessionid = requestInfo.getParameters().getString(BaseDataKey.sessionid);
			if(sessionid != null){
				requestInfo.setSessionid(sessionid);
			}
		}

		response.getWriter().write(baseLogic.clone().handle(requestInfo));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
	

}
