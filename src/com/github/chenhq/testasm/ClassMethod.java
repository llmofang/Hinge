package com.github.chenhq.testasm;

import org.objectweb.asm.commons.Method;

/*    */ //package com.newrelic.agent.compile;
/*    */
/*    */ //import com.newrelic.objectweb.asm.commons.Method;
/*    */
/*    */ public final class ClassMethod
/*    */ {
/*    */   private final String className;
/*    */   private final String methodName;
/*    */   private final String methodDesc;
/*    */
/*    */   public ClassMethod(String className, String methodName, String methodDesc)
/*    */   {
/* 11 */     this.className = className;
/* 12 */     this.methodName = methodName;
/* 13 */     this.methodDesc = methodDesc;
/*    */   }
/*    */
/*    */   static ClassMethod getClassMethod(String signature) {
/*    */     try {
/* 18 */       int descIndex = signature.lastIndexOf('(');
/*    */       String methodDesc;
/*    */       //String methodDesc;
/* 20 */       if (descIndex == -1) {
/* 21 */         descIndex = signature.length();
/* 22 */         methodDesc = "";
/*    */       } else {
/* 24 */         methodDesc = signature.substring(descIndex);
/*    */       }
/* 26 */       String beforeMethodDesc = signature.substring(0, descIndex);
/* 27 */       int methodIndex = beforeMethodDesc.lastIndexOf('.');
/*    */
/* 29 */       return new ClassMethod(signature.substring(0, methodIndex), signature.substring(methodIndex + 1, descIndex), methodDesc);
/*    */     } catch (Exception ex) {
/* 31 */       throw new RuntimeException("Error parsing " + signature, ex);
/*    */     }
/*    */   }
/*    */
/*    */   Method getMethod() {
/* 36 */     return new Method(this.methodName, this.methodDesc);
/*    */   }
/*    */
/*    */   public String getClassName() {
/* 40 */     return this.className;
/*    */   }
/*    */   public String getMethodName() {
/* 43 */     return this.methodName;
/*    */   }
/*    */
/*    */   public String getMethodDesc()
/*    */   {
/* 48 */     return this.methodDesc;
/*    */   }
/*    */
/*    */   public int hashCode()
/*    */   {
/* 53 */     int prime = 31;
/* 54 */     int result = 1;
/* 55 */     result = 31 * result + (this.className == null ? 0 : this.className.hashCode());
/*    */
/* 57 */     result = 31 * result + (this.methodDesc == null ? 0 : this.methodDesc.hashCode());
/*    */
/* 59 */     result = 31 * result + (this.methodName == null ? 0 : this.methodName.hashCode());
/*    */
/* 61 */     return result;
/*    */   }
/*    */
/*    */   public boolean equals(Object obj)
/*    */   {
/* 66 */     if (this == obj)
/* 67 */       return true;
/* 68 */     if (obj == null)
/* 69 */       return false;
/* 70 */     if (getClass() != obj.getClass())
/* 71 */       return false;
/* 72 */     ClassMethod other = (ClassMethod)obj;
/* 73 */     if (this.className == null) {
/* 74 */       if (other.className != null)
/* 75 */         return false;
/* 76 */     } else if (!this.className.equals(other.className))
/* 77 */       return false;
/* 78 */     if (this.methodDesc == null) {
/* 79 */       if (other.methodDesc != null)
/* 80 */         return false;
/* 81 */     } else if (!this.methodDesc.equals(other.methodDesc))
/* 82 */       return false;
/* 83 */     if (this.methodName == null) {
/* 84 */       if (other.methodName != null)
/* 85 */         return false;
/* 86 */     } else if (!this.methodName.equals(other.methodName))
/* 87 */       return false;
/* 88 */     return true;
/*    */   }
/*    */
/*    */   public String toString()
/*    */   {
/* 93 */     return this.className + '.' + this.methodName + this.methodDesc;
/*    */   }
/*    */ }

/* Location:           /home/think/Downloads/newrelic-android-4.120.0/lib/class.rewriter.jar
 * Qualified Name:     com.newrelic.agent.compile.ClassMethod
 * JD-Core Version:    0.6.2
 */