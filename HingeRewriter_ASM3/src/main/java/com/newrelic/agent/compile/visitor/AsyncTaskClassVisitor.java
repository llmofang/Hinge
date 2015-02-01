package com.newrelic.agent.compile.visitor;

import com.newrelic.agent.compile.InstrumentationContext;
import com.newrelic.agent.compile.Log;
import com.google.common.collect.ImmutableMap;
import  com.llmofang.objectweb.asm.ClassAdapter;
import  com.llmofang.objectweb.asm.ClassVisitor;
import  com.llmofang.objectweb.asm.MethodVisitor;
import  com.llmofang.objectweb.asm.Type;

public class AsyncTaskClassVisitor extends ClassAdapter {
    public static final String TARGET_CLASS = "android/os/AsyncTask";
    private final InstrumentationContext context;
    private final Log log;
    private boolean instrument = false;

    public static final ImmutableMap<String, String> traceMethodMap = ImmutableMap.of("doInBackground", "([Ljava/lang/Object;)Ljava/lang/Object;");

    public static final ImmutableMap<String, String> endTraceMethodMap = ImmutableMap.of("onPostExecute", "(Ljava/lang/Object;)V");

    public AsyncTaskClassVisitor(ClassVisitor cv, InstrumentationContext context, Log log) {
        super(cv);
        this.context = context;
        this.log = log;
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if ((superName != null) && (superName.equals("android/os/AsyncTask"))) {
            interfaces = TraceClassDecorator.addInterface(interfaces);
            super.visit(version, access, name, signature, superName, interfaces);

            this.instrument = true;
            this.log.info("Rewriting " + this.context.getClassName());
            this.context.markModified();
        } else {
            super.visit(version, access, name, signature, superName, interfaces);
        }
    }

    public void visitEnd() {
        if (this.instrument) {
            TraceClassDecorator decorator = new TraceClassDecorator(this);

            decorator.addTraceField();
            decorator.addTraceInterface(Type.getObjectType(this.context.getClassName()));

            this.log.info("Added Trace object and interface to " + this.context.getClassName());
        }

        super.visitEnd();
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);

        if (this.instrument) {
            if ((traceMethodMap.containsKey(name)) && (((String) traceMethodMap.get(name)).equals(desc))) {
                TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(methodVisitor, access, name, desc, this.context);
                traceMethodVisitor.setUnloadContext();
                return traceMethodVisitor;
            }

            if ((endTraceMethodMap.containsKey(name)) && (((String) endTraceMethodMap.get(name)).equals(desc))) {
                TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(methodVisitor, access, name, desc, this.context);
                return traceMethodVisitor;
            }
        }

        return methodVisitor;
    }
}
