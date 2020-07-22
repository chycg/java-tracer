package com.ali.trace.spy.inject;

import java.lang.instrument.Instrumentation;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

import com.ali.trace.spy.core.ConfigPool;
import com.ali.trace.spy.jetty.JettyServer;

/**
 * trace code inject
 *
 * @author nkhanlang@163.com
 *
 */
public class TraceInjecter {

	private final ClassLoader classLoader;
	private final Type type;
	private final Method start;
	private final Method end;
	private final ConfigPool pool = ConfigPool.getPool();

	public TraceInjecter(Instrumentation inst, Class<?> clasz, int port) throws NoSuchMethodException, SecurityException {
		classLoader = getClass().getClassLoader();
		type = Type.getType(clasz);
		start = Method.getMethod(clasz.getMethod("s", new Class<?>[] { String.class, String.class }));
		end = Method.getMethod(clasz.getMethod("e", new Class<?>[] { String.class, String.class }));
		pool.setInst(inst);
		pool.setWeaveClass(clasz);

		new JettyServer(port);
	}

	public byte[] getBytes(final ClassLoader loader, final String name, byte[] bytes) {
		Integer type = 0;
		try {
			if (name != null) {
				if (loader != null && loader != classLoader
						&& !name.startsWith("com/alibaba/jvm/sandbox/core/manager/impl/SandboxClassFileTransformer")
						&& !name.startsWith("com/google/gson/internal/reflect/ReflectionAccessor")
						|| loader == null && name.startsWith("java/com/alibaba/jvm/sandbox/spy")) {
					bytes = new CodeReader(loader, name, bytes, pool.isRedefine(loader, name)).getBytes();
					type = 1;
				}
			}
		} catch (TypeNotPresentException e) {
			type = 3;
		} catch (Throwable t) {
			type = 2;
		} finally {
			pool.addClass(loader, name, type);
		}

		return bytes;
	}

	class CodeReader extends ClassReader {

		private final ClassWriter classWriter;

		public CodeReader(final ClassLoader loader, final String name, byte[] bytes, final boolean redefine) {
			super(bytes);
			classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
				@Override
				public ClassLoader getClassLoader() {
					return loader;
				}

				@Override
				protected String getCommonSuperClass(final String type1, final String type2) {
					if (!redefine) {
						if (name.equals(type1)) {
							throw new TypeNotPresentException(type1, new Exception("circular define 1:[" + type1 + "," + type2 + "]"));
						}
						if (name.equals(type2)) {
							throw new TypeNotPresentException(type2, new Exception("circular define 2:[" + type1 + "," + type2 + "]"));
						}
					}

					return super.getCommonSuperClass(type1, type2);
				}
			};

			accept(new CodeVisitor(classWriter), EXPAND_FRAMES);
		}

		/**
		 * return modified bytes
		 */
		public byte[] getBytes() {
			return classWriter.toByteArray();
		}
	}

	/**
	 * weave code before and after each method
	 *
	 * @author hanlang.hl
	 *
	 */
	class CodeVisitor extends ClassVisitor {

		private String cName;

		public CodeVisitor(ClassVisitor cv) {
			super(Opcodes.ASM7, cv);
		}

		@Override
		public void visit(int paramInt1, int paramInt2, String paramString1, String paramString2, String paramString3,
				String[] paramArrayOfString) {
			cName = paramString1.replace('/', '.').replaceAll("\\$", ".");
			super.visit(paramInt1, paramInt2, paramString1, paramString2, paramString3, paramArrayOfString);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			if ((access & 256) != 0) {
				return super.visitMethod(access, name, desc, signature, exceptions);
			}
			return new FinallyAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
		}

		class FinallyAdapter extends AdviceAdapter {

			private String mName;
			private Label startFinally = new Label();
			private Label endFinally = new Label();

			public FinallyAdapter(MethodVisitor methodVisitor, int acc, String name, String desc) {
				super(Opcodes.ASM7, methodVisitor, acc, name, desc);
				this.mName = name.replaceAll("<|>|\\$", "");
			}

			@Override
			protected void onMethodEnter() {
				push(cName);
				push(mName);
				invokeStatic(type, start);
				mark(startFinally);
			}

			@Override
			public void visitMaxs(int maxStack, int maxLocals) {
				mark(endFinally);
				visitTryCatchBlock(startFinally, endFinally, mark(), null);
				onFinally();
				dup();
				throwException();
				super.visitMaxs(maxStack, maxLocals);
			}

			@Override
			protected void onMethodExit(int opcode) {
				if (opcode != ATHROW) {
					onFinally();
				}
			}

			private void onFinally() {
				push(cName);
				push(mName);
				invokeStatic(type, end);
			}
		}
	}
}
