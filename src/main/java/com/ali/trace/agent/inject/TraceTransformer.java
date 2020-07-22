package com.ali.trace.agent.inject;

import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

public class TraceTransformer implements ClassFileTransformer {

	private final Object injecter;

	private final Method method;

	public TraceTransformer(Object injecter) throws NoSuchMethodException, SecurityException {
		this.injecter = injecter;
		method = injecter.getClass().getMethod("getBytes", ClassLoader.class, String.class, byte[].class);
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> rawClass, ProtectionDomain domain, byte[] bytes) {
		try {
			return (byte[]) method.invoke(injecter, loader, className, bytes);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return bytes;
	}
}
