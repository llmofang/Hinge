package com.newrelic.agent.compile.visitor;

import com.newrelic.agent.compile.InstrumentationContext;
import  com.llmofang.objectweb.asm.ClassAdapter;
import  com.llmofang.objectweb.asm.ClassVisitor;

public class ContextInitializationClassVisitor extends ClassAdapter {
    private final InstrumentationContext context;

    public ContextInitializationClassVisitor(ClassVisitor cv, InstrumentationContext context) {
        super(cv);
        this.context = context;
    }

    public void visit(int version, int access, String name, String sig, String superName, String[] interfaces) {
        this.context.setClassName(name);
        this.context.setSuperClassName(superName);

        super.visit(version, access, name, sig, superName, interfaces);
    }
}

/* Location:           /home/cw/class-rewriter/class-rewriter-4.120.0.jar
 * Qualified Name:     com.newrelic.agent.compile.visitor.ContextInitializationClassVisitor
 * JD-Core Version:    0.6.2
 */