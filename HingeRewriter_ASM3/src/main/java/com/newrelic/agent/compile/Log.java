package com.newrelic.agent.compile;

public abstract interface Log {
    public abstract void info(String paramString);

    public abstract void debug(String paramString);

    public abstract void warning(String paramString);

    public abstract void warning(String paramString, Throwable paramThrowable);

    public abstract void error(String paramString);

    public abstract void error(String paramString, Throwable paramThrowable);
}

/* Location:           /home/cw/class-rewriter/class-rewriter-4.120.0.jar
 * Qualified Name:     com.newrelic.agent.compile.Log
 * JD-Core Version:    0.6.2
 */