package com.nettyboot.upload;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.ClientPeer;
import com.nettyboot.config.LogicResultHelper;
import com.nettyboot.util.ClientUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;


public abstract class UploadBaseHandler extends SimpleChannelInboundHandler<HttpObject> {

	protected static Logger logger = LoggerFactory.getLogger(UploadBaseHandler.class);

	private String allowOrigin = null;
	private UploadRequestInfo uploadRequestInfo = null;
	private ResultFileInfo resultFileInfo = null;

	protected Map<String, String> requestHeaders = null;
	protected JSONObject requestParameters = null;

	private static HttpDataFactory httpDataFactory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
	private HttpPostRequestDecoder postDecoder = null;

	static {
		DiskFileUpload.deleteOnExitTemporaryFile = true;
		DiskFileUpload.baseDirectory = null;
		DiskAttribute.deleteOnExitTemporaryFile = true;
		DiskAttribute.baseDirectory = null;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (postDecoder != null) {
			postDecoder.cleanFiles();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		logger.error("XHandler.exceptionCaught!", cause);
		ctx.channel().close();
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		if (msg instanceof io.netty.handler.codec.http.HttpRequest) {
			io.netty.handler.codec.http.HttpRequest request = (HttpRequest) msg;

			if (HttpUtil.is100ContinueExpected(request)) {
				send100Continue(ctx);
			}

			if (!request.decoderResult().isSuccess()) {
				sendFatalErrorResult(ctx, HttpResponseStatus.BAD_REQUEST);
				return;
			}

			HttpHeaders header = request.headers();
			allowOrigin = header.get(HttpHeaderNames.ORIGIN);
			if (allowOrigin != null) {
				if(!UploadConfig.checkAllowOrigin(allowOrigin)){
					allowOrigin = null;
				}
			}

			logger.info("XHandler.requestMethod=" + request.method());
			if (request.method() == HttpMethod.OPTIONS) {
				sendOptionsResult(ctx);
				return;
			}

			if (request.method() != HttpMethod.POST) {
				sendFatalErrorResult(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
				return;
			}

			QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
			String path = queryDecoder.path();
			logger.info("XHandler.requestPath=" + path);
			if (path == null) {
				sendFatalErrorResult(ctx, HttpResponseStatus.FORBIDDEN);
				return;
			}

			uploadRequestInfo = new UploadRequestInfo(path);

			if (uploadRequestInfo.module() == null) {
				sendFatalErrorResult(ctx, HttpResponseStatus.FORBIDDEN);
				return;
			}

			this.requestHeaders = UploadConfig.checkHeaders(header);

			this.cleanRequestParameters(queryDecoder.parameters());

			try {
				postDecoder = new HttpPostRequestDecoder(httpDataFactory, request);
			} catch (ErrorDataDecoderException e) {
				logger.error("postDecoder.ErrorDataDecoderException", e);
				sendResult(ctx, LogicResultHelper.errorInternal());
				return;
			}
		}

		if (postDecoder != null) {
			if (msg instanceof HttpContent) {
				HttpContent chunk = (HttpContent) msg;
				try {
					postDecoder.offer(chunk);
				} catch (ErrorDataDecoderException e1) {
					logger.error("postDecoder.ErrorDataDecoderException", e1);
					postDecoder.destroy();
					postDecoder = null;
					sendResult(ctx, LogicResultHelper.errorInternal());
					return;
				}

				boolean isOK = readHttpDataChunkByChunk();

				if (chunk instanceof LastHttpContent) {
					postDecoder.destroy();
					postDecoder = null;
					if (isOK) {
						handleBusiness(ctx);
					} else {
						sendResult(ctx, LogicResultHelper.errorInternal());
					}
				}
			}
		}
	}

	private boolean readHttpDataChunkByChunk() {
		boolean isOK = false;
		while (postDecoder != null && postDecoder.hasNext()) {
			InterfaceHttpData httpData = postDecoder.next();
			if (httpData != null) {
				try {
					if (httpData.getHttpDataType() == HttpDataType.FileUpload) {
						FileUpload fileUpload = (FileUpload) httpData;
						if (fileUpload.isCompleted()) {
							resultFileInfo = UploadConfig.handleImageFromHttpData(fileUpload, uploadRequestInfo);
						}
					}
				} catch (Exception e) {
					logger.error("readHttpDataChunkByChunk.Exception", e);
				} finally {
//                    if (httpData != null) {
//                        httpData.release();
//                    }
				}
			}
		}
		return isOK;
	}



	private void cleanRequestParameters(Map<String, List<String>> params) {
		if (requestParameters == null) {
			requestParameters = new JSONObject();
		}

		List<String> tpv = null;
		String pv = null;
		for (String pn : params.keySet()) {
			tpv = params.get(pn);
			if (tpv.size() > 1) {
				JSONArray temp = new JSONArray();
				temp.add(tpv);
				pv = temp.toJSONString();
			} else if (params.get(pn).size() == 1) {
				pv = params.get(pn).get(0).trim();
			} else {
				pv = "";
			}
			requestParameters.put(pn, pv);
		}
	}

	private static void send100Continue(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE, Unpooled.EMPTY_BUFFER);
		ctx.write(response);
	}

	private void sendFatalErrorResult(ChannelHandlerContext ctx, HttpResponseStatus status) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
		if (allowOrigin != null) {
			response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, allowOrigin);
		}
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	private void deleteFile(String filepath) {
		File file = new File(uploadRequestInfo.getSavePath() + filepath);
		if (file.isFile() && file.exists()) {
			file.delete();
		}
	}

	private void sendOptionsResult(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		if (allowOrigin != null) {
			response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, allowOrigin);
			String allowMethods = UploadConfig.getAccessControlAllowMethods();
			if(allowMethods != null && !allowMethods.isEmpty()){
				response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, allowMethods);
			}
			String allowHeaders = UploadConfig.getAccessControlAllowHeaders();
			if(allowHeaders != null && !allowHeaders.isEmpty()){
				response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, allowHeaders);
			}
		}
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	private void sendResult(ChannelHandlerContext ctx, String resultString) {
		logger.info("sendResult.resultString=" + resultString);
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(resultString.getBytes()));
		if (allowOrigin != null) {
			response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, allowOrigin);
		}
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=" + CharsetUtil.UTF_8.toString());
		response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	protected boolean checkUserSession(String peerid, String sessionid){
		return true;
	}

	protected boolean saveFileInfo(ResultFileInfo resultFileInfo){
		return true;
	}

	private void handleBusiness(ChannelHandlerContext ctx) {
		try {
			String peerid = requestHeaders.get(BaseDataKey.peerid);
			if(peerid == null){
				peerid = requestParameters.getString(BaseDataKey.peerid);
			}
			ClientPeer clientPeer = ClientUtil.checkPeerid(peerid);
			if(clientPeer.error()){
				this.sendResult(ctx, LogicResultHelper.errorValidation());
				return;
			}

			String sessionid = requestHeaders.get(BaseDataKey.sessionid);
			if(sessionid == null){
				sessionid = requestParameters.getString(BaseDataKey.sessionid);
			}

			if(!checkUserSession(peerid, sessionid)){
				this.sendResult(ctx, LogicResultHelper.errorInternal());
				return;
			}

			if(!this.saveFileInfo(resultFileInfo)){
				this.sendResult(ctx, LogicResultHelper.errorInternal());
				return;
			}

			this.sendResult(ctx, LogicResultHelper.success(resultFileInfo));
		} catch (Exception e) {
			this.deleteFile(resultFileInfo.getFilepath());
			logger.error("handleBusiness.Exception", e);
			this.sendResult(ctx, LogicResultHelper.errorInternal());
			return;
		} finally {

		}

	}
}