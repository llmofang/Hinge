package com.llmofang.hinge.agent.compile.visitor;

import com.github.chenhq.agent.compile.ClassMethod;
import com.github.chenhq.agent.compile.InstrumentationContext;
import com.github.chenhq.agent.compile.Log;
//import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.Type;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;
import org.objectweb.asm.Opcodes;

public class WrapMethodClassVisitor extends ClassVisitor {
	private final InstrumentationContext context;
	private final Log log;

	public WrapMethodClassVisitor(ClassVisitor cv,
			InstrumentationContext context, Log log) {
		super(Opcodes.ASM4, cv);
		this.context = context;
		this.log = log;
	}

	public MethodVisitor visitMethod(int access, String name, String desc,
			String sig, String[] exceptions) {
		if (this.context.isSkippedMethod(name, desc)) {
			return super.visitMethod(access, name, desc, sig, exceptions);
		}

		return new MethodWrapMethodVisitor(super.visitMethod(access, name,
				desc, sig, exceptions), access, name, desc, this.context,
				this.log);
	}

	private static final class MethodWrapMethodVisitor extends GeneratorAdapter {
		private final String name;
		private final String desc;
		private final InstrumentationContext context;
		private final Log log;
		private boolean newInstructionFound = false;
		private boolean dupInstructionFound = false;

		public MethodWrapMethodVisitor(MethodVisitor mv, int access,
				String name, String desc, InstrumentationContext context,
				Log log) {
			
			super(mv, access, name, desc);
			this.name = name;
			this.desc = desc;
			this.context = context;
			this.log = log;
		}

		public void visitMethodInsn(int opcode, String owner, String name,
				String desc) {
			if (opcode == 186) {
				this.log.warning(MessageFormat
						.format("[{0}] INVOKEDYNAMIC instruction cannot be instrumented",
								new Object[] { this.context.getClassName()
										.replaceAll("/", ".") }));
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
			ClassMethod method = new ClassMethod(owner, name, desc);

			ClassMethod wrappingMethod = this.context.getMethodWrapper(method);
			if (wrappingMethod != null) {
				this.log.info(MessageFormat.format(
						"[{0}] wrapping call to {1} with {2}",
						new Object[] {
								this.context.getClassName()
										.replaceAll("/", "."),
								method.toString(), wrappingMethod.toString() }));
				super.visitMethodInsn(opcode, owner, name, desc);
				super.visitMethodInsn(184, wrappingMethod.getClassName(),
						wrappingMethod.getMethodName(),
						wrappingMethod.getMethodDesc());
				this.context.markModified();
				return true;
			}

			return false;
		}

		private boolean tryReplaceCallSite(int opcode, String owner,
				String name, String desc) {
			Collection<?> replacementMethods = this.context
					.getCallSiteReplacements(owner, name, desc);

			if (replacementMethods.isEmpty()) {
				return false;
			}

			ClassMethod method = new ClassMethod(owner, name, desc);

			Iterator<?> i$ = replacementMethods.iterator();
			if (i$.hasNext()) {
				ClassMethod replacementMethod = (ClassMethod) i$.next();

				boolean isSuperCallInOverride = (opcode == 183)
						&& (!owner.equals(this.context.getClassName()))
						&& (this.name.equals(name)) && (this.desc.equals(desc));

				if (isSuperCallInOverride) {
					this.log.info(MessageFormat
							.format("[{0}] skipping call site replacement for super call in overriden method: {1}:{2}",
									new Object[] {
											this.context.getClassName()
													.replaceAll("/", "."),
											this.name, this.desc }));

					return false;
				}

				if ((opcode == 183) && (name.equals("<init>"))) {
					Method originalMethod = new Method(name, desc);

					if ((this.context.getSuperClassName() != null)
							&& (this.context.getSuperClassName().equals(owner))) {
						this.log.info(MessageFormat
								.format("[{0}] skipping call site replacement for class extending {1}",
										new Object[] {
												this.context
														.getFriendlyClassName(),
												this.context
														.getFriendlySuperClassName() }));
						return false;
					}

					this.log.info(MessageFormat.format(
							"[{0}] tracing constructor call to {1} - {2}",
							new Object[] { this.context.getFriendlyClassName(),
									method.toString(), owner }));

					int[] locals = new int[originalMethod.getArgumentTypes().length];
					for (int i = locals.length - 1; i >= 0; i--) {
						locals[i] = newLocal(originalMethod.getArgumentTypes()[i]);
						storeLocal(locals[i]);
					}

					visitInsn(87);

					if ((this.newInstructionFound)
							&& (this.dupInstructionFound)) {
						visitInsn(87);
					}

					for (int local : locals) {
						loadLocal(local);
					}

					super.visitMethodInsn(184,
							replacementMethod.getClassName(),
							replacementMethod.getMethodName(),
							replacementMethod.getMethodDesc());

					if ((this.newInstructionFound)
							&& (!this.dupInstructionFound))
						visitInsn(87);
				} else if (opcode == 184) {
					this.log.info(MessageFormat.format(
							"[{0}] replacing static call to {1} with {2}",
							new Object[] {
									this.context.getClassName().replaceAll("/",
											"."), method.toString(),
									replacementMethod.toString() }));

					super.visitMethodInsn(184,
							replacementMethod.getClassName(),
							replacementMethod.getMethodName(),
							replacementMethod.getMethodDesc());
				} else {
					Method newMethod = new Method(
							replacementMethod.getMethodName(),
							replacementMethod.getMethodDesc());

					this.log.info(MessageFormat
							.format("[0] replacing call to {0} with {1} (with instance check)",
									new Object[] {
											//this.context.getClassName().replaceAll("/", "."),
											method.toString(),
											replacementMethod.toString() }));

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
					super.visitMethodInsn(184,
							replacementMethod.getClassName(),
							replacementMethod.getMethodName(),
							replacementMethod.getMethodDesc());

					visitLabel(end);
				}

				this.context.markModified();
				return true;
			}

			return false;
		}
	}
}
