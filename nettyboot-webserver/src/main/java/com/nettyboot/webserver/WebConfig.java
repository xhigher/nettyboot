package com.nettyboot.webserver;

import com.nettyboot.config.BaseConfig.PostBodyType;
import com.nettyboot.config.RequestInfo;
import io.netty.handler.codec.http.*;

import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * @copyright (c) xhigher 2015
 * @author xhigher    2015-3-26 
 */
public final class WebConfig {

    private static final Pattern requestPathPattern = Pattern.compile("^\\/v(\\d+)\\/([a-zA-Z]\\w+)\\/([a-zA-Z]\\w+)\\/?$");


    private static String headerNameSign = "NB";
    private static String headerIPName = null;
    private static String headerDeviceName = null;
    private static String headerVersionName = null;

    private static String headerPeeridName = null;
    private static String headerSessionidName = null;

    private static String accessControlAllowHeaders = null;
    private static String accessControlAllowMethods = null;
    private static Pattern accessControlAllowOrigin = null;
    private static String responseContentType = null;

    private static final Map<String, String> headerNameKeys = new HashMap<>();

    protected static void init(Properties properties){
        accessControlAllowHeaders = properties.getProperty("webserver.accessControlAllowHeaders", "").trim();
        accessControlAllowMethods = properties.getProperty("webserver.accessControlAllowMethods", "").trim().toUpperCase();
        String allowOrigin = properties.getProperty("webserver.accessControlAllowOrigin", "").trim();
        if(!allowOrigin.isEmpty()){
            accessControlAllowOrigin = Pattern.compile("^http(s?):\\/\\/("+allowOrigin+")(.*?)$");
        }
        responseContentType = properties.getProperty("webserver.responseContentType", "application/json").trim();

        headerNameSign = properties.getProperty("webserver.headerNameSign", "NB").trim().toUpperCase();
        headerIPName = properties.getProperty("webserver.headerIPName", String.format("X-%s-IP", headerNameSign));
        headerDeviceName = properties.getProperty("webserver.headerDeviceName", String.format("X-%s-DEVICE", headerNameSign));
        headerVersionName = properties.getProperty("webserver.headerVersionName", String.format("X-%s-VERSION", headerNameSign));
        headerPeeridName = properties.getProperty("webserver.headerPeeridName", String.format("X-%s-PEERID", headerNameSign));
        headerSessionidName = properties.getProperty("webserver.headerSessionidName", String.format("X-%s-SESSIONID", headerNameSign));
    }

    protected static void parseHeaders(HttpHeaders headers, RequestInfo requestInfo){
        if(headers.contains(headerIPName)){
            requestInfo.setClientIP(headers.get(headerIPName));
        }
        if(headers.contains(headerDeviceName)){
            requestInfo.setClientDevice(headers.get(headerDeviceName));
        }
        if(headers.contains(headerVersionName)){
            requestInfo.setClientVersion(headers.get(headerVersionName));
        }
        if(headers.contains(headerPeeridName)){
            requestInfo.setPeerid(headers.get(headerPeeridName));
        }
        if(headers.contains(headerSessionidName)){
            requestInfo.setSessionid(headers.get(headerSessionidName));
        }
        Iterator<Map.Entry<String, String>> iterator = headers.iteratorAsString();
        Map.Entry<String, String> entry = null;
        while (iterator.hasNext()){
            entry = iterator.next();
            requestInfo.addHeader(entry.getKey(), entry.getValue());
        }
    }

    protected static Matcher checkRequestPath(String path){
        return requestPathPattern.matcher(path);
    }

    protected static String getAccessControlAllowHeaders(){
        return accessControlAllowHeaders;
    }

    protected static String getAccessControlAllowMethods(){
        return accessControlAllowMethods;
    }

    protected static String getResponseContentType(){
        return responseContentType + "; charset=" + CharsetUtil.UTF_8.toString();
    }

    protected static PostBodyType getPostBodyType(){
        if(responseContentType.contains("/json")){
            return PostBodyType.JSON;
        }
        return PostBodyType.FORM;
    }

    protected static boolean checkAllowOrigin(String origin){
	    if(accessControlAllowOrigin == null){
            return true;
        }
        return accessControlAllowOrigin.matcher(origin).matches();
    }

    protected static boolean checkAllowMethods(HttpMethod method){
	    if(accessControlAllowMethods == null || accessControlAllowMethods.isEmpty()){
	        return true;
        }
        return (method != null && accessControlAllowMethods.contains(method.name().toUpperCase()));
    }


}
