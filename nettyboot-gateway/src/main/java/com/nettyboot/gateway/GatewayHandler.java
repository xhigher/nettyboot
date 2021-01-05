package com.nettyboot.gateway;

import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.ClientPeer;
import com.nettyboot.config.LogicConfig;
import com.nettyboot.config.LogicResultHelper;
import com.nettyboot.util.ClientUtil;
import com.nettyboot.webserver.WebBaseHandler;
import io.netty.channel.ChannelHandlerContext;

public class GatewayHandler extends WebBaseHandler {

    @Override
    protected void executeLogic(ChannelHandlerContext context) {
        try {
            ClientManager.submit(context.channel(), allowOrigin, requestInfo);
        }catch(Exception e){
            logger.error("executeLogic.Exception: {}, {}", this.requestInfo.toString(), e);
            this.sendResult(context, LogicResultHelper.errorRequest());
        }
    }

    @Override
    protected boolean checkRequest(ChannelHandlerContext context) {
        LogicConfig logicConfig = ServiceManager.getLogicConfig(requestInfo.getModule(), requestInfo.getAction(), requestInfo.getVersion());
        if(logicConfig == null){
            this.sendResult(context, LogicResultHelper.errorRequest());
            return false;
        }

        if(!logicConfig.getMethod().toString().equals(requestMethod.name())){
            this.sendResult(context, LogicResultHelper.errorMethod());
            return false;
        }

        if(!requestInfo.checkAllowedIP(logicConfig.getIps())){
            this.sendResult(context, LogicResultHelper.errorRequest());
        }

        if(logicConfig.getParameters().length > 0){
            String errinfo = this.requestInfo.checkRequiredParameters(logicConfig.getParameters());
            if(errinfo != null){
                this.sendResult(context, LogicResultHelper.errorParameter(errinfo));
                return false;
            }
        }

        String peerid = requestInfo.getPeerid();
        if(peerid == null){
            peerid = requestInfo.getParameters().getString(BaseDataKey.peerid);
            if(peerid == null || peerid.isEmpty()){
                this.sendResult(context, LogicResultHelper.errorValidation());
                return false;
            }
            requestInfo.setPeerid(peerid);
        }

        ClientPeer clientPeer = ClientUtil.checkPeerid(peerid);
        if(logicConfig.isPeerid()){
            if(clientPeer.error()){
                this.sendResult(context, LogicResultHelper.errorValidation());
                return false;
            }
        }

        String sessionid = requestInfo.getSessionid();
        if(sessionid == null){
            sessionid = requestInfo.getParameters().getString(BaseDataKey.sessionid);
            if(sessionid != null){
                requestInfo.setSessionid(sessionid);
            }
        }

        return true;
    }

}
