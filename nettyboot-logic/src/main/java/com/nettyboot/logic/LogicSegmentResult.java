package com.nettyboot.logic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class LogicSegmentResult {
    public final String error;
    public final JSONObject data;
    public final JSONArray data2;

    public LogicSegmentResult() {
        this(null, null, null);
    }

    public LogicSegmentResult(String error) {
        this(error, null, null);
    }

    public LogicSegmentResult(JSONObject data) {
        this(null, data, null);
    }

    public LogicSegmentResult(JSONArray data) {
        this(null, null, data);
    }

    private LogicSegmentResult(String error, JSONObject data, JSONArray data2) {
        this.error = error;
        this.data = data;
        this.data2 = data2;
    }
}
