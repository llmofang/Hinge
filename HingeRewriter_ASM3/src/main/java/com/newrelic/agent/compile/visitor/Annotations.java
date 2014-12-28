package com.newrelic.agent.compile.visitor;

public final class Annotations {
    public static final String INSTRUMENTED = "Lcom/newrelic/agent/android/instrumentation/Instrumented;";

    public static boolean isNewRelicAnnotation(String descriptor) {
        return descriptor.startsWith("Lcom/newrelic/agent/android/instrumentation/");
    }
}

/* Location:           /home/cw/class-rewriter/class-rewriter-4.120.0.jar
 * Qualified Name:     com.newrelic.agent.compile.visitor.Annotations
 * JD-Core Version:    0.6.2
 */