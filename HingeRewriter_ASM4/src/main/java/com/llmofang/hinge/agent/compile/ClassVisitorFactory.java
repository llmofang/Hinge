package com.llmofang.hinge.agent.compile;

import org.objectweb.asm.ClassVisitor;


abstract class ClassVisitorFactory {
    private final boolean retransformOkay;

    public ClassVisitorFactory(boolean retransformOkay) {
        this.retransformOkay = retransformOkay;
    }

    public boolean isRetransformOkay() {
        return this.retransformOkay;
    }

    abstract ClassVisitor create(ClassVisitor paramClassVisitor);
}
