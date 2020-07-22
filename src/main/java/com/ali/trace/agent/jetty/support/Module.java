package com.ali.trace.agent.jetty.support;


import java.lang.reflect.Method;
import java.util.regex.Pattern;

import com.ali.trace.agent.jetty.handler.ITraceHttpHandler;
/**
 * @author nkhanlang@163.com
 * @date 2019-08-21 23:30
 */
public class Module implements Comparable<Module> {
    private Pattern pattern;
    private Method method;
    private ITraceHttpHandler httpHandler;
    private int order;
    private String path;

    public Module(String path, Method method, ITraceHttpHandler httpHandler, int order) {
        this.path = path;
        this.method = method;
        this.httpHandler = httpHandler;
        this.order = order;
        this.pattern = Pattern.compile("^" + path + "$");
    }
    public ITraceHttpHandler getHttpHandler(){
        return httpHandler;
    }
    public Method getMethod(){
        return method;
    }

    public boolean match(String path) {
        return pattern.matcher(path).find();
    }

    public int compareTo(Module o) {
        int compare = order - o.order;
        if (compare == 0) {
            compare = o.path.length() - path.length();
        }
        if (compare == 0) {
            compare = o.path.compareTo(path);
        }
        return compare;
    }
}