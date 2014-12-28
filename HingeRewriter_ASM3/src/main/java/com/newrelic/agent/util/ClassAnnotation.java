package com.newrelic.agent.util;

import java.util.Map;

public abstract interface ClassAnnotation {
    public abstract String getClassName();

    public abstract String getName();

    public abstract Map<String, Object> getAttributes();
}

/* Location:           /home/cw/class-rewriter/class-rewriter-4.120.0.jar
 * Qualified Name:     com.newrelic.agent.util.ClassAnnotation
 * JD-Core Version:    0.6.2
 */