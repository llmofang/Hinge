 package com.llmofang.hinge.agent.compile.visitor;
 


//import com.llmofang.hinge.agent.Obfuscation.Proguard;
//import com.llmofang.hinge.agent.compile.HaltBuildException;
import com.llmofang.hinge.agent.compile.InstrumentationContext;
import com.llmofang.hinge.agent.compile.Log;
import com.llmofang.hinge.agent.compile.RewriterAgent;
//import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.UUID;

 public class NewRelicClassVisitor extends ClassVisitor
 {
   private static String buildId;
   private final InstrumentationContext context;
   private final Log log;
 
   public NewRelicClassVisitor(ClassVisitor cv, InstrumentationContext context, Log log)
   {
     super(4,cv);
     this.context = context;
     this.log = log;
   }
 
   public static String getBuildId() {
     if (buildId == null) {
       buildId = UUID.randomUUID().toString();
     }
 
     return buildId;
   }
 
   public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
   {
     if ((this.context.getClassName().equals("com/newrelic/agent/android/NewRelic")) && (name.equals("isInstrumented"))) {
       return new NewRelicMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
     }
 
     if ((this.context.getClassName().equals("com/newrelic/agent/android/harvest/crash/Crash")) && (name.equals("getBuildId"))) {
       return new BuildIdMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
     }
 
     if ((this.context.getClassName().equals("com/newrelic/agent/android/AndroidAgentImpl")) && (name.equals("pokeCanary"))) {
       return new CanaryMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
     }
 
     return super.visitMethod(access, name, desc, signature, exceptions);
   }
 
   public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
   {
     if ((this.context.getClassName().equals("com/newrelic/agent/android/Agent")) && (name.equals("VERSION")) && 
       (!value.equals(RewriterAgent.getVersion()))) {
       //TODO
       //throw new HaltBuildException("New Relic Error: Your agent and class rewriter versions do not match: agent = " + value + " class rewriter = " + RewriterAgent.getVersion() + ".  You probably need to update one of these components.  If you're using gradle and just updated, run gradle -stop to restart the daemon.");
     }
 
     return super.visitField(access, name, desc, signature, value);
   }
 
   private final class CanaryMethodVisitor extends GeneratorAdapter
   {
     private boolean foundCanaryAlive = false;
 
     public CanaryMethodVisitor(MethodVisitor mv, int access, String name, String desc) {
       //TODO
       //super(access, name, desc);
       super(mv, access, name, desc);
     }
 
     public void visitMethodInsn(int opcode, String owner, String name, String desc)
     {
       if (name.equals("canaryMethod"))
         this.foundCanaryAlive = true;
     }
 
     public void visitEnd()
     {
       if (this.foundCanaryAlive) {
         NewRelicClassVisitor.this.log.info("Found canary alive");
       } else {
         //NewRelicClassVisitor.this.log.info("Evidence of Proguard detected, sending mapping.txt");

         //Proguard proguard = new Proguard(NewRelicClassVisitor.this.log);
         //proguard.findAndSendMapFile();
       }
     }
   }
 
   private final class NewRelicMethodVisitor extends GeneratorAdapter
   {
     public NewRelicMethodVisitor(MethodVisitor mv, int access, String name, String desc)
     {
       //TODO
       // super(access, name, desc);
       super(mv, access, name, desc);
     }
 
     public void visitCode()
     {
       super.visitInsn(4);
       super.visitInsn(172);
 
       NewRelicClassVisitor.this.log.info("Marking NewRelic agent as instrumented");
       NewRelicClassVisitor.this.context.markModified();
     }
   }
 
   private final class BuildIdMethodVisitor extends GeneratorAdapter
   {
     public BuildIdMethodVisitor(MethodVisitor mv, int access, String name, String desc)
     {
       //TODO
       //super(access, name, desc);
       super(mv, access,name, desc);
     }
 
     public void visitCode()
     {
       super.visitLdcInsn(NewRelicClassVisitor.getBuildId());
       super.visitInsn(176);
 
       NewRelicClassVisitor.this.log.info("Setting build identifier to " + NewRelicClassVisitor.getBuildId());
       NewRelicClassVisitor.this.context.markModified();
     }
   }
 }

/* Location:           /home/think/Downloads/newrelic-android-4.120.0/lib/class.rewriter.jar
 * Qualified Name:     com.newrelic.agent.compile.visitor.NewRelicClassVisitor
 * JD-Core Version:    0.6.2
 */