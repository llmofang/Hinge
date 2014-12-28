package com.newrelic.agent.compile.visitor;

import com.newrelic.agent.compile.InstrumentationContext;
import com.newrelic.agent.compile.Log;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;

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
            this.context.addUniqueTag("Lcom/newrelic/agent/android/instrumentation/Instrumented;");
            super.visitAnnotation("Lcom/newrelic/agent/android/instrumentation/Instrumented;", false);
            this.log.info(MessageFormat.format("[{0}] tagging as instrumented", new Object[]{this.context.getFriendlyClassName()}));
        }

        super.visitEnd();
    }
}

/* Location:           /home/cw/class-rewriter/class-rewriter-4.120.0.jar
 * Qualified Name:     com.newrelic.agent.compile.visitor.AnnotatingClassVisitor
 * JD-Core Version:    0.6.2
 */