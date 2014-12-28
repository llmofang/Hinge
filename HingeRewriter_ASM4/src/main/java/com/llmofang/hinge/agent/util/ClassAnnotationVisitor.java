 package com.llmofang.hinge.agent.util;
 
 import org.objectweb.asm.AnnotationVisitor;
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.commons.EmptyVisitor;
 import java.util.ArrayList;
 import java.util.Collection;
 
 class ClassAnnotationVisitor extends EmptyVisitor
 {
   private final Collection<ClassAnnotation> annotations = new ArrayList();
   private String className;
   private final String annotationDescription;
 
   public ClassAnnotationVisitor(String annotationDescription)
   {
     this.annotationDescription = annotationDescription;
   }
 
   public Collection<ClassAnnotation> getAnnotations() {
     return this.annotations;
   }
 
   public static Collection<ClassAnnotation> getAnnotations(ClassReader cr, String annotationDescription) {
     ClassAnnotationVisitor visitor = new ClassAnnotationVisitor(annotationDescription);
     cr.accept(visitor, 0);
     return visitor.getAnnotations();
   }
 
   public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
   {
     this.className = name;
   }
 
   public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
     if (this.annotationDescription.equals(desc)) {
       ClassAnnotationImpl annotationVisitor = new ClassAnnotationImpl(this.className, desc);
       this.annotations.add(annotationVisitor);
       return annotationVisitor;
     }
     return null;
   }
 }

/* Location:           /home/cw/git/HttpClient/resource/ant/newrelic-android-4.120.0/lib/class.rewriter.jar
 * Qualified Name:     com.newrelic.agent.util.ClassAnnotationVisitor
 * JD-Core Version:    0.6.2
 */