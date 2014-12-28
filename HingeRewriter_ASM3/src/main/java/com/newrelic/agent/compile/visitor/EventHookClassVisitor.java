package com.newrelic.agent.compile.visitor;

import com.newrelic.agent.compile.InstrumentationContext;
import com.newrelic.agent.compile.Log;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.util.*;


import java.util.Map.Entry;


public abstract class EventHookClassVisitor extends ClassAdapter {
    protected final Set<String> baseClasses;
    private final Map methodVisitors = new HashMap();
    protected String superName;
    protected boolean instrument = false;
    protected final InstrumentationContext context;
    protected final Log log;

    public EventHookClassVisitor(ClassVisitor cv, InstrumentationContext context, Log log, Set<String> baseClasses, Map<Method, Method> methodMappings) {
        super(cv);
        this.context = context;
        this.log = log;
        this.baseClasses = Collections.unmodifiableSet(baseClasses);
//     this.methodVisitors = new HashMap();
//     for (Map.Entry entry : methodMappings.entrySet())
//       this.methodVisitors.put(entry.getKey(), new MethodVisitorFactory((Method)entry.getValue()));
        java.util.Map.Entry entry;
        for (Iterator i$ = methodMappings.entrySet().iterator(); i$.hasNext(); methodVisitors.put(entry.getKey(), new MethodVisitorFactory((Method) entry.getValue())))
            entry = (java.util.Map.Entry) i$.next();
    }


    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.superName = superName;

        this.instrument = this.baseClasses.contains(superName);

        if (this.instrument) {
            this.context.markModified();
            this.log.info("Rewriting " + name);
        }
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (!this.instrument) {
            return mv;
        }

        Method method = new Method(name, desc);
        MethodVisitorFactory v = (MethodVisitorFactory) this.methodVisitors.get(method);
        if (v != null) {
            this.methodVisitors.remove(method);
            return v.createMethodVisitor(access, method, mv, false);
        }
        return mv;
    }

    public void visitEnd() {
        if (!this.instrument) {
            return;
        }

//     for (Map.Entry entry : this.methodVisitors.entrySet()) {
//       MethodVisitor mv = super.visitMethod(4, ((Method)entry.getKey()).getName(), ((Method)entry.getKey()).getDescriptor(), null, null);
//
//       mv = ((MethodVisitorFactory)entry.getValue()).createMethodVisitor(4, (Method)entry.getKey(), mv, true);
//       mv.visitCode();
//       mv.visitInsn(177);
//       mv.visitMaxs(0, 0);
//       mv.visitEnd();
//     }
        MethodVisitor mv;
        for (Iterator i$ = methodVisitors.entrySet().iterator(); i$.hasNext(); mv.visitEnd()) {
            java.util.Map.Entry entry = (java.util.Map.Entry) i$.next();
            mv = super.visitMethod(4, ((Method) entry.getKey()).getName(), ((Method) entry.getKey()).getDescriptor(), null, null);
            mv = ((MethodVisitorFactory) entry.getValue()).createMethodVisitor(4, (Method) entry.getKey(), mv, true);
            mv.visitCode();
            mv.visitInsn(177);
            mv.visitMaxs(0, 0);
        }
        super.visitEnd();
    }

    protected abstract void injectCodeIntoMethod(GeneratorAdapter paramGeneratorAdapter, Method paramMethod1, Method paramMethod2);

    protected class MethodVisitorFactory {
        final Method monitorMethod;

        public MethodVisitorFactory(Method monitorMethod) {
            this.monitorMethod = monitorMethod;
        }

        public MethodVisitor createMethodVisitor(int access, final Method method, MethodVisitor mv, final boolean callSuper) {
            return new GeneratorAdapter(access, method, mv) {
                public void visitCode() {
                    super.visitCode();

                    if (callSuper) {
                        loadThis();
                        for (int i = 0; i < method.getArgumentTypes().length; i++) {
                            loadArg(i);
                        }
                        visitMethodInsn(183, EventHookClassVisitor.this.superName, method.getName(), method.getDescriptor());
                    }

                    EventHookClassVisitor.this.injectCodeIntoMethod(this, method, EventHookClassVisitor.MethodVisitorFactory.this.monitorMethod);
                }
            };
        }
    }
}

/* Location:           /home/cw/class-rewriter/class-rewriter-4.120.0.jar
 * Qualified Name:     com.newrelic.agent.compile.visitor.EventHookClassVisitor
 * JD-Core Version:    0.6.2
 */