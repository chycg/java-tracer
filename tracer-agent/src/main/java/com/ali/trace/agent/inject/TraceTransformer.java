package com.ali.trace.agent.inject;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import com.ali.trace.spy.inject.TraceInjecter;

public class TraceTransformer implements ClassFileTransformer {

	private final TraceInjecter injecter;

	public TraceTransformer(TraceInjecter injecter) {
		this.injecter = injecter;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> rawClass, ProtectionDomain domain, byte[] bytes) {
		return injecter.getBytes(loader, className, bytes);
	}
}
