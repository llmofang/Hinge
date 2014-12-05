 package com.github.chenhq.agent.compile;

 public class ClassData
 {
   private final byte[] mainClassBytes;
   private final String shimClassName;
   private final byte[] shimClassBytes;
   private final boolean modified;

   public ClassData(byte[] mainClassBytes, String shimClassName, byte[] shimClassBytes, boolean modified)
   {
     this.mainClassBytes = mainClassBytes;
     this.shimClassName = shimClassName;
     this.shimClassBytes = shimClassBytes;
     this.modified = modified;
   }

   public ClassData(byte[] mainClassBytes, boolean modified) {
     this(mainClassBytes, null, null, modified);
   }

   public byte[] getMainClassBytes() {
     return this.mainClassBytes;
   }

   public String getShimClassName() {
     return this.shimClassName;
   }

   public byte[] getShimClassBytes() {
     return this.shimClassBytes;
   }

   public boolean isShimPresent() {
     return this.shimClassName != null;
   }

   public boolean isModified() {
     return this.modified;
   }
 }





