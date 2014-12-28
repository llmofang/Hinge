 package com.llmofang.hinge.agent.compile.visitor;
 


import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

import java.util.ArrayList;



 public class TraceMethodVisitor extends AdviceAdapter
 {
   public static final String TRACE_MACHINE_INTERNAL_CLASSNAME = "com/newrelic/agent/android/tracing/TraceMachine";
   protected final InstrumentationContext context;
   protected final Log log;
   private String name;
   protected Boolean unloadContext = Boolean.valueOf(false);
   protected Boolean startTracing = Boolean.valueOf(false);
   private int access;
 
   public TraceMethodVisitor(MethodVisitor mv, int access, String name, String desc, InstrumentationContext context)
   {
     //TODO
     //super(mv, access, name, desc);
     super(4, mv, access, name, desc);
     this.access = access;
     this.context = context;
     this.log = context.getLog();
     this.name = name;
   }
 
   public void setUnloadContext() {
     this.unloadContext = Boolean.valueOf(true);
   }
 
   public void setStartTracing() {
     this.startTracing = Boolean.valueOf(true);
   }
 
   protected void onMethodEnter()
   {
     Type targetType = Type.getObjectType("com/newrelic/agent/android/tracing/TraceMachine");
     if (this.startTracing.booleanValue())
     {
       super.visitLdcInsn(this.context.getSimpleClassName());
 
       super.invokeStatic(targetType, new Method("startTracing", "(Ljava/lang/String;)V"));
     }
 
     if ((this.access & 0x8) != 0) {
       this.log.info("Tracing static method " + this.context.getClassName() + "#" + this.name);
 
       super.visitInsn(1);
       super.visitLdcInsn(this.context.getSimpleClassName() + "#" + this.name);
       emitAnnotationParamsList(this.name);
       super.invokeStatic(targetType, new Method("enterMethod", "(Lcom/newrelic/agent/android/tracing/Trace;Ljava/lang/String;Ljava/util/ArrayList;)V"));
     } else {
       this.log.info("Tracing method " + this.context.getClassName() + "#" + this.name);
 
       Label tryStart = new Label();
       Label tryEnd = new Label();
       Label tryHandler = new Label();
 
       super.visitLabel(tryStart);
       super.loadThis();
       super.getField(Type.getObjectType(this.context.getClassName()), "_nr_trace", Type.getType("Lcom/newrelic/agent/android/tracing/Trace;"));
 
       super.visitLdcInsn(this.context.getSimpleClassName() + "#" + this.name);
 
       emitAnnotationParamsList(this.name);
 
       super.invokeStatic(targetType, new Method("enterMethod", "(Lcom/newrelic/agent/android/tracing/Trace;Ljava/lang/String;Ljava/util/ArrayList;)V"));
 
       super.goTo(tryEnd);
       super.visitLabel(tryHandler);
 
       super.pop();
       super.visitInsn(1);
       super.visitLdcInsn(this.context.getSimpleClassName() + "#" + this.name);
 
       emitAnnotationParamsList(this.name);
 
       super.invokeStatic(targetType, new Method("enterMethod", "(Lcom/newrelic/agent/android/tracing/Trace;Ljava/lang/String;Ljava/util/ArrayList;)V"));
 
       super.visitLabel(tryEnd);
       super.visitTryCatchBlock(tryStart, tryEnd, tryHandler, "java/lang/NoSuchFieldError");
     }
   }
 
   private void emitAnnotationParamsList(String name) {
     ArrayList annotationParameters = this.context.getTracedMethodParameters(name);
     if ((annotationParameters == null) || (annotationParameters.size() == 0)) {
       super.visitInsn(1);
       return;
     }
 
     Method constructor = Method.getMethod("void <init> ()");
     Method add = Method.getMethod("boolean add(java.lang.Object)");
     Type arrayListType = Type.getObjectType("java/util/ArrayList");
 
     super.newInstance(arrayListType);
     super.dup();
     super.invokeConstructor(arrayListType, constructor);
     //TODO
     //for (String parameterEntry : annotationParameters)
     for (Object parameterEntry : annotationParameters) {
       super.dup();
       super.visitLdcInsn(parameterEntry);
       super.invokeVirtual(arrayListType, add);
       super.pop();
     }
   }
 
   protected void onMethodExit(int opcode)
   {
     Type targetType = Type.getObjectType("com/newrelic/agent/android/tracing/TraceMachine");
     super.invokeStatic(targetType, new Method("exitMethod", "()V"));
 
     if (this.unloadContext.booleanValue()) {
       super.loadThis();
       targetType = Type.getObjectType("com/newrelic/agent/android/tracing/TraceMachine");
       super.invokeStatic(targetType, new Method("unloadTraceContext", "(Ljava/lang/Object;)V"));
     }
   }
 }

/* Location:           /home/think/Downloads/newrelic-android-4.120.0/lib/class.rewriter.jar
 * Qualified Name:     com.newrelic.agent.compile.visitor.TraceMethodVisitor
 * JD-Core Version:    0.6.2
 */