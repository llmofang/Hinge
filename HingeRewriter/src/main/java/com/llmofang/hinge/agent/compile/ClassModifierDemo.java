package com.llmofang.hinge.agent.compile;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.llmofang.hinge.agent.compile.visitor.WrapMethodClassVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
//import com.llmofang.hinge.testasm.ASMHelloWorld;

public class ClassModifierDemo {

	public static class ModifierMethodWriter extends MethodVisitor {

		private String methodName;

		public ModifierMethodWriter(int api, MethodVisitor mv, String methodName) {
			super(api, mv);
			this.methodName = methodName;
		}

		// This is the point we insert the code. Note that the instructions are
		// added right after
		// the visitCode method of the super class. This ordering is very
		// important.
		@Override
		public void visitCode() {
			super.visitCode();
			super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			super.visitLdcInsn("method: " + methodName);
			super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(Ljava/lang/String;)V");
		}

		@Override
		public void visitInsn(int arg0) {
			// TODO Auto-generated method stub
			super.visitInsn(arg0);
		}

	}

	// Our class modifier class visitor. It delegate all calls to the super
	// class
	// Only makes sure that it returns our MethodVisitor for every method
	public static class ModifierClassWriter extends ClassVisitor {
		private int api;

		public ModifierClassWriter(int api, ClassWriter cv) {
			super(api, cv);
			this.api = api;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
				String signature, String[] exceptions) {

			MethodVisitor mv = super.visitMethod(access, name, desc, signature,
					exceptions);
			ModifierMethodWriter mvw = new ModifierMethodWriter(api, mv, name);
			return mvw;
		}

	}

	public static void main(String[] args) throws IOException {
        //Wrap the ClassWriter with our custom ClassVisitor
        //ModifierClassWriter mcw=new ModifierClassWriter(Opcodes.ASM4, cw);
        String logFileName = "/tmp/test.log";
        Log log = new FileLogImpl(logFileName, true);
        log.debug("Bootstrapping New Relic Android class rewriter");

        ClassRemapperConfig config = null;
		try {
			config = new ClassRemapperConfig(log);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        InstrumentationContext context = new InstrumentationContext(config, log);



        InputStream in=ClassModifierDemo.class.getResourceAsStream("/com/llmofang/hinge/agent/compile/ClassModificationDemo.class");
        ClassReader classReader=new ClassReader(in);
        ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        WrapMethodClassVisitor wmcv = new WrapMethodClassVisitor(cw, context, log);
        classReader.accept(wmcv,8);

        //Write the output to a class file
        File outputDir=new File("out/com/llmofang/hinge/testasm");
        outputDir.mkdirs();
        DataOutputStream dout=new DataOutputStream(new FileOutputStream(new File(outputDir,"MY_ClassModificationDemo.class")));
        dout.write(cw.toByteArray());
    }
}