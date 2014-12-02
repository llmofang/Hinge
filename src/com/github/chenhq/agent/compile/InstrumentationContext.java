/*     */ package com.github.chenhq.agent.compile;
/*     */
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.Collection;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.WeakHashMap;
/*     */
/*     */ public class InstrumentationContext
/*     */ {
/*  10 */   private static final String[] ANDROID_8_MISSING_CLASS_WHITE_LIST = { "android.view.View$AccessibilityDelegate", "android.view.accessibility.AccessibilityNodeProvider" };
/*     */
/*  16 */   private static final HashMap<Integer, Set<String>> MISSING_CLASS_WHITE_LIST = new HashMap() { } ;
/*     */
/*  20 */   private final WeakHashMap<String, Class<?>> cache = new WeakHashMap();
/*     */   private final ClassRemapperConfig config;
/*     */   private Shim shim;
/*     */   private final Log log;
/*     */   private boolean classModified;
/*     */   private String className;
/*     */   private String superClassName;
/*  27 */   private final ArrayList<String> tags = new ArrayList();
/*     */   private HashMap<String, String> tracedMethods;
/*     */   private HashMap<String, String> skippedMethods;
/*  30 */   private final HashMap<String, ArrayList<String>> tracedMethodParameters = new HashMap();
/*     */
/*     */   public InstrumentationContext(ClassRemapperConfig config, Log log) {
/*  33 */     this.config = config;
/*  34 */     this.log = log;
/*  35 */     this.tracedMethods = new HashMap();
/*  36 */     this.skippedMethods = new HashMap();
/*     */   }
/*     */
/*     */   public Log getLog() {
/*  40 */     return this.log;
/*     */   }
/*     */
/*     */   public void reset() {
/*  44 */     this.classModified = false;
/*  45 */     this.className = null;
/*  46 */     this.superClassName = null;
/*  47 */     this.shim = null;
/*  48 */     this.tags.clear();
/*     */   }
/*     */
/*     */   public void markModified() {
/*  52 */     this.classModified = true;
/*     */   }
/*     */
/*     */   public boolean isClassModified() {
/*  56 */     return this.classModified;
/*     */   }
/*     */
/*     */   public void addTag(String tag) {
/*  60 */     this.tags.add(tag);
/*     */   }
/*     */
/*     */   public void addUniqueTag(String tag) {
/*  64 */     while (this.tags.remove(tag));
/*  65 */     addTag(tag);
/*     */   }
/*     */
/*     */   public void addTracedMethod(String name, String desc) {
/*  69 */     this.log.info("Will trace method " + this.className + "#" + name + ":" + desc + " as requested");
/*  70 */     this.tracedMethods.put(this.className + "#" + name, desc);
/*     */   }
/*     */
/*     */   public void addSkippedMethod(String name, String desc) {
/*  74 */     this.log.info("Will skip all tracing in method " + this.className + "#" + name + ":" + desc + " as requested");
/*  75 */     this.skippedMethods.put(this.className + "#" + name, desc);
/*     */   }
/*     */
/*     */   public void addTracedMethodParameter(String methodName, String parameterName, String parameterClass, String parameterValue) {
/*  79 */     this.log.info("Adding traced method parameter " + parameterName + " for method " + methodName);
/*     */
/*  81 */     String name = this.className + "#" + methodName;
/*  82 */     if (!this.tracedMethodParameters.containsKey(name)) {
/*  83 */       this.tracedMethodParameters.put(name, new ArrayList());
/*     */     }
/*  85 */     ArrayList methodParameters = (ArrayList)this.tracedMethodParameters.get(name);
/*  86 */     methodParameters.add(parameterName);
/*  87 */     methodParameters.add(parameterClass);
/*  88 */     methodParameters.add(parameterValue);
/*     */   }
/*     */
/*     */   public ArrayList<String> getTracedMethodParameters(String methodName) {
/*  92 */     return (ArrayList)this.tracedMethodParameters.get(this.className + "#" + methodName);
/*     */   }
/*     */
/*     */   public boolean isTracedMethod(String name, String desc) {
/*  96 */     return searchMethodMap(this.tracedMethods, name, desc);
/*     */   }
/*     */
/*     */   public boolean isSkippedMethod(String name, String desc) {
/* 100 */     return searchMethodMap(this.skippedMethods, name, desc);
/*     */   }
/*     */
/*     */   private boolean searchMethodMap(Map<String, String> map, String name, String desc) {
/* 104 */     String descToMatch = (String)map.get(this.className + "#" + name);
/*     */
/* 106 */     if (descToMatch == null) {
/* 107 */       return false;
/*     */     }
/* 109 */     if (desc.equals(desc)) {
/* 110 */       return true;
/*     */     }
/*     */
/* 114 */     return false;
/*     */   }
/*     */
/*     */   public List<String> getTags() {
/* 118 */     return this.tags;
/*     */   }
/*     */
/*     */   public boolean hasTag(String tag) {
/* 122 */     return this.tags.contains(tag);
/*     */   }
/*     */
/*     */   public void setClassName(String className) {
/* 126 */     this.className = className;
/*     */   }
/*     */
/*     */   public String getClassName() {
/* 130 */     return this.className;
/*     */   }
/*     */
/*     */   public String getFriendlyClassName() {
/* 134 */     return this.className.replaceAll("/", ".");
/*     */   }
/*     */
/*     */   public String getFriendlySuperClassName() {
/* 138 */     return this.superClassName.replaceAll("/", ".");
/*     */   }
/*     */
/*     */   public String getSimpleClassName() {
/* 142 */     if (this.className.contains("/")) {
/* 143 */       return this.className.substring(this.className.lastIndexOf("/") + 1);
/*     */     }
/* 145 */     return this.className;
/*     */   }
/*     */
/*     */   public void setSuperClassName(String superClassName)
/*     */   {
/* 150 */     this.superClassName = superClassName;
/*     */   }
/*     */
/*     */   public String getSuperClassName() {
/* 154 */     return this.superClassName;
/*     */   }
/*     */
/*     */   public void setShim(Shim shim) {
/* 158 */     this.shim = shim;
/*     */   }
/*     */
/*     */   public Shim getShim() {
/* 162 */     return this.shim;
/*     */   }
/*     */
/*     */   public ClassData newClassData(byte[] mainClassBytes) {
/* 166 */     if (this.shim != null) {
/* 167 */       return new ClassData(mainClassBytes, this.shim.getClassName(), this.shim.getBytes(), isClassModified());
/*     */     }
/*     */
/* 170 */     return new ClassData(mainClassBytes, isClassModified());
/*     */   }
/*     */
/*     */   public ClassMethod getMethodWrapper(ClassMethod method)
/*     */   {
/* 175 */     return this.config.getMethodWrapper(method);
/*     */   }
/*     */
/*     */   public Collection<ClassMethod> getCallSiteReplacements(String className, String methodName, String methodDesc) {
/* 179 */     return this.config.getCallSiteReplacements(className, methodName, methodDesc);
/*     */   }
/*     */ }

/* Location:           /home/think/Downloads/newrelic-android-4.120.0/lib/class.rewriter.jar
 * Qualified Name:     com.newrelic.agent.compile.InstrumentationContext
 * JD-Core Version:    0.6.2
 */