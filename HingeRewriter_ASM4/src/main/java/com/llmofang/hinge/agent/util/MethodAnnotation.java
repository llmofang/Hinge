package com.llmofang.hinge.agent.util;

import java.util.Map;

public abstract interface MethodAnnotation
{
  public abstract String getMethodName();

  public abstract String getMethodDesc();

  public abstract String getClassName();

  public abstract String getName();

  public abstract Map<String, Object> getAttributes();
}
