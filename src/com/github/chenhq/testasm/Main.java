package com.github.chenhq.testasm;

import org.objectweb.asm.commons.Method;

public class Main {

	public static void main(String[] args) {
		String resp = HttpClient1.execHttp();
		System.out.println("Resp: " + resp);

	}
	
    private boolean tryReplaceCallSite(int opcode, String owner, String name, String desc)
    {
//      Collection replacementMethods = this.context.getCallSiteReplacements(owner, name, desc);
//
//      if (replacementMethods.isEmpty()) {
//        return false;
//      }

      ClassMethod method = new ClassMethod(owner, name, desc);


//      Iterator i$ = replacementMethods.iterator();



//        boolean isSuperCallInOverride = (opcode == 183) && (!owner.equals(this.context.getClassName())) && (this.name.equals(name)) && (this.desc.equals(desc));
//
//        if (isSuperCallInOverride) {
//          this.log.info(MessageFormat.format("[{0}] skipping call site replacement for super call in overriden method: {1}:{2}", new Object[] { this.context.getClassName().replaceAll("/", "."), this.name, this.desc }));
//
//          return false;
//        }

//        if ((opcode == 183) && (name.equals("<init>"))) {
//          Method originalMethod = new Method(name, desc);
//
//          if ((this.context.getSuperClassName() != null) && (this.context.getSuperClassName().equals(owner)))
//          {
//            this.log.info(MessageFormat.format("[{0}] skipping call site replacement for class extending {1}", new Object[] { this.context.getFriendlyClassName(), this.context.getFriendlySuperClassName() }));
//            return false;
//          }
//
//          this.log.info(MessageFormat.format("[{0}] tracing constructor call to {1} - {2}", new Object[] { this.context.getFriendlyClassName(), method.toString(), owner }));
//
//          int[] locals = new int[originalMethod.getArgumentTypes().length];
//          for (int i = locals.length - 1; i >= 0; i--) {
//            locals[i] = newLocal(originalMethod.getArgumentTypes()[i]);
//            storeLocal(locals[i]);
//          }
//
//          visitInsn(87);
//
//          if ((this.newInstructionFound) && (this.dupInstructionFound)) {
//            visitInsn(87);
//          }
//
//          for (int local : locals) {
//            loadLocal(local);
//          }
//
//          super.visitMethodInsn(184, replacementMethod.getClassName(), replacementMethod.getMethodName(), replacementMethod.getMethodDesc());
//
//          if ((this.newInstructionFound) && (!this.dupInstructionFound))
//            visitInsn(87);
//        }
//        else if (opcode == 184) {
//          this.log.info(MessageFormat.format("[{0}] replacing static call to {1} with {2}", new Object[] { this.context.getClassName().replaceAll("/", "."), method.toString(), replacementMethod.toString() }));
//
//          super.visitMethodInsn(184, replacementMethod.getClassName(), replacementMethod.getMethodName(), replacementMethod.getMethodDesc());
//        } else {
          // Method newMethod = new Method(replacementMethod.getMethodName(), replacementMethod.getMethodDesc());
          Method newMethod = new Method("", "");

          //this.log.info(MessageFormat.format("[{0}] replacing call to {1} with {2} (with instance check)", new Object[] { this.context.getClassName().replaceAll("/", "."), method.toString(), replacementMethod.toString() }));

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
          super.visitMethodInsn(184, replacementMethod.getClassName(), replacementMethod.getMethodName(), replacementMethod.getMethodDesc());

          visitLabel(end);
        }

        this.context.markModified();
        return true;
//      }

      return false;
    }
  }	

}
