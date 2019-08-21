package com.ali.trace.spy.jetty;

import com.ali.trace.spy.jetty.handler.IndexHandler;
import com.ali.trace.spy.jetty.handler.StaticHandler;
import com.ali.trace.spy.jetty.support.HandlerConfig;
import  org.eclipse.jetty.servlet.ServletContextHandler;

import java.net.InetSocketAddress;

import javax.servlet.Servlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.ali.trace.spy.jetty.handler.ClassHandler;
import com.ali.trace.spy.jetty.handler.ThreadHandler;
import com.ali.trace.spy.jetty.handler.TraceHandler;

public class JettyServer {

    public JettyServer(int port) {
        
        final ServletContextHandler context = new ServletContextHandler(null, ModuleHttpServlet.ROOT, ServletContextHandler.NO_SESSIONS);

        context.setClassLoader(JettyServer.class.getClassLoader());
        final String pathSpec = "/*";
        context.addServlet(new ServletHolder(new ModuleHttpServlet(HandlerConfig.getInstance())), pathSpec);

        Server httpServer = new Server(new InetSocketAddress(port));
        if (httpServer.getThreadPool() instanceof QueuedThreadPool) {
            final QueuedThreadPool qtp = (QueuedThreadPool)httpServer.getThreadPool();
            qtp.setName("tracer-jetty-qtp" + qtp.hashCode());
        }
        httpServer.setHandler(context);
        try {
            httpServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public static void main(String[] args) {
        new JettyServer(8080);
    }
}
