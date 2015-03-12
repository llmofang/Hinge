package com.newrelic.agent.compile.visitor;

import com.newrelic.agent.compile.InstrumentationContext;
import com.newrelic.agent.compile.Log;
import  com.llmofang.objectweb.asm.ClassAdapter;
import  com.llmofang.objectweb.asm.ClassVisitor;

import java.text.MessageFormat;

public class AnnotatingClassVisitor extends ClassAdapter {
    private final InstrumentationContext context;
    private final Log log;

    public AnnotatingClassVisitor(ClassVisitor cv, InstrumentationContext context, Log log) {
        super(cv);
        this.context = context;
        this.log = log;
    }

    public void visitEnd() {
        if (this.context.isClassModified()) {
            this.context.addUniqueTag("Lcom/llmofang/android/agent/Instrumented;");
            super.visitAnnotation("Lcom/llmofang/android/agent/Instrumented;", false);
            this.log.info(MessageFormat.format("[{0}] tagging as instrumented", new Object[]{this.context.getFriendlyClassName()}));
        }

        super.visitEnd();
    }
}