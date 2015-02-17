package com.llmofang.android.agent;

import java.lang.annotation.Target;

@Target({java.lang.annotation.ElementType.METHOD})
public @interface WrapReturn
{
  public abstract String className();

  public abstract String methodName();

  public abstract String methodDesc();
}
