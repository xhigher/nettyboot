package com.nettyboot.http;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;


public class HttpClientHelper {
	
	private static final OkHttpClient httpClient = new OkHttpClient.Builder().build();

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType XML = MediaType.parse("application/xml; charset=utf-8");
	private static final MediaType FORM_URLENCODED = MediaType.parse("application/x-www-form-urlencoded");
	
	public enum DataType {
		JSON, FORM
	}

    public static String get(String url, Map<String, String> params) throws IOException {
        HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(url).newBuilder();
        for(String name : params.keySet()){
            httpUrlBuilder.addQueryParameter(name, params.get(name));
        }
        Request request = new Request.Builder().url(httpUrlBuilder.build()).build();
        Response response = httpClient.newCall(request).execute();
        if(response.isSuccessful()) {
        	return response.body().string();
        }else {
        	response.close();
        	return null;
        }
    }
    
    public static String getGB2312(String url, Map<String, String> params) throws IOException {
        HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(url).newBuilder();
        for(String name : params.keySet()){
            httpUrlBuilder.addQueryParameter(name, params.get(name));
        }
        Request request = new Request.Builder().url(httpUrlBuilder.build()).build();
        Response response = httpClient.newCall(request).execute();
        if(response.isSuccessful()) {
        	byte[] b = response.body().bytes(); 
            return new String(b, "GB2312");
        }else {
        	response.close();
        	return null;
        }
    }
    
    public static String post(String url, Map<String, String> params) throws IOException {
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        for(String name : params.keySet()){
            bodyBuilder.add(name, params.get(name));
        }
        Request request = new Request.Builder().url(url).post(bodyBuilder.build()).build();
        Response response = httpClient.newCall(request).execute();
        if(response.isSuccessful()) {
        	return response.body().string();
        }else {
        	response.close();
        	return null;
        }
    }
    
    public static String post(String url, JSONObject data) throws IOException {
        RequestBody bodyData = RequestBody.create(data.toJSONString(),JSON);
        Request request = new Request.Builder().url(url).post(bodyData).build();
        Response response = httpClient.newCall(request).execute();
        if(response.isSuccessful()) {
        	return response.body().string();
        }else {
        	response.close();
        	return null;
        }
    }
    
    public static String post(String url, JSONArray data) throws IOException {
        RequestBody bodyData = RequestBody.create(data.toJSONString(),JSON);
        Request request = new Request.Builder().url(url).post(bodyData).build();
        Response response = httpClient.newCall(request).execute();
        if(response.isSuccessful()) {
        	return response.body().string();
        }else {
        	response.close();
        	return null;
        }
    }
    
    public static String postXML(String url, String xmlString) throws IOException {
        RequestBody bodyData = RequestBody.create(xmlString, XML);
        Request request = new Request.Builder().url(url).post(bodyData).build();
        Response response = httpClient.newCall(request).execute();
        if(response.isSuccessful()) {
        	return response.body().string();
        }else {
        	response.close();
        	return null;
        }
    }
    

    public static void get(String url, Map<String, String> params, Callback responseCallback, Object tag) throws IOException {
        HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(url).newBuilder();
        for(String name : params.keySet()){
            httpUrlBuilder.addQueryParameter(name, params.get(name));
        }
        Request request = new Request.Builder().tag(tag).url(httpUrlBuilder.build()).build();
        Call call = httpClient.newCall(request);
        call.enqueue(responseCallback);
    }

    public static void post(String url, Map<String, String> params, Callback responseCallback, Object tag) throws IOException {
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        for(String name : params.keySet()){
            bodyBuilder.add(name, params.get(name));
        }
        Request request = new Request.Builder().tag(tag).url(url).post(bodyBuilder.build()).build();
        Call call = httpClient.newCall(request);
        call.enqueue(responseCallback);
    }
    
    public static void post(String url, String data, DataType type, Callback responseCallback, Object tag) throws IOException {
    	RequestBody bodyData = RequestBody.create(data, type== DataType.JSON?JSON:FORM_URLENCODED);
        Request request = new Request.Builder().tag(tag).url(url).post(bodyData).build();
        Call call = httpClient.newCall(request);
        call.enqueue(responseCallback);
    }
    
    public static void post(String url, JSONArray data, Callback responseCallback, Object tag) throws IOException {
    	RequestBody bodyData = RequestBody.create(data.toJSONString(), JSON);
        Request request = new Request.Builder().tag(tag).url(url).post(bodyData).build();
        Call call = httpClient.newCall(request);
        call.enqueue(responseCallback);
    }
    
    public static void post(String url, JSONObject data, Callback responseCallback, Object tag) throws IOException {
    	RequestBody bodyData = RequestBody.create(data.toJSONString(), JSON);
        Request request = new Request.Builder().tag(tag).url(url).post(bodyData).build();
        Call call = httpClient.newCall(request);
        call.enqueue(responseCallback);
    }

}
