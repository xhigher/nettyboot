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
public class InternalException extends NokException {

    private static final int errorCode = ErrorCode.INTERNAL_ERROR;
    private static final long serialVersionUID = -6175691874195585586L;

    public InternalException() {
        super(errorCode);
    }

    public InternalException(String errinfo) {
        super(errorCode, errinfo);
    }

}
