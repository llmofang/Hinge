package com.newrelic.agent.compile;

import  com.llmofang.objectweb.asm.ClassAdapter;
import  com.llmofang.objectweb.asm.ClassVisitor;
import  com.llmofang.objectweb.asm.MethodVisitor;
import  com.llmofang.objectweb.asm.commons.Method;

import java.util.Map;

class ClassAdapterBase extends ClassAdapter {
    final Map<Method, MethodVisitorFactory> methodVisitors;
    private final Log log;

    public ClassAdapterBase(Log log, ClassVisitor cv, Map<Method, MethodVisitorFactory> methodVisitors) {
        super(cv);
        this.methodVisitors = methodVisitors;
        this.log = log;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        MethodVisitorFactory factory = (MethodVisitorFactory) this.methodVisitors.get(new Method(name, desc));
        if (factory != null) {
            return new SkipInstrumentedMethodsMethodVisitor(factory.create(mv, access, name, desc));
        }

        return mv;
    }
}

