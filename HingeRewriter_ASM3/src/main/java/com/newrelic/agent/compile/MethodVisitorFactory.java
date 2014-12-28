package com.newrelic.agent.compile;

import org.objectweb.asm.MethodVisitor;

abstract interface MethodVisitorFactory {
    public abstract MethodVisitor create(MethodVisitor paramMethodVisitor, int paramInt, String paramString1, String paramString2);
}

/* Location:           /home/cw/class-rewriter/class-rewriter-4.120.0.jar
 * Qualified Name:     com.newrelic.agent.compile.MethodVisitorFactory
 * JD-Core Version:    0.6.2
 */