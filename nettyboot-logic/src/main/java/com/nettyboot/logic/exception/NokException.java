package com.nettyboot.logic.exception;

import com.nettyboot.config.ErrorCode;

/**
 * 一句话描述该类的功能
 *
 * @author : lmr2015
 * @version : v1.0
 * @Description : // TODO
 * @createTime : 2021/6/1 17:38
 * @updateTime : 2021/6/1 17:38
 */
public class NokException extends RuntimeException {

    private static final long serialVersionUID = 4492104702837362697L;

    private int errcode;
    private String errinfo;
    private Object data;

    public NokException(int errcode) {
        this(errcode, null, null);
    }

    public NokException(String errinfo) {
        this(ErrorCode.NOK, errinfo, null);
    }

    public NokException(int errcode, String errinfo) {
        this(errcode, errinfo, null);
    }

    public NokException(int errcode, String errinfo, Object data) {
        this.errcode = errcode;
        this.errinfo = errinfo;
        this.data = data;
    }

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getErrinfo() {
        return errinfo;
    }

    public void setErrinfo(String errinfo) {
        this.errinfo = errinfo;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
