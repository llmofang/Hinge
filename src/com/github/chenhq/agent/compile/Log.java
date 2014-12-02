package com.github.chenhq.agent.compile;

public abstract interface Log
{
  public abstract void info(String paramString);

  public abstract void debug(String paramString);

  public abstract void warning(String paramString);

  public abstract void warning(String paramString, Throwable paramThrowable);

  public abstract void error(String paramString);

  public abstract void error(String paramString, Throwable paramThrowable);
}
