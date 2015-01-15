package com.newrelic.agent.compile;

import com.llmofang.objectweb.asm.AnnotationVisitor;
import  com.llmofang.objectweb.asm.MethodAdapter;
import  com.llmofang.objectweb.asm.MethodVisitor;
import  com.llmofang.objectweb.asm.Type;

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

