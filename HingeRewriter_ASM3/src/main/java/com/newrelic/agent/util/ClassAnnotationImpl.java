package com.newrelic.agent.util;

class ClassAnnotationImpl extends AnnotationImpl
        implements ClassAnnotation {
    private final String className;

    public ClassAnnotationImpl(String className, String name) {
        super(name);
        this.className = className;
    }

    public String getClassName() {
        return this.className;
    }
}

/* Location:           /home/cw/class-rewriter/class-rewriter-4.120.0.jar
 * Qualified Name:     com.newrelic.agent.util.ClassAnnotationImpl
 * JD-Core Version:    0.6.2
 */