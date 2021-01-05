package com.nettyboot.rpcmessage;

import com.alibaba.fastjson.JSON;
import com.nettyboot.config.RequestInfo;

public class RequestMessageData extends RequestInfo {

    @Override
    public String toString(){
        return JSON.toJSONString(this);
    }
}
