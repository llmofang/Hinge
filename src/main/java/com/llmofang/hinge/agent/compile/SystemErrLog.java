 package com.llmofang.hinge.agent.compile;


 import java.util.Map;


 final class SystemErrLog implements Log
 {
   private final Map<String, String> agentOptions;
 
   public SystemErrLog(Map<String, String> agentOptions)
   {
     this.agentOptions = agentOptions;
   }
 
   public void info(String message)
   {
     System.out.println("[newrelic.info] " + message);
   }
 
   public void debug(String message)
   {
     if (this.agentOptions.get("debug") != null)
       System.out.println("[newrelic.debug] " + message);
   }
 
   public void warning(String message)
   {
     System.err.println("[newrelic.warn] " + message);
   }
 
   public void warning(String message, Throwable cause)
   {
     System.err.println("[newrelic.warn] " + message);
     cause.printStackTrace(System.err);
   }
 
   public void error(String message)
   {
     System.err.println("[newrelic.error] " + message);
   }
 
   public void error(String message, Throwable cause)
   {
     System.err.println("[newrelic.error] " + message);
     cause.printStackTrace(System.err);
   }
 }





