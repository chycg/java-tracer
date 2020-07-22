package com.ali.trace.spy.jetty.handler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;


/**
 * @author nkhanlang@163.com
 * @date 2019-08-21 23:32
 */
public interface ITraceHttpHandler {

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface TracerPath {
        String value() default "";
        int order() default 0;
    }

    @Target({ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface TraceParam {
        String value() default "";
    }

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface TraceView {

    }

    public class ModelMap extends HashMap<String, Object>{

		private static final long serialVersionUID = -7495222750472825696L;

    }
}
