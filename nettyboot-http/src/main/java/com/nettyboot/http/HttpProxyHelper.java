package com.nettyboot.http;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class HttpProxyHelper {

	private static Logger logger = LoggerFactory.getLogger(HttpProxyHelper.class);

	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	public static final MediaType XML = MediaType.parse("application/xml; charset=utf-8");
	public static final MediaType FORM_URLENCODED = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
	public static final MediaType TEXT = MediaType.parse("text/plain; charset=utf-8");

	private static final String[] USER_AGENT = {
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36", //google
			"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36 OPR/60.0.3255.109", //opera
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36 QIHU 360SE", //360
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.79 Safari/537.36 Maxthon/5.2.7.3000", //遨游
			"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:67.0) Gecko/20100101 Firefox/67.0", //firefox
			"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_8; en-us) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50" //safari
	};

	public static enum DataType{
		JSON, FORM
	};

	protected static class MyProxySelector extends ProxySelector{

		@Override
		public List<Proxy> select(URI uri) {
			List<Proxy> proxyList = new ArrayList<>();

			String host = IPProxyManager.getHostPort(uri.toString());
			if(host != null) {
				String[] hostPort = host.split(":");
				logger.error("QSProxySelector: host={}, port={}", hostPort);
				proxyList.add(new Proxy(Type.HTTP, new InetSocketAddress(hostPort[0], Integer.parseInt(hostPort[1]))));
			}
			return proxyList;
		}

		@Override
		public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
			//logger.error("QSProxySelector.connectFailed: uri: {}, sa: {}", uri.toString(), sa.toString());
		}
	}

	private OkHttpClient httpClient;

	public HttpProxyHelper(boolean proxy){
		if(proxy) {
			httpClient = new OkHttpClient.Builder()
					.proxySelector(new MyProxySelector())
					.connectTimeout(5, TimeUnit.SECONDS)
					.readTimeout(5, TimeUnit.SECONDS).followRedirects(false)
					.retryOnConnectionFailure(false)
					.build();
		}else{
			httpClient = new OkHttpClient.Builder()
					.connectTimeout(5, TimeUnit.SECONDS)
					.readTimeout(5, TimeUnit.SECONDS).followRedirects(false)
					.retryOnConnectionFailure(false)
					.build();
		}
	}

	public void shutdown(){
		try {
			if (httpClient != null) {
				if(httpClient.cache() != null){
					httpClient.cache().close();
				}
				httpClient.connectionPool().evictAll();
				httpClient.dispatcher().executorService().shutdown();
				httpClient = null;
			}
		}catch (Exception e){
			logger.error("XClient.shutdown error:", e);
		}
	}

    public String getByProxyRetry(String url, String charset, Map<String, String> headers){
        return getByProxyRetry(url, charset, headers, 5);
    }

    public String getByProxyRetry(String url, String charset, Map<String, String> headers, Integer tryCount) {
        String responseString = getByProxy(url, charset, headers);
        int count = 1;
        while(responseString == null || responseString.equals("407") || responseString.equals("403") || responseString.contains("403 Forbidden") || responseString.contains("You don't have permission to access the URL on this server")) {
            try {
                if(count >= tryCount) {
                    break;
                }
                Thread.sleep((int)(Math.random()* 100+100));

                count ++;
                responseString = getByProxy(url, charset, headers);
                
            } catch (Exception e) {
            	logger.error("getByProxyRetry.Exception", e);
            }finally {

            }
        }
        if(count > tryCount && (responseString == null || (responseString != null && (responseString.equals("404") || responseString.equals("443") || responseString.equals("error"))))) {
        	logger.error("getHttpConnection retry: url:{}, trys: {}", url, count);
        }
        return responseString;
    }
	
	public String randomUserAgent() {
		Random random = new Random();
		int i = random.nextInt(USER_AGENT.length);
		String userAgent = USER_AGENT[i];
		return userAgent;
	}
	
    public String getByProxy(String url, String charset, Map<String, String> headers){
    	Response response = null;
        try {
        	Headers.Builder headersbuilder = new Headers.Builder();
        	//headersbuilder.add("Content-Type", "text/html; charset=utf-8");
			if(headers == null || !headers.containsKey("User-Agent")){
				headersbuilder.add("User-Agent", randomUserAgent());
			}
        	headersbuilder.add("Accept-Language", "zh-CN,zh;q=0.9");
        	headersbuilder.add("Connection", "close");
        	//headersbuilder.add("Accept-Encoding", "gzip, deflate");
        	if(headers != null && !headers.isEmpty()) {
        		for(String name : headers.keySet()) {
					if(!"accept-encoding".equals(name.toLowerCase())) {
						headersbuilder.add(name, headers.get(name));
					}
        		}
        	}
    		
            Request request = new Request.Builder().url(url).headers(headersbuilder.build()).build();
            response = httpClient.newCall(request).execute();
            if(response.isSuccessful()) {
            	if(charset == null) {
            		String contentType = response.header("Content-Type");
                	if(contentType != null) {
                		contentType = contentType.toLowerCase();
                        if(charset == null){
                            if(contentType.contains("utf-8")){
                                charset = "UTF-8";
                            }else if(contentType.contains("gbk")){
                                charset = "GBK";
                            }else if(contentType.contains("gb2312")){
                                charset = "GB2312";
                            }else if(contentType.contains("iso8859-1")){
                                charset = "iso8859-1";
                            }else{
                                charset = "UTF-8";
                            }
                        }
                	}
                	if(charset == null) {
                		charset = "UTF-8";
                	}
            	}
            	
                byte[] bodyBytes = response.body().bytes();
                String bodyStr = new String(bodyBytes, charset);
                return bodyStr.trim();
            }else {
            	response.close();
            	logger.info("getByProxy:url={}, responseCode={}", url, response.code());
            	return String.valueOf(response.code());
            }
            
        }catch(SocketTimeoutException e) {
        	if(response != null) {
        		response.close();
        	}
        }catch(ConnectException e) {
        	if(response != null) {
        		response.close();
        	}
        }catch(IOException e) {
			logger.error("getByProxy.IOException: {}, url: {}", e.getMessage(), url);
			if(response != null) {
				response.close();
			}
		}catch (Exception e){
        	if(response != null) {
        		response.close();
        	}
        	logger.error("getByProxy.Exception {}, url: {}, headers: {}", e.getMessage(), url, headers);
        	logger.error("getByProxy.Exception: ", e);
        }finally {
        }
        return null;
    }


	public String get(String url, String charset){
		Response response = null;
		try {
			Headers.Builder headersbuilder = new Headers.Builder();
			Request request = new Request.Builder().url(url).headers(headersbuilder.build()).build();
			response = httpClient.newCall(request).execute();
			if(response.isSuccessful()) {
				String contentType = response.header("Content-Type").toLowerCase();
				if(charset == null) {
					if (contentType.contains("utf-8")) {
						charset = "UTF-8";
					} else if (contentType.contains("gbk")) {
						charset = "GBK";
					} else if (contentType.contains("gb2312")) {
						charset = "GB2312";
					} else if (contentType.contains("iso8859-1")) {
						charset = "iso8859-1";
					} else {
						charset = "UTF-8";
					}
				}
				byte[] bodyBytes = response.body().bytes();
				String bodyStr = new String(bodyBytes, charset);
				return bodyStr;
			}else {
				response.close();
			}
		}catch (Exception e){
			if(response != null) {
				response.close();
			}
			//logger.error("get.Exception", e);
		}finally {
		}
		return null;
	}

	public String get(String url, String charset, Map<String, String> headers){
		Response response = null;
		try {
			Headers.Builder headersbuilder = new Headers.Builder();
			for(String name : headers.keySet()){
				headersbuilder.add(name, headers.get(name));
			}
			Request request = new Request.Builder().url(url).headers(headersbuilder.build()).build();
			response = httpClient.newCall(request).execute();
			if(response.isSuccessful()) {
				String contentType = response.header("Content-Type").toLowerCase();
				if(charset == null) {
					if (contentType.contains("utf-8")) {
						charset = "UTF-8";
					} else if (contentType.contains("gbk")) {
						charset = "GBK";
					} else if (contentType.contains("gb2312")) {
						charset = "GB2312";
					} else if (contentType.contains("iso8859-1")) {
						charset = "iso8859-1";
					} else {
						charset = "UTF-8";
					}
				}
				byte[] bodyBytes = response.body().bytes();
				String bodyStr = new String(bodyBytes, charset);
				return bodyStr;
			}else {
				response.close();
			}
		}catch (Exception e){
			if(response != null) {
				response.close();
			}
			//logger.error("get.Exception", e);
		}finally {
		}
		return null;
	}

	public String get(String url, Map<String, String> params, Map<String, String> headers, String charset){
		Response response = null;
		try {
			if(params == null){
				params = new HashMap<>();
			}

			if(headers == null){
				headers = new HashMap<>();
			}
			Headers.Builder headersbuilder = new Headers.Builder();
			for(String name : headers.keySet()){
				headersbuilder.add(name, headers.get(name));
			}
			HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(url).newBuilder();
			for(String name : params.keySet()){
				httpUrlBuilder.addQueryParameter(name, params.get(name));
			}
			Request request = new Request.Builder().url(httpUrlBuilder.build()).headers(headersbuilder.build()).build();
			response = httpClient.newCall(request).execute();
			if(response.isSuccessful()) {
				String contentType = response.header("Content-Type").toLowerCase();
				if(charset == null) {
					if (contentType.contains("utf-8")) {
						charset = "UTF-8";
					} else if (contentType.contains("gbk")) {
						charset = "GBK";
					} else if (contentType.contains("gb2312")) {
						charset = "GB2312";
					} else if (contentType.contains("iso8859-1")) {
						charset = "iso8859-1";
					} else {
						charset = "UTF-8";
					}
				}
				byte[] bodyBytes = response.body().bytes();
				String bodyStr = new String(bodyBytes, charset);
				return bodyStr;
			}else {
				response.close();
			}
		}catch (Exception e){
			if(response != null) {
				response.close();
			}
			//logger.error("get.Exception", e);
		}finally {
		}
		return null;
	}

}
