/*    */ package com.github.chenhq.agent.compile.visitor;
/*    */
/*    */ import com.github.chenhq.agent.compile.InstrumentationContext;
/*    */ import com.github.chenhq.agent.compile.Log;

import org.objectweb.asm.ClassAdapter;
/*    */// import org.objectweb.asm.ClassAdapter;
/*    */ import org.objectweb.asm.ClassVisitor;

/*    */ import java.text.MessageFormat;
/*    */
/*    */ public class AnnotatingClassVisitor extends ClassAdapter
/*    */ {
/*    */   private final InstrumentationContext context;
/*    */   private final Log log;
/*    */
/*    */   public AnnotatingClassVisitor(ClassVisitor cv, InstrumentationContext context, Log log)
/*    */   {
/* 16 */     super(cv);
/* 17 */     this.context = context;
/* 18 */     this.log = log;
/*    */   }
/*    */
/*    */   public void visitEnd()
/*    */   {
/* 26 */     if (this.context.isClassModified()) {
/* 27 */       this.context.addUniqueTag("Lcom/newrelic/agent/android/instrumentation/Instrumented;");
/* 28 */       super.visitAnnotation("Lcom/newrelic/agent/android/instrumentation/Instrumented;", false);
/* 29 */       this.log.info(MessageFormat.format("[{0}] tagging as instrumented", new Object[] { this.context.getFriendlyClassName() }));
/*    */     }
/*    */
/* 32 */     super.visitEnd();
/*    */   }
/*    */ }

/* Location:           /home/think/Downloads/newrelic-android-4.120.0/lib/class.rewriter.jar
 * Qualified Name:     com.newrelic.agent.compile.visitor.AnnotatingClassVisitor
 * JD-Core Version:    0.6.2
 */