package com.newrelic.agent.compile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;


import java.util.Map.Entry;


public class ClassRemapperConfig {
    public static final String WRAP_METHOD_IDENTIFIER = "WRAP_METHOD:";
    public static final String REPLACE_CALL_SITE_IDENTIFIER = "REPLACE_CALL_SITE:";
    private final Map<ClassMethod, ClassMethod> methodWrappers;
    private final Map<String, Collection<ClassMethod>> callSiteReplacements;

    public ClassRemapperConfig(Log log)
            throws ClassNotFoundException {
        Map remappings = getRemappings(log);
        this.methodWrappers = getMethodWrappers(remappings, log);
        this.callSiteReplacements = getCallSiteReplacements(remappings, log);
    }

    public ClassMethod getMethodWrapper(ClassMethod method) {
        return (ClassMethod) this.methodWrappers.get(method);
    }

    public Collection<ClassMethod> getCallSiteReplacements(String className, String methodName, String methodDesc) {
        ArrayList methods = new ArrayList();

        Collection matches = (Collection) this.callSiteReplacements.get(MessageFormat.format("{0}:{1}", new Object[]{methodName, methodDesc}));
        if (matches != null) {
            methods.addAll(matches);
        }
        matches = (Collection) this.callSiteReplacements.get(MessageFormat.format("{0}.{1}:{2}", new Object[]{className, methodName, methodDesc}));
        if (matches != null) {
            methods.addAll(matches);
        }

        return methods;
    }

    private static Map<ClassMethod, ClassMethod> getMethodWrappers(Map<String, String> remappings, Log log)
            throws ClassNotFoundException {
        HashMap methodWrappers = new HashMap();
        for (Map.Entry entry : remappings.entrySet()) {
            if (((String) entry.getKey()).startsWith("WRAP_METHOD:")) {
                String originalSig = ((String) entry.getKey()).substring("WRAP_METHOD:".length());
                ClassMethod origClassMethod = ClassMethod.getClassMethod(originalSig);
                ClassMethod wrappingMethod = ClassMethod.getClassMethod((String) entry.getValue());

                methodWrappers.put(origClassMethod, wrappingMethod);
            }
        }
        return methodWrappers;
    }

    private static Map<String, Collection<ClassMethod>> getCallSiteReplacements(Map<String, String> remappings, Log log)
            throws ClassNotFoundException {
        HashMap temp = new HashMap();
        for (Map.Entry entry : remappings.entrySet()) {
            if (((String) entry.getKey()).startsWith("REPLACE_CALL_SITE:")) {
                String originalSig = ((String) entry.getKey()).substring("REPLACE_CALL_SITE:".length());

                if (originalSig.contains(".")) {
                    ClassMethod origClassMethod = ClassMethod.getClassMethod(originalSig);
                    ClassMethod replacement = ClassMethod.getClassMethod((String) entry.getValue());

                    String key = MessageFormat.format("{0}.{1}:{2}", new Object[]{origClassMethod.getClassName(), origClassMethod.getMethodName(), origClassMethod.getMethodDesc()});

                    Set replacements = (Set) temp.get(key);
                    if (replacements == null) {
                        replacements = new HashSet();
                        temp.put(key, replacements);
                    }
                    replacements.add(replacement);
                } else {
                    String[] nameDesc = originalSig.split(":");

                    int paren = originalSig.indexOf("(");
                    String methodName = originalSig.substring(0, paren);
                    String methodDesc = originalSig.substring(paren);

                    String key = MessageFormat.format("{0}:{1}", new Object[]{methodName, methodDesc});
                    ClassMethod replacement = ClassMethod.getClassMethod((String) entry.getValue());

                    Set replacements = (Set) temp.get(key);
                    if (replacements == null) {
                        replacements = new HashSet();
                        temp.put(key, replacements);
                    }
                    replacements.add(replacement);
                }
            }
        }

        HashMap callSiteReplacements = new HashMap();

//     for (Map.Entry entry : temp.entrySet()) {
//       callSiteReplacements.put(entry.getKey(), entry.getValue());
//     }
        java.util.Map.Entry entry;
        for (Iterator i$ = temp.entrySet().iterator(); i$.hasNext(); callSiteReplacements.put(entry.getKey(), entry.getValue()))
            entry = (java.util.Map.Entry) i$.next();
        return callSiteReplacements;
    }

    private static Map getRemappings(Log log) {
        Properties props = new Properties();
        URL resource = ClassRemapper.class.getResource("/type_map.properties");
        if (resource == null) {
            log.error("Unable to find the type map");
            System.exit(1);
        }
        InputStream in = null;
        try {
            in = resource.openStream();
            props.load(in);
        } catch (Throwable ex) {
            log.error("Unable to read the type map", ex);
            System.exit(1);
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                }
        }
        return props;
    }
}

/* Location:           /home/cw/class-rewriter/class-rewriter-4.120.0.jar
 * Qualified Name:     com.newrelic.agent.compile.ClassRemapperConfig
 * JD-Core Version:    0.6.2
 */