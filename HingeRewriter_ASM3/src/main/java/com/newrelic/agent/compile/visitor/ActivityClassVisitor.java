package com.newrelic.agent.compile.visitor;

import com.google.common.collect.ImmutableSet;
import com.newrelic.agent.compile.InstrumentationContext;
import com.newrelic.agent.compile.Log;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import  com.llmofang.objectweb.asm.ClassVisitor;
import  com.llmofang.objectweb.asm.MethodVisitor;
import  com.llmofang.objectweb.asm.Type;
import  com.llmofang.objectweb.asm.commons.GeneratorAdapter;
import  com.llmofang.objectweb.asm.commons.Method;

import java.util.Set;

public class ActivityClassVisitor extends EventHookClassVisitor {
    static final ImmutableSet<String> ACTIVITY_CLASS_NAMES = ImmutableSet.of("android/app/Activity", "android/app/Fragment", "android/accounts/AccountAuthenticatorActivity", "android/app/ActivityGroup", "android/app/TabActivity", "android/app/AliasActivity", new String[]{"android/app/ExpandableListActivity", "android/app/ListActivity", "android/app/LauncherActivity", "android/preference/PreferenceActivity", "android/app/NativeActivity", "android/support/v4/app/FragmentActivity", "android/support/v4/app/Fragment", "android/support/v4/app/DialogFragment", "android/support/v4/app/ListFragment", "android/support/v7/app/ActionBarActivity"});

    static final Type applicationStateMonitorType = Type.getObjectType("com/newrelic/agent/android/background/ApplicationStateMonitor");

    public static final ImmutableMap<String, String> traceMethodMap = ImmutableMap.of("onCreate", "(Landroid/os/Bundle;)V", "onCreateView", "(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;");

    public static final ImmutableSet<String> startTracingOn = ImmutableSet.of("onCreate");

    public ActivityClassVisitor(ClassVisitor cv, InstrumentationContext context, Log log) {
        super(cv, context, log, ACTIVITY_CLASS_NAMES, ImmutableMap.of(new Method("onStart", "()V"), new Method("activityStarted", "()V"), new Method("onStop", "()V"), new Method("activityStopped", "()V")));
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (this.baseClasses.contains(superName)) {
            interfaces = TraceClassDecorator.addInterface(interfaces);
        }

        super.visit(version, access, name, signature, superName, interfaces);
    }

    protected void injectCodeIntoMethod(GeneratorAdapter generatorAdapter, Method method, Method monitorMethod) {
        generatorAdapter.invokeStatic(applicationStateMonitorType, new Method("getInstance", applicationStateMonitorType, new Type[0]));

        generatorAdapter.invokeVirtual(applicationStateMonitorType, monitorMethod);
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (ACTIVITY_CLASS_NAMES.contains(this.context.getClassName())) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        if ((this.instrument) && (traceMethodMap.containsKey(name)) && (((String) traceMethodMap.get(name)).equals(desc))) {
            MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
            TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(methodVisitor, access, name, desc, this.context);

            if (startTracingOn.contains(name)) {
                traceMethodVisitor.setStartTracing();
            }

            return traceMethodVisitor;
        }

        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}

/* Location:           /home/cw/class-rewriter/class-rewriter-4.120.0.jar
 * Qualified Name:     com.newrelic.agent.compile.visitor.ActivityClassVisitor
 * JD-Core Version:    0.6.2
 */