/*     */ package com.github.chenhq.agent.compile.visitor;
/*     */
/*     */ import com.github.chenhq.agent.compile.InstrumentationContext;
/*     */ import com.github.chenhq.agent.compile.Log;
/*     */ import org.objectweb.asm.AnnotationVisitor;
/*     */ import org.objectweb.asm.Attribute;
/*     */ import org.objectweb.asm.ClassVisitor;
/*     */ import org.objectweb.asm.FieldVisitor;
/*     */ import org.objectweb.asm.Label;
/*     */ import org.objectweb.asm.MethodVisitor;
/*     */ import java.text.MessageFormat;
/*     */
/*     */ public class PrefilterClassVisitor
/*     */   implements ClassVisitor
/*     */ {
/*     */   private static final String TRACE_ANNOTATION_CLASSPATH = "Lcom/newrelic/agent/android/instrumentation/Trace;";
/*     */   private static final String SKIP_TRACE_ANNOTATION_CLASSPATH = "Lcom/newrelic/agent/android/instrumentation/SkipTrace;";
/*     */   private final InstrumentationContext context;
/*     */   private final Log log;
/*     */
/*     */   public PrefilterClassVisitor(InstrumentationContext context, Log log)
/*     */   {
/*  21 */     this.context = context;
/*  22 */     this.log = log;
/*     */   }
/*     */
/*     */   public void visit(int version, int access, String name, String sig, String superName, String[] interfaces)
/*     */   {
/*  27 */     this.context.setClassName(name);
/*  28 */     this.context.setSuperClassName(superName);
/*     */   }
/*     */
/*     */   public AnnotationVisitor visitAnnotation(String desc, boolean visible)
/*     */   {
/*  33 */     if (Annotations.isNewRelicAnnotation(desc)) {
/*  34 */       this.log.info(MessageFormat.format("[{0}] class has New Relic tag: {1}", new Object[] { this.context.getClassName(), desc }));
/*  35 */       this.context.addTag(desc);
/*     */     }
/*  37 */     return null;
/*     */   }
/*     */
/*     */   public void visitAttribute(Attribute arg0)
/*     */   {
/*     */   }
/*     */
/*     */   public void visitEnd()
/*     */   {
/*     */   }
/*     */
/*     */   public FieldVisitor visitField(int access, String name, String desc, String sig, Object value)
/*     */   {
/*  50 */     return null;
/*     */   }
/*     */
/*     */   public void visitInnerClass(String arg0, String arg1, String arg2, int arg3)
/*     */   {
/*     */   }
/*     */
/*     */   public MethodVisitor visitMethod(int access, final String name, final String desc, String signature, String[] exceptions)
/*     */   {
/*  60 */     MethodVisitor methodVisitor = new MethodVisitor()
/*     */     {
/*     */       public AnnotationVisitor visitAnnotationDefault() {
/*  63 */         return null;
/*     */       }
/*     */
/*     */       public AnnotationVisitor visitAnnotation(String annotationDesc, boolean visible)
/*     */       {
/*  68 */         if (annotationDesc.equals("Lcom/newrelic/agent/android/instrumentation/Trace;")) {
/*  69 */           PrefilterClassVisitor.this.context.addTracedMethod(name, desc);
/*  70 */           return new TraceAnnotationVisitor(name, PrefilterClassVisitor.this.context);
/*     */         }
/*     */
/*  73 */         if (annotationDesc.equals("Lcom/newrelic/agent/android/instrumentation/SkipTrace;")) {
/*  74 */           PrefilterClassVisitor.this.context.addSkippedMethod(name, desc);
/*  75 */           return null;
/*     */         }
/*  77 */         return null;
/*     */       }
/*     */
/*     */       public AnnotationVisitor visitParameterAnnotation(int i, String s, boolean b)
/*     */       {
/*  82 */         return null;
/*     */       }
/*     */
/*     */       public void visitAttribute(Attribute attribute)
/*     */       {
/*     */       }
/*     */
/*     */       public void visitCode()
/*     */       {
/*     */       }
/*     */
/*     */       public void visitFrame(int i, int i2, Object[] objects, int i3, Object[] objects2)
/*     */       {
/*     */       }
/*     */
/*     */       public void visitInsn(int i)
/*     */       {
/*     */       }
/*     */
/*     */       public void visitIntInsn(int i, int i2)
/*     */       {
/*     */       }
/*     */
/*     */       public void visitVarInsn(int i, int i2)
/*     */       {
/*     */       }
/*     */
/*     */       public void visitTypeInsn(int i, String s)
/*     */       {
/*     */       }
/*     */
/*     */       public void visitFieldInsn(int i, String s, String s2, String s3)
/*     */       {
/*     */       }
/*     */
/*     */       public void visitMethodInsn(int i, String s, String s2, String s3)
/*     */       {
/*     */       }
/*     */
/*     */       public void visitJumpInsn(int i, Label label)
/*     */       {
/*     */       }
/*     */
/*     */       public void visitLabel(Label label)
/*     */       {
/*     */       }
/*     */
/*     */       public void visitLdcInsn(Object o)
/*     */       {
/*     */       }
/*     */
/*     */       public void visitIincInsn(int i, int i2)
/*     */       {
/*     */       }
/*     */
/*     */       public void visitTableSwitchInsn(int i, int i2, Label label, Label[] labels)
/*     */       {
/*     */       }
/*     */
/*     */       public void visitLookupSwitchInsn(Label label, int[] ints, Label[] labels)
/*     */       {
/*     */       }
/*     */
/*     */       public void visitMultiANewArrayInsn(String s, int i)
/*     */       {
/*     */       }
/*     */
/*     */       public void visitTryCatchBlock(Label label, Label label2, Label label3, String s)
/*     */       {
/*     */       }
/*     */
/*     */       public void visitLocalVariable(String s, String s2, String s3, Label label, Label label2, int i)
/*     */       {
/*     */       }
/*     */
/*     */       public void visitLineNumber(int i, Label label)
/*     */       {
/*     */       }
/*     */
/*     */       public void visitMaxs(int i, int i2)
/*     */       {
/*     */       }
/*     */
/*     */       public void visitEnd()
/*     */       {
/*     */       }
/*     */     };
/* 169 */     return methodVisitor;
/*     */   }
/*     */
/*     */   public void visitOuterClass(String arg0, String arg1, String arg2)
/*     */   {
/*     */   }
/*     */
/*     */   public void visitSource(String arg0, String arg1)
/*     */   {
/*     */   }
/*     */ }

/* Location:           /home/think/Downloads/newrelic-android-4.120.0/lib/class.rewriter.jar
 * Qualified Name:     com.newrelic.agent.compile.visitor.PrefilterClassVisitor
 * JD-Core Version:    0.6.2
 */