package com.github.chenhq.testasm;

import java.io.InputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.commons.Method;

public class DemoClassInstructionViewer {

    public static class MethodPrinterVisitor extends ClassVisitor{

        public MethodPrinterVisitor(int api, ClassVisitor cv) {
            super(api, cv);
        }


        public MethodPrinterVisitor(int api) {
            super(api);
        }


        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                String signature, String[] exceptions) {

            System.out.println("\n"+name+desc);

            MethodVisitor oriMv= new MethodVisitor(4) {
            };
            //An instructionAdapter is a special MethodVisitor that
            //lets us process instructions easily
            InstructionAdapter instMv=new InstructionAdapter(oriMv){

                @Override
                public void visitInsn(int opcode) {
                    System.out.println(opcode);
                    super.visitInsn(opcode);
                }

            };
            return instMv;

        }


    }


    public static void main(String[] args) throws Exception{
        //InputStream in = ASMHelloWorld.class.getResourceAsStream("/com/github/chenhq/testasm/ASMHelloWorld.class");
        InputStream in = ASMHelloWorld.class.getResourceAsStream("/com/github/chenhq/testasm/HttpClient1.class");
        ClassReader classReader=new ClassReader(in);
        MethodPrinterVisitor cl = new MethodPrinterVisitor(Opcodes.ASM4);
        classReader.accept(cl, 0);
        //ClassMethod method = new ClassMethod();


//        InputStream in=ASMHelloWorld.class.getResourceAsStream("/java/lang/String.class");
//        ClassReader classReader=new ClassReader(in);
//        classReader.accept(cl, 0);

    }

}