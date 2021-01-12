package com.nettyboot.webserver;

import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.ClientPeer;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicResultHelper;
import com.nettyboot.logic.*;
import com.nettyboot.util.ClientUtil;
import io.netty.channel.ChannelHandlerContext;

public class WebDefaultHandler extends WebBaseHandler {

    public WebDefaultHandler(){
        super();
    }

    @Override
    protected boolean checkRequest(ChannelHandlerContext context){
        LogicAnnotation logicAnnotation = LogicManager.getLogicConfig(requestInfo.getModule(), requestInfo.getAction(), requestInfo.getVersion());
        if(logicAnnotation == null){
            this.sendResult(context, LogicResultHelper.errorRequest());
            return false;
        }

        if(!logicAnnotation.method().toString().equals(requestMethod.name())){
            this.sendResult(context, LogicResultHelper.errorMethod());
            return false;
        }

        if(!requestInfo.checkAllowedIP(logicAnnotation.ips())){
            this.sendResult(context, LogicResultHelper.errorRequest());
        }

        if(logicAnnotation.parameters().length > 0){
            String errinfo = requestInfo.checkRequiredParameters(logicAnnotation.parameters());
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
        if(logicAnnotation.peerid()){
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

    @Override
    protected void executeLogic(ChannelHandlerContext context){
        try {
            this.sendResult(context, LogicManager.executeLogic(requestInfo));
        } catch (Exception e) {
            this.sendResult(context, LogicResultHelper.errorRequest());
        }
    }
}
