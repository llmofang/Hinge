 package com.llmofang.hinge.agent.compile;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintWriter;

 final class FileLogImpl
   implements Log
 {
   private final PrintWriter writer;
   private boolean isDebug = false;
 
   public FileLogImpl(String logFileName, boolean isDebug)
   {
     this.isDebug = isDebug;
     try {
       this.writer = new PrintWriter(new FileOutputStream(logFileName));
     } catch (FileNotFoundException e) {
       throw new RuntimeException(e);
     }
   }
 
   private void writeln(String level, String message) {
     this.writer.write("[" + level + "] " + message + "\n");
     this.writer.flush();
   }
 
   public void info(String message)
   {
     writeln("info", message);
   }
 
   public void debug(String message)
   {
     if (this.isDebug)
       writeln("debug", message);
   }
 
   public void warning(String message)
   {
     writeln("warn", message);
   }
 
   public void warning(String message, Throwable cause)
   {
     writeln("warn", message);
     cause.printStackTrace(this.writer);
     this.writer.flush();
   }
 
   public void error(String message)
   {
     writeln("error", message);
   }
 
   public void error(String message, Throwable cause)
   {
     writeln("error", message);
     cause.printStackTrace(this.writer);
     this.writer.flush();
   }
 }





