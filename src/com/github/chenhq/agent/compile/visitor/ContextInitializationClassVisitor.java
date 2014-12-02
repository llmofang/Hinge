/*    */ package com.github.chenhq.agent.compile.visitor;
/*    */
/*    */ import com.github.chenhq.agent.compile.InstrumentationContext;
/*    */ import org.objectweb.asm.ClassAdapter;
/*    */ import org.objectweb.asm.ClassVisitor;
/*    */
/*    */ public class ContextInitializationClassVisitor extends ClassAdapter
/*    */ {
/*    */   private final InstrumentationContext context;
/*    */
/*    */   public ContextInitializationClassVisitor(ClassVisitor cv, InstrumentationContext context)
/*    */   {
/* 12 */     super(cv);
/* 13 */     this.context = context;
/*    */   }
/*    */
/*    */   public void visit(int version, int access, String name, String sig, String superName, String[] interfaces)
/*    */   {
/* 18 */     this.context.setClassName(name);
/* 19 */     this.context.setSuperClassName(superName);
/*    */
/* 21 */     super.visit(version, access, name, sig, superName, interfaces);
/*    */   }
/*    */ }

/* Location:           /home/think/Downloads/newrelic-android-4.120.0/lib/class.rewriter.jar
 * Qualified Name:     com.newrelic.agent.compile.visitor.ContextInitializationClassVisitor
 * JD-Core Version:    0.6.2
 */