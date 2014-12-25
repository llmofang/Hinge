 package com.llmofang.hinge.agent.compile;

 import java.io.File;
 import java.util.HashSet;
 import java.util.List;

 public final class Shim
 {
   public static final String SHIM_CLASS_SUFFIX = "$$NewRelicShim$$1";
   private final String className;
   private final String superClassName;
   private final HashSet<ClassMethod> overrides;
   private final byte[] bytes;

   public Shim(String className, String superClassName, byte[] bytes, List<ClassMethod> overrides)
   {
     this.className = className;
     this.superClassName = superClassName;
     this.bytes = bytes;
     this.overrides = new HashSet(overrides);
   }

   public String getClassName() {
     return this.className;
   }

   public String getFriendlyClassName() {
     return this.className.replaceAll("/", ".");
   }

   public String getSuperClassName() {
     return this.superClassName;
   }

   public boolean overrides(String className, String name, String signature) {
     return this.overrides.contains(new ClassMethod(className, name, signature));
   }

   public byte[] getBytes() {
     return this.bytes;
   }

   public static boolean isShimClass(String className) {
     return className.endsWith("$$NewRelicShim$$1");
   }

   public static String getShimClassName(String className) {
     return className + "$$NewRelicShim$$1";
   }

   public static File getShimClassFile(String shimClassName, File directory) {
     String[] parts = shimClassName.split("/");
     String className = parts[(parts.length - 1)];
     StringBuilder sb = new StringBuilder();
     for (int i = 0; i < parts.length - 1; i++) {
       sb.append(parts[i]);
       sb.append(File.separator);
     }
     String packageDir = sb.toString();
     return new File(directory.getAbsolutePath() + File.separator + packageDir + className + ".class");
   }
 }





