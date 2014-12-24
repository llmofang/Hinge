 package com.llmofang.hinge.agent.compile.visitor;

 import com.github.chenhq.agent.compile.InstrumentationContext;
 import com.github.chenhq.agent.compile.Log;

// import org.objectweb.asm.ClassAdapter;
// import org.objectweb.asm.ClassAdapter;
 import org.objectweb.asm.ClassVisitor;
 import org.objectweb.asm.Opcodes;

 import java.text.MessageFormat;

 public class AnnotatingClassVisitor extends ClassVisitor
 {
   private final InstrumentationContext context;
   private final Log log;

   public AnnotatingClassVisitor(ClassVisitor cv, InstrumentationContext context, Log log)
   {
     super(Opcodes.ASM4,cv);
     this.context = context;
     this.log = log;
   }

   public void visitEnd()
   {
     if (this.context.isClassModified()) {
       this.context.addUniqueTag("Lcom/newrelic/agent/android/instrumentation/Instrumented;");
       super.visitAnnotation("Lcom/newrelic/agent/android/instrumentation/Instrumented;", false);
       this.log.info(MessageFormat.format("[{0}] tagging as instrumented", new Object[] { this.context.getFriendlyClassName() }));
     }

     super.visitEnd();
   }
 }





