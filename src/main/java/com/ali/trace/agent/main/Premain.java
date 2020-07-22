package com.ali.trace.agent.main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import com.ali.trace.agent.inject.TraceEnhance;
import com.ali.trace.agent.inject.TraceTransformer;
import com.ali.trace.agent.loader.SpyClassLoader;

/**
 * @author nkhanlang@163.com
 */
public class Premain {

	private static int port = 18902;

	private static String traceClassName = "com.ali.trace.agent.main.TraceInjecter";

	private static final SpyClassLoader spyLoader = new SpyClassLoader(ClassLoader.getSystemClassLoader());
	private static final AtomicReference<Object> INJECT = new AtomicReference<>();

	public static void premain(String args, Instrumentation inst) throws NoSuchMethodException, SecurityException {
		Object inject = loadSpyJar(inst);
		if (inject == null) {
			System.out.println("inject is null");
			return;
		}

		inst.addTransformer(new TraceTransformer(inject), true);
	}

	/**
	 * agent loaded by bootstrap loader, spy and all dependent jars loaded by spy loader
	 */
	private static Object loadSpyJar(Instrumentation inst) {
		Object inject = INJECT.get();
		if (inject != null)
			return inject;

		String PATH = "/META-INF/lib";
		String path = Premain.class.getResource(PATH).getPath();
		if (path.endsWith(PATH)) {
			path = path.substring(0, path.length() - PATH.length() - 1);
		}

		try {
			try (JarInputStream jarInput = new JarInputStream(new URL(path).openStream())) {
				JarEntry entry = null;
				while ((entry = jarInput.getNextJarEntry()) != null) {
					String entryName = "/" + entry.getName();
					if (entryName.startsWith(PATH) && entryName.endsWith(".jar")) {
						ByteArrayOutputStream bytes = new ByteArrayOutputStream();
						int chunk = 0;
						byte[] data = new byte[256];
						while (-1 != (chunk = jarInput.read(data))) {
							bytes.write(data, 0, chunk);
						}

						data = bytes.toByteArray();
						spyLoader.load(new ByteArrayInputStream(data, 0, data.length));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			Class<?> injectClass = spyLoader.loadClass(traceClassName);
			inject = injectClass.getConstructor(Instrumentation.class, Class.class, int.class).newInstance(inst, TraceEnhance.class, port);

			INJECT.set(inject);
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return inject;
	}
}
