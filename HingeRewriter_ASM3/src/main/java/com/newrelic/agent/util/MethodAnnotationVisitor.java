package com.newrelic.agent.util;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

import java.util.ArrayList;
import java.util.Collection;

class MethodAnnotationVisitor {
    public static Collection<MethodAnnotation> getAnnotations(ClassReader cr, String annotationDescription) {
        MethodAnnotationClassVisitor visitor = new MethodAnnotationClassVisitor(annotationDescription);
        cr.accept(visitor, 0);
        return visitor.getAnnotations();
    }

    private static class MethodAnnotationClassVisitor extends EmptyVisitor {
        String className;
        private final String annotationDescription;
        private final Collection<MethodAnnotation> annotations = new ArrayList();

        public MethodAnnotationClassVisitor(String annotationDescription) {
            this.annotationDescription = annotationDescription;
        }

        public Collection<MethodAnnotation> getAnnotations() {
            return this.annotations;
        }

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.className = name;
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new MethodAnnotationVisitorImpl(name, desc);
        }

        private class MethodAnnotationVisitorImpl extends EmptyVisitor {
            private final String methodName;
            private final String methodDesc;

            public MethodAnnotationVisitorImpl(String name, String desc) {
                this.methodName = name;
                this.methodDesc = desc;
            }

            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (MethodAnnotationVisitor.MethodAnnotationClassVisitor.this.annotationDescription.equals(desc)) {
                    MethodAnnotationImpl annotation = new MethodAnnotationImpl(desc);
                    MethodAnnotationVisitor.MethodAnnotationClassVisitor.this.annotations.add(annotation);
                    return annotation;
                }
                return null;
            }

            private class MethodAnnotationImpl extends AnnotationImpl implements MethodAnnotation {
                public MethodAnnotationImpl(String desc) {
                    super(desc);
                }

                public String getMethodName() {
                    return MethodAnnotationVisitor.MethodAnnotationClassVisitor.MethodAnnotationVisitorImpl.this.methodName;
                }

                public String getMethodDesc() {
                    return MethodAnnotationVisitor.MethodAnnotationClassVisitor.MethodAnnotationVisitorImpl.this.methodDesc;
                }

                public String getClassName() {
                    return MethodAnnotationVisitor.MethodAnnotationClassVisitor.this.className;
                }
            }
        }
    }
}

/* Location:           /home/cw/class-rewriter/class-rewriter-4.120.0.jar
 * Qualified Name:     com.newrelic.agent.util.MethodAnnotationVisitor
 * JD-Core Version:    0.6.2
 */