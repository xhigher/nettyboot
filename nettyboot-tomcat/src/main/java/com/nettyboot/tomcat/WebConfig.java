package com.nettyboot.tomcat;

import com.nettyboot.config.RequestInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * @copyright (c) xhigher 2015
 * @author xhigher    2015-3-26 
 */
public final class WebConfig {

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
        headerIPName = String.format("X-%s-IP", headerNameSign);
        headerDeviceName = String.format("X-%s-DEVICE", headerNameSign);
        headerVersionName = String.format("X-%s-VERSION", headerNameSign);
        headerPeeridName = String.format("X-%s-PEERID", headerNameSign);
        headerSessionidName = String.format("X-%s-SESSIONID", headerNameSign);
    }

    protected static void parseHeaders(HttpServletRequest request, RequestInfo requestInfo){
        String headerValue = request.getHeader(headerIPName);
        if(headerValue != null){
            requestInfo.setClientIP(headerValue);
        }
        headerValue = request.getHeader(headerDeviceName);
        if(headerValue != null){
            requestInfo.setClientDevice(headerValue);
        }
        headerValue = request.getHeader(headerVersionName);
        if(headerValue != null){
            requestInfo.setClientVersion(headerValue);
        }
        headerValue = request.getHeader(headerPeeridName);
        if(headerValue != null){
            requestInfo.setPeerid(headerValue);
        }
        headerValue = request.getHeader(headerSessionidName);
        if(headerValue != null){
            requestInfo.setSessionid(headerValue);
        }
    }

    protected static String getAccessControlAllowHeaders(){
        return accessControlAllowHeaders;
    }

    protected static String getAccessControlAllowMethods(){
        return accessControlAllowMethods;
    }



}
