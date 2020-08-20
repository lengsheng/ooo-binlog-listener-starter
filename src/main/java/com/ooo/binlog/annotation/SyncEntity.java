package com.ooo.binlog.annotation;

import com.ooo.binlog.processor.DefaultBinLogProcessor;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface SyncEntity {

    Class mapperClass();

    String listenerHost();

//    String database() default "";

    Class listenerClass() default DefaultBinLogProcessor.class;

    String id() default "id";

}