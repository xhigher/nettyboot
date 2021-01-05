package com.nettyboot.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogicAnnotation {

    String module() default "";

    String action() default "";

    int version() default 1;

    LogicMethod method() default LogicMethod.GET;
    
    String[] parameters() default {};
    
    boolean peerid() default true;
    
    String[] ips() default {};

}