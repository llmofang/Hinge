package com.newrelic.agent.compile;

import org.objectweb.asm.ClassWriter;

class PatchedClassWriter extends ClassWriter {
    private final ClassLoader classLoader;

    public PatchedClassWriter(int flags, ClassLoader classLoader) {
        super(flags);
        this.classLoader = classLoader;
    }

    protected String getCommonSuperClass(String type1, String type2) {
        Class c;
        Class d;
        try {
            c = Class.forName(type1.replace('/', '.'), true, this.classLoader);
            d = Class.forName(type2.replace('/', '.'), true, this.classLoader);
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
        if (c.isAssignableFrom(d)) {
            return type1;
        }
        if (d.isAssignableFrom(c)) {
            return type2;
        }
        if ((c.isInterface()) || (d.isInterface())) {
            return "java/lang/Object";
        }
        do
            c = c.getSuperclass();
        while (!c.isAssignableFrom(d));
        return c.getName().replace('.', '/');
    }
}

/* Location:           /home/cw/class-rewriter/class-rewriter-4.120.0.jar
 * Qualified Name:     com.newrelic.agent.compile.PatchedClassWriter
 * JD-Core Version:    0.6.2
 */