package com.github.chenhq.agent.compile.visitor;

//import org.objectweb.asm.commons.Log.java
import com.github.chenhq.agent.compile.ClassMethod;
//import com.github.chenhq.agent.compile.InstrumentationContext;
//import com.github.chenhq.agent.compile.Log;
//import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
//import org.objectweb.asm.Type;
import java.text.MessageFormat;
//import java.util.Collection;
//import java.util.Iterator;

public class WrapMethodClassVisitor extends ClassVisitor {
	// private final InstrumentationContext context;
	// private final Log log;

	public WrapMethodClassVisitor(int api, ClassVisitor cv) {
		super(api, cv);
		// this.context = context;
		// this.log = log;
	}

	public MethodVisitor visitMethod(int access, String name, String desc,
			String sig, String[] exceptions) {
		// if (this.context.isSkippedMethod(name, desc)) {
		// return super.visitMethod(access, name, desc, sig, exceptions);
		// }

		return new MethodWrapMethodVisitor(super.visitMethod(access, name,
				desc, sig, exceptions), access, name, desc);
	}

	private static final class MethodWrapMethodVisitor extends GeneratorAdapter {
		private final String name;
		private final String desc;
		// private final InstrumentationContext context;
		// private final Log log;
		private boolean newInstructionFound = false;
		private boolean dupInstructionFound = false;

		public MethodWrapMethodVisitor(MethodVisitor mv, int access,
				String name, String desc) {
			super(mv, access, name, desc);
			this.name = name;
			this.desc = desc;
			// this.context = context;
			// this.log = log;
		}

		public void visitMethodInsn(int opcode, String owner, String name,
				String desc) {
			if (opcode == 186) {
				MessageFormat
						.format("[{0}] INVOKEDYNAMIC instruction cannot be instrumented",
								new Object[] { "this.context.getClassName()"
										.replaceAll("/", ".") });
				super.visitMethodInsn(opcode, owner, name, desc);
				return;
			}

			if ((!tryReplaceCallSite(opcode, owner, name, desc))
					&& (!tryWrapReturnValue(opcode, owner, name, desc)))
				super.visitMethodInsn(opcode, owner, name, desc);
		}

		public void visitTypeInsn(int opcode, String type) {
			if (opcode == 187) {
				this.newInstructionFound = true;
				this.dupInstructionFound = false;
			}

			super.visitTypeInsn(opcode, type);
		}

		public void visitInsn(int opcode) {
			if (opcode == 89) {
				this.dupInstructionFound = true;
			}

			super.visitInsn(opcode);
		}

		private boolean tryWrapReturnValue(int opcode, String owner,
				String name, String desc) {
			// ClassMethod method = new ClassMethod(owner, name, desc);
			//
			// ClassMethod wrappingMethod =
			// this.context.getMethodWrapper(method);
			// if (wrappingMethod != null) {
			// this.log.info(MessageFormat.format("[{0}] wrapping call to {1} with {2}",
			// new Object[] { this.context.getClassName().replaceAll("/", "."),
			// method.toString(), wrappingMethod.toString() }));
			// super.visitMethodInsn(opcode, owner, name, desc);
			// super.visitMethodInsn(184, wrappingMethod.getClassName(),
			// wrappingMethod.getMethodName(), wrappingMethod.getMethodDesc());
			// this.context.markModified();
			// return true;
			// }

			return false;
		}

		private boolean tryReplaceCallSite(int opcode, String owner,
				String name, String desc) {
			ClassMethod method = new ClassMethod(owner, name, desc);
			boolean isSuperCallInOverride = (opcode == 183)
					&& (!owner.equals("ClassName")) && (this.name.equals(name))
					&& (this.desc.equals(desc));

			if (isSuperCallInOverride) {
				System.out
						.println(MessageFormat
								.format("[{0}] skipping call site replacement for super call in overriden method: {1}:{2}",
										new Object[] {
												"this.context.getClassName()"
														.replaceAll("/", "."),
												this.name, this.desc }));

				return false;
			}

			String new_owner = "com/newrelic/agent/android/instrumentation/HttpInstrumentation";
			String new_name = "execute";
			String new_desc = "(Lorg/apache/http/client/HttpClient;" +
			                  "Lorg/apache/http/client/methods/HttpUriRequest;" +
					          "Lorg/apache/http/client/ResponseHandler;)" +
			                  "Ljava/lang/Object;";
			ClassMethod replacementMethod = new ClassMethod(new_owner, new_name, new_desc);
			Method newMethod = new Method(replacementMethod.getMethodName(),
					replacementMethod.getMethodDesc());

			System.out
					.println(MessageFormat
							.format("[{0}] replacing call to {1} with {2} (with instance check)",
									new Object[] {
											"this.context.getClassName()"
													.replaceAll("/", "."),
											method.toString(),
											replacementMethod.toString() }));
			// [com.github.chenhq.httpclient.service.UserServiceImpl]
			// org/apache/http/client/HttpClient.execute(
			// Lorg/apache/http/client/methods/HttpUriRequest;
			// Lorg/apache/http/client/ResponseHandler;)
			// Ljava/lang/Object;
			// com/newrelic/agent/android/instrumentation/HttpInstrumentation.execute(
			// Lorg/apache/http/client/HttpClient;
			// Lorg/apache/http/client/methods/HttpUriRequest;
			// Lorg/apache/http/client/ResponseHandler;)
			// Ljava/lang/Object;

			Method originalMethod = new Method(name, desc);

			int[] locals = new int[originalMethod.getArgumentTypes().length];
			for (int i = locals.length - 1; i >= 0; i--) {
				locals[i] = newLocal(originalMethod.getArgumentTypes()[i]);
				storeLocal(locals[i]);
			}

			dup();

			instanceOf(newMethod.getArgumentTypes()[0]);
			Label isInstanceOfLabel = new Label();
			visitJumpInsn(154, isInstanceOfLabel);

			for (int local : locals) {
				loadLocal(local);
			}
			super.visitMethodInsn(opcode, owner, name, desc);

			Label end = new Label();
			visitJumpInsn(167, end);
			visitLabel(isInstanceOfLabel);

			checkCast(newMethod.getArgumentTypes()[0]);

			for (int local : locals) {
				loadLocal(local);
			}
			super.visitMethodInsn(184, replacementMethod.getClassName(),
					replacementMethod.getMethodName(),
					replacementMethod.getMethodDesc());

			visitLabel(end);

			// this.context.markModified();
			return true;

			// return false;
		}
	}
}
