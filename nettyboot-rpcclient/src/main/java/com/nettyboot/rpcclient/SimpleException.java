package com.nettyboot.rpcclient;

public class SimpleException extends RuntimeException {

    private static final long serialVersionUID = 3300883970826150060L;

    private final ExceptionType type;
    
    public enum ExceptionType{
    	internal,
    	timeout,
    	connection,
    	interrupted,
    }
    
    public SimpleException() {
        super();
    	this.type = ExceptionType.internal;
    }

    public SimpleException(ExceptionType type, String message, Throwable cause) {
        super(message, cause);
    	this.type = type;
    }

    public SimpleException(ExceptionType type, String message) {
        super(message);
    	this.type = type;
    }

    public SimpleException(ExceptionType type, Throwable cause) {
        super(cause);
    	this.type = type;
    }
    
    public SimpleException(ExceptionType type) {
        super();
    	this.type = type;
    }
    
    public boolean isInternalError() {
    	return this.type == ExceptionType.internal;
    }
    
    public boolean isTimeoutError() {
    	return this.type == ExceptionType.timeout;
    }

}
