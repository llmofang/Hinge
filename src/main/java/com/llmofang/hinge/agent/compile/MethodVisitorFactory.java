package com.llmofang.hinge.agent.compile;

import org.objectweb.asm.MethodVisitor;

abstract interface MethodVisitorFactory
{
  public abstract MethodVisitor create(MethodVisitor paramMethodVisitor, int paramInt, String paramString1, String paramString2);
}
