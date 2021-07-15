package com.nettyboot.elasticsearch;

import com.alibaba.fastjson.JSON;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class XElasticSearch {
	
	private static final Logger logger = LoggerFactory.getLogger(XElasticSearch.class);

	private static RestHighLevelClient restClient = null;

	private static boolean initStarted = false;
	private static boolean initOK = false;
	
	public static void init(Properties properties) {
		if(properties.containsKey("elasticsearch.status") && 1==Integer.parseInt(properties.getProperty("elasticsearch.status").trim())) {
			if(initStarted || initOK){
				return;
			}
			initStarted = true;
			int size = Integer.parseInt(properties.getProperty("elasticsearch.node.size").trim());
			HttpHost[] httpHosts = new HttpHost[size];
			for (int i = 1; i <= size; i++) {
				httpHosts[i-1] = HttpHost.create(properties.getProperty("elasticsearch.node" + i + ".ip").trim()+":"+properties.getProperty("elasticsearch.node" + i + ".port").trim());
			}
			
			RestClientBuilder builder = RestClient.builder(httpHosts);
			if(properties.containsKey("elasticsearch.user") && properties.containsKey("elasticsearch.password")) {
				final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(properties.getProperty("elasticsearch.user"), properties.getProperty("elasticsearch.password")));

				builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
					@Override
					public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
						httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
						httpClientBuilder.setDefaultIOReactorConfig(IOReactorConfig.custom().setIoThreadCount(10).build());
						httpClientBuilder.setKeepAliveStrategy((response, context) -> {
							Args.notNull(response, "HTTP response");
							final HeaderElementIterator it = new BasicHeaderElementIterator(
									response.headerIterator(HTTP.CONN_KEEP_ALIVE));
							while (it.hasNext()) {
								final HeaderElement he = it.nextElement();
								final String param = he.getName();
								final String value = he.getValue();
								if (value != null && param.equalsIgnoreCase("timeout")) {
									try {
										return Long.parseLong(value) * 1000;
									} catch (final NumberFormatException ignore) {
									}
								}
							}
							// 三分钟
							return TimeUnit.MINUTES.toMillis(3);
						});
						return httpClientBuilder;
					}
				});
			}
			restClient = new RestHighLevelClient(builder);
			checkDataSource();
			initOK = true;
		}
	}

	private static void checkDataSource(){
		try {
			GetRequest getRequest = new GetRequest("test", "1");
			GetResponse getResponse = restClient.get(getRequest, RequestOptions.DEFAULT);
			logger.info("getDocument: " + JSON.toJSONString(getResponse));
		}catch (ElasticsearchStatusException e){
			logger.info("XElasticSearch.checkDataSource normal status: {}", e.getMessage());
		} catch (Exception e) {
			logger.error("XElasticSearch.checkDataSource.Exception:", e);
		}
	}
	
	public static RestHighLevelClient getClient() {
		return restClient;
	}
	
	public static boolean createIndex(String indexName) {
		try {
			GetIndexRequest getRequest = new GetIndexRequest(indexName);
			if(restClient.indices().exists(getRequest, RequestOptions.DEFAULT)) {
				logger.warn("createIndex.Index.existed:"+indexName);
				return true;
			}
			CreateIndexRequest createRequest = new CreateIndexRequest(indexName);
			CreateIndexResponse createResponse = restClient.indices().create(createRequest, RequestOptions.DEFAULT.toBuilder().build());
			return createResponse.isAcknowledged();
		}catch(Exception e) {
			logger.error("createIndex.Exception", e);
		}
		return false;
	}

	public static boolean createIndex(String indexName, XContentBuilder builder) {
		try {
			GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
			if(restClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
				logger.warn("createIndex.Index.existed:"+indexName);
				return true;
			}
			CreateIndexRequest createRequest = new CreateIndexRequest(indexName);
			createRequest.mapping(builder);
			CreateIndexResponse createResponse = restClient.indices().create(createRequest, RequestOptions.DEFAULT.toBuilder().build());
			return createResponse.isAcknowledged();
		}catch(Exception e) {
			logger.error("createIndex.Exception", e);
		}
		return false;
	}

	public static boolean getIndex(String indexName) {
		try {
			GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
			if(!restClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
				logger.warn("getIndex.Index.notExisted:"+indexName);
				return true;
			}
			GetIndexResponse getIndexResponse = restClient.indices().get(getIndexRequest, RequestOptions.DEFAULT);
			logger.info(getIndexResponse.getMappings().toString());
			return true;
		}catch(Exception e) {
			logger.error("getIndex.Exception", e);
		}
		return false;
	}

	public static boolean deleteIndex(String indexName) {
		try {
			GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
			if(!restClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
				logger.warn("deleteIndex.Index.notExisted:"+indexName);
				return true;
			}
			DeleteIndexRequest deleteRequest = new DeleteIndexRequest(indexName);
			AcknowledgedResponse deleteResponse = restClient.indices().delete(deleteRequest, RequestOptions.DEFAULT.toBuilder().build());
			return deleteResponse.isAcknowledged();
		}catch(Exception e) {
			logger.error("deleteIndex.Exception", e);
		}
		return false;
	}
	
	public static void close(){
		try {
			restClient.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}