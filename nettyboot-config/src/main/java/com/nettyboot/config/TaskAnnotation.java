package com.nettyboot.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TaskAnnotation {

    String name() default "";

    boolean test() default false;

    TaskType type() default TaskType.single;
    
    String starttime() default "";
    
    long delay() default 30;
    
    long interval() default 60;

    TimeUnit timeunit() default TimeUnit.SECONDS;

}