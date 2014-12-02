/*    */ package com.github.chenhq.agent.compile;
/*    */
/*    */ public class ClassData
/*    */ {
/*    */   private final byte[] mainClassBytes;
/*    */   private final String shimClassName;
/*    */   private final byte[] shimClassBytes;
/*    */   private final boolean modified;
/*    */
/*    */   public ClassData(byte[] mainClassBytes, String shimClassName, byte[] shimClassBytes, boolean modified)
/*    */   {
/* 10 */     this.mainClassBytes = mainClassBytes;
/* 11 */     this.shimClassName = shimClassName;
/* 12 */     this.shimClassBytes = shimClassBytes;
/* 13 */     this.modified = modified;
/*    */   }
/*    */
/*    */   public ClassData(byte[] mainClassBytes, boolean modified) {
/* 17 */     this(mainClassBytes, null, null, modified);
/*    */   }
/*    */
/*    */   public byte[] getMainClassBytes() {
/* 21 */     return this.mainClassBytes;
/*    */   }
/*    */
/*    */   public String getShimClassName() {
/* 25 */     return this.shimClassName;
/*    */   }
/*    */
/*    */   public byte[] getShimClassBytes() {
/* 29 */     return this.shimClassBytes;
/*    */   }
/*    */
/*    */   public boolean isShimPresent() {
/* 33 */     return this.shimClassName != null;
/*    */   }
/*    */
/*    */   public boolean isModified() {
/* 37 */     return this.modified;
/*    */   }
/*    */ }

/* Location:           /home/think/Downloads/newrelic-android-4.120.0/lib/class.rewriter.jar
 * Qualified Name:     com.newrelic.agent.compile.ClassData
 * JD-Core Version:    0.6.2
 */