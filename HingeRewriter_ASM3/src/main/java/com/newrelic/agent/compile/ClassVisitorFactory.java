package com.newrelic.agent.compile;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;

abstract class ClassVisitorFactory {
    private final boolean retransformOkay;

    public ClassVisitorFactory(boolean retransformOkay) {
        this.retransformOkay = retransformOkay;
    }

    public boolean isRetransformOkay() {
        return this.retransformOkay;
    }

    abstract ClassAdapter create(ClassVisitor paramClassVisitor);
}

/* Location:           /home/cw/class-rewriter/class-rewriter-4.120.0.jar
 * Qualified Name:     com.newrelic.agent.compile.ClassVisitorFactory
 * JD-Core Version:    0.6.2
 */