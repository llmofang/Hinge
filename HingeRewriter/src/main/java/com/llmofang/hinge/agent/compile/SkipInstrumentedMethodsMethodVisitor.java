 package com.llmofang.hinge.agent.compile;
 


import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;






 class SkipInstrumentedMethodsMethodVisitor extends MethodVisitor
 {
   public SkipInstrumentedMethodsMethodVisitor(MethodVisitor mv)
   {
     //TODO
     //super(mv);
     super(4,mv);
   }
 
   public AnnotationVisitor visitAnnotation(String desc, boolean visible)
   {
     if (Type.getDescriptor(InstrumentedMethod.class).equals(desc)) {
       throw new SkipException();
     }
     return super.visitAnnotation(desc, visible);
   }
 }
