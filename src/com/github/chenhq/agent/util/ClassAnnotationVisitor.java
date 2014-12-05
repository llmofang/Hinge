/*    */ package com.github.chenhq.agent.util;
/*    */ 
/*    */ import org.objectweb.asm.AnnotationVisitor;
/*    */ import org.objectweb.asm.ClassReader;
/*    */ import org.objectweb.asm.commons.EmptyVisitor;
/*    */ import java.util.ArrayList;
/*    */ import java.util.Collection;
/*    */ 
/*    */ class ClassAnnotationVisitor extends EmptyVisitor
/*    */ {
/* 16 */   private final Collection<ClassAnnotation> annotations = new ArrayList();
/*    */   private String className;
/*    */   private final String annotationDescription;
/*    */ 
/*    */   public ClassAnnotationVisitor(String annotationDescription)
/*    */   {
/* 21 */     this.annotationDescription = annotationDescription;
/*    */   }
/*    */ 
/*    */   public Collection<ClassAnnotation> getAnnotations() {
/* 25 */     return this.annotations;
/*    */   }
/*    */ 
/*    */   public static Collection<ClassAnnotation> getAnnotations(ClassReader cr, String annotationDescription) {
/* 29 */     ClassAnnotationVisitor visitor = new ClassAnnotationVisitor(annotationDescription);
/* 30 */     cr.accept(visitor, 0);
/* 31 */     return visitor.getAnnotations();
/*    */   }
/*    */ 
/*    */   public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
/*    */   {
/* 36 */     this.className = name;
/*    */   }
/*    */ 
/*    */   public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
/* 40 */     if (this.annotationDescription.equals(desc)) {
/* 41 */       ClassAnnotationImpl annotationVisitor = new ClassAnnotationImpl(this.className, desc);
/* 42 */       this.annotations.add(annotationVisitor);
/* 43 */       return annotationVisitor;
/*    */     }
/* 45 */     return null;
/*    */   }
/*    */ }

/* Location:           /home/cw/git/HttpClient/resource/ant/newrelic-android-4.120.0/lib/class.rewriter.jar
 * Qualified Name:     com.newrelic.agent.util.ClassAnnotationVisitor
 * JD-Core Version:    0.6.2
 */