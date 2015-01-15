package com.newrelic.agent.compile.visitor;

import com.newrelic.agent.compile.InstrumentationContext;
import com.newrelic.agent.compile.Log;
import  com.llmofang.objectweb.asm.ClassAdapter;
import  com.llmofang.objectweb.asm.ClassVisitor;
import  com.llmofang.objectweb.asm.MethodVisitor;

public class TraceAnnotationClassVisitor extends ClassAdapter {
    private final InstrumentationContext context;

    public TraceAnnotationClassVisitor(ClassVisitor cv, InstrumentationContext context, Log log) {
        super(cv);
        this.context = context;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);

        if ((this.context.isTracedMethod(name, desc) & !this.context.isSkippedMethod(name, desc))) {
            this.context.markModified();
            return new TraceMethodVisitor(methodVisitor, access, name, desc, this.context);
        }

        return methodVisitor;
    }
}

/* Location:           /home/cw/class-rewriter/class-rewriter-4.120.0.jar
 * Qualified Name:     com.newrelic.agent.compile.visitor.TraceAnnotationClassVisitor
 * JD-Core Version:    0.6.2
 */