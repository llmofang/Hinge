package com.newrelic.agent.compile;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

class SkipInstrumentedMethodsMethodVisitor extends MethodAdapter {
    public SkipInstrumentedMethodsMethodVisitor(MethodVisitor mv) {
        super(mv);
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (Type.getDescriptor(InstrumentedMethod.class).equals(desc)) {
            throw new SkipException();
        }
        return super.visitAnnotation(desc, visible);
    }
}

/* Location:           /home/cw/class-rewriter/class-rewriter-4.120.0.jar
 * Qualified Name:     com.newrelic.agent.compile.SkipInstrumentedMethodsMethodVisitor
 * JD-Core Version:    0.6.2
 */