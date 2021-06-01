package com.nettyboot.rpcmessage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import com.nettyboot.util.StringUtil;


public class SimpleMessage implements Serializable {

    private static final long serialVersionUID = -9018048318331786494L;

    private String msgid;
    private MessageType type;
    private Object data;

    public SimpleMessage(){

    }

    public SimpleMessage(MessageType type) {
        this.type = type;
        this.msgid = System.currentTimeMillis() + StringUtil.randomString(7, true);
    }

    public SimpleMessage(MessageType type, String msgid){
        this.type = type;
        this.msgid = msgid;
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString(){
        return JSON.toJSONString(this);
    }
}
