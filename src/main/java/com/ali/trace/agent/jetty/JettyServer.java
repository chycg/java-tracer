package com.ali.trace.agent.jetty;

import java.net.InetSocketAddress;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.ali.trace.agent.jetty.support.HandlerConfig;

/**
 * @author nkhanlang@163.com
 */
public class JettyServer {

	public JettyServer(int port) {
		ServletContextHandler context = new ServletContextHandler(null, ModuleHttpServlet.ROOT, ServletContextHandler.NO_SESSIONS);
		context.setClassLoader(JettyServer.class.getClassLoader());
		context.addServlet(new ServletHolder(new ModuleHttpServlet(HandlerConfig.getInstance())), "/*");

		Server httpServer = new Server(new InetSocketAddress(port));
		if (httpServer.getThreadPool() instanceof QueuedThreadPool) {
			final QueuedThreadPool qtp = (QueuedThreadPool) httpServer.getThreadPool();
			qtp.setName("tracer-jetty-qtp" + qtp.hashCode());
		}
		httpServer.setHandler(context);
		try {
			httpServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
