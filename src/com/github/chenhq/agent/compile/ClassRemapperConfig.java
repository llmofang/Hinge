/*     */ package com.github.chenhq.agent.compile;
/*     */
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.net.URL;
/*     */ import java.text.MessageFormat;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */
/*     */ public class ClassRemapperConfig
/*     */ {
/*     */   public static final String WRAP_METHOD_IDENTIFIER = "WRAP_METHOD:";
/*     */   public static final String REPLACE_CALL_SITE_IDENTIFIER = "REPLACE_CALL_SITE:";
/*     */   private final Map<ClassMethod, ClassMethod> methodWrappers;
/*     */   private final Map<String, Collection<ClassMethod>> callSiteReplacements;
/*     */
/*     */   public ClassRemapperConfig(Log log)
/*     */     throws ClassNotFoundException
/*     */   {
/*  19 */     Map remappings = getRemappings(log);
/*  20 */     this.methodWrappers = getMethodWrappers(remappings, log);
/*  21 */     this.callSiteReplacements = getCallSiteReplacements(remappings, log);
/*     */   }
/*     */
/*     */   public ClassMethod getMethodWrapper(ClassMethod method) {
/*  25 */     return (ClassMethod)this.methodWrappers.get(method);
/*     */   }
/*     */
/*     */   public Collection<ClassMethod> getCallSiteReplacements(String className, String methodName, String methodDesc) {
/*  29 */     ArrayList methods = new ArrayList();
/*     */
/*  35 */     Collection matches = (Collection)this.callSiteReplacements.get(MessageFormat.format("{0}:{1}", new Object[] { methodName, methodDesc }));
/*  36 */     if (matches != null) {
/*  37 */       methods.addAll(matches);
/*     */     }
/*  39 */     matches = (Collection)this.callSiteReplacements.get(MessageFormat.format("{0}.{1}:{2}", new Object[] { className, methodName, methodDesc }));
/*  40 */     if (matches != null) {
/*  41 */       methods.addAll(matches);
/*     */     }
/*     */
/*  44 */     return methods;
/*     */   }
/*     */
/*     */   private static Map<ClassMethod, ClassMethod> getMethodWrappers(Map<String, String> remappings, Log log)
/*     */     throws ClassNotFoundException
/*     */   {
/*  55 */     HashMap methodWrappers = new HashMap();
/*  56 */     for (Map.Entry entry : remappings.entrySet()) {
/*  57 */       if (((String)entry.getKey()).startsWith("WRAP_METHOD:")) {
/*  58 */         String originalSig = ((String)entry.getKey()).substring("WRAP_METHOD:".length());
/*  59 */         ClassMethod origClassMethod = ClassMethod.getClassMethod(originalSig);
/*  60 */         ClassMethod wrappingMethod = ClassMethod.getClassMethod((String)entry.getValue());
/*     */
/*  62 */         methodWrappers.put(origClassMethod, wrappingMethod);
/*     */       }
/*     */     }
/*  65 */     return methodWrappers;
/*     */   }
/*     */
/*     */   private static Map<String, Collection<ClassMethod>> getCallSiteReplacements(Map<String, String> remappings, Log log)
/*     */     throws ClassNotFoundException
/*     */   {
/*  77 */     HashMap temp = new HashMap();
/*  78 */     for (Map.Entry entry : remappings.entrySet()) {
/*  79 */       if (((String)entry.getKey()).startsWith("REPLACE_CALL_SITE:")) {
/*  80 */         String originalSig = ((String)entry.getKey()).substring("REPLACE_CALL_SITE:".length());
/*     */
/*  84 */         if (originalSig.contains(".")) {
/*  85 */           ClassMethod origClassMethod = ClassMethod.getClassMethod(originalSig);
/*  86 */           ClassMethod replacement = ClassMethod.getClassMethod((String)entry.getValue());
/*     */
/*  88 */           String key = MessageFormat.format("{0}.{1}:{2}", new Object[] { origClassMethod.getClassName(), origClassMethod.getMethodName(), origClassMethod.getMethodDesc() });
/*     */
/*  90 */           Set replacements = (Set)temp.get(key);
/*  91 */           if (replacements == null) {
/*  92 */             replacements = new HashSet();
/*  93 */             temp.put(key, replacements);
/*     */           }
/*  95 */           replacements.add(replacement);
/*     */         } else {
/*  97 */           String[] nameDesc = originalSig.split(":");
/*     */
/*  99 */           int paren = originalSig.indexOf("(");
/* 100 */           String methodName = originalSig.substring(0, paren);
/* 101 */           String methodDesc = originalSig.substring(paren);
/*     */
/* 103 */           String key = MessageFormat.format("{0}:{1}", new Object[] { methodName, methodDesc });
/* 104 */           ClassMethod replacement = ClassMethod.getClassMethod((String)entry.getValue());
/*     */
/* 106 */           Set replacements = (Set)temp.get(key);
/* 107 */           if (replacements == null) {
/* 108 */             replacements = new HashSet();
/* 109 */             temp.put(key, replacements);
/*     */           }
/* 111 */           replacements.add(replacement);
/*     */         }
/*     */       }
/*     */     }
/*     */
/* 116 */     HashMap callSiteReplacements = new HashMap();
/*     */
/* 118 */     for (Map.Entry entry : temp.entrySet()) {
/* 119 */       callSiteReplacements.put(entry.getKey(), entry.getValue());
/*     */     }
/* 121 */     return callSiteReplacements;
/*     */   }
/*     */
/*     */   private static Map getRemappings(Log log)
/*     */   {
/* 131 */     Properties props = new Properties();
/* 132 */     URL resource = ClassRemapper.class.getResource("/type_map.properties");
/* 133 */     if (resource == null) {
/* 134 */       log.error("Unable to find the type map");
/* 135 */       System.exit(1);
/*     */     }
/* 137 */     InputStream in = null;
/*     */     try {
/* 139 */       in = resource.openStream();
/* 140 */       props.load(in);
/*     */     } catch (Throwable ex) {
/* 142 */       log.error("Unable to read the type map", ex);
/* 143 */       System.exit(1);
/*     */     } finally {
/* 145 */       if (in != null)
/*     */         try {
/* 147 */           in.close();
/*     */         }
/*     */         catch (IOException e) {
/*     */         }
/*     */     }
/* 152 */     return props;
/*     */   }
/*     */ }

/* Location:           /home/think/Downloads/newrelic-android-4.120.0/lib/class.rewriter.jar
 * Qualified Name:     com.newrelic.agent.compile.ClassRemapperConfig
 * JD-Core Version:    0.6.2
 */