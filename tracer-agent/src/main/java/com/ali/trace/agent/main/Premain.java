package com.ali.trace.agent.main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import com.ali.trace.agent.inject.TraceEnhance;
import com.ali.trace.agent.inject.TraceTransformer;
import com.ali.trace.agent.loader.SpyClassLoader;
import com.ali.trace.spy.inject.TraceInjecter;

/**
 * @author nkhanlang@163.com
 */
public class Premain {

	private static final String PATH = "/META-INF/lib";
	private static int port = 18902;

	private static final SpyClassLoader spyLoader = new SpyClassLoader(ClassLoader.getSystemClassLoader());
	private static final AtomicReference<TraceInjecter> INJECT = new AtomicReference<TraceInjecter>();
	private static Set<String> CANT_TRANSFORM = new HashSet<String>();

	public static void premain(String args, Instrumentation inst) {
		TraceInjecter inject = loadSpyJar(inst);
		if (inject == null) {
			System.out.println("inject is null");
			return;
		}

		inst.addTransformer(new TraceTransformer(inject), true);
	}

	/**
	 * agent loaded by bootstrap loader, spy and all dependent jars loaded by spy loader
	 */
	private static TraceInjecter loadSpyJar(Instrumentation inst) {
		TraceInjecter inject = INJECT.get();
		if (inject != null)
			return inject;

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

			inject = new TraceInjecter(inst, TraceEnhance.class, port);
			INJECT.set(inject);
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return inject;
	}

	public static void agentmain(String args, Instrumentation inst) {
		CoreEngine.process(args, inst);
		Class<?>[] classes = inst.getAllLoadedClasses();
		for (Class<?> clasz : classes) {
			String name = clasz.getName();
			try {
				if (clasz.getClassLoader() != null && clasz.getClassLoader().getParent() != null && !CANT_TRANSFORM.contains(name)) {
					inst.retransformClasses(new Class<?>[] { clasz });
				}
			} catch (Throwable t) {
				CANT_TRANSFORM.add(name);
			}
		}
	}

}
