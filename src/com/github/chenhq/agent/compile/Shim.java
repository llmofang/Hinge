/*    */ package com.github.chenhq.agent.compile;
/*    */
/*    */ import java.io.File;
/*    */ import java.util.HashSet;
/*    */ import java.util.List;
/*    */
/*    */ public final class Shim
/*    */ {
/*    */   public static final String SHIM_CLASS_SUFFIX = "$$NewRelicShim$$1";
/*    */   private final String className;
/*    */   private final String superClassName;
/*    */   private final HashSet<ClassMethod> overrides;
/*    */   private final byte[] bytes;
/*    */
/*    */   public Shim(String className, String superClassName, byte[] bytes, List<ClassMethod> overrides)
/*    */   {
/* 18 */     this.className = className;
/* 19 */     this.superClassName = superClassName;
/* 20 */     this.bytes = bytes;
/* 21 */     this.overrides = new HashSet(overrides);
/*    */   }
/*    */
/*    */   public String getClassName() {
/* 25 */     return this.className;
/*    */   }
/*    */
/*    */   public String getFriendlyClassName() {
/* 29 */     return this.className.replaceAll("/", ".");
/*    */   }
/*    */
/*    */   public String getSuperClassName() {
/* 33 */     return this.superClassName;
/*    */   }
/*    */
/*    */   public boolean overrides(String className, String name, String signature) {
/* 37 */     return this.overrides.contains(new ClassMethod(className, name, signature));
/*    */   }
/*    */
/*    */   public byte[] getBytes() {
/* 41 */     return this.bytes;
/*    */   }
/*    */
/*    */   public static boolean isShimClass(String className) {
/* 45 */     return className.endsWith("$$NewRelicShim$$1");
/*    */   }
/*    */
/*    */   public static String getShimClassName(String className) {
/* 49 */     return className + "$$NewRelicShim$$1";
/*    */   }
/*    */
/*    */   public static File getShimClassFile(String shimClassName, File directory) {
/* 53 */     String[] parts = shimClassName.split("/");
/* 54 */     String className = parts[(parts.length - 1)];
/* 55 */     StringBuilder sb = new StringBuilder();
/* 56 */     for (int i = 0; i < parts.length - 1; i++) {
/* 57 */       sb.append(parts[i]);
/* 58 */       sb.append(File.separator);
/*    */     }
/* 60 */     String packageDir = sb.toString();
/* 61 */     return new File(directory.getAbsolutePath() + File.separator + packageDir + className + ".class");
/*    */   }
/*    */ }

/* Location:           /home/think/Downloads/newrelic-android-4.120.0/lib/class.rewriter.jar
 * Qualified Name:     com.newrelic.agent.compile.Shim
 * JD-Core Version:    0.6.2
 */