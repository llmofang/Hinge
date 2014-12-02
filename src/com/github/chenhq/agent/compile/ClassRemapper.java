/*     */ package com.github.chenhq.agent.compile;
/*     */
/*     */ import com.github.chenhq.agent.compile.visitor.AnnotatingClassVisitor;
/*     */ import com.github.chenhq.agent.compile.visitor.ContextInitializationClassVisitor;
/*     */ import com.github.chenhq.agent.compile.visitor.PrefilterClassVisitor;
/*     */ import com.github.chenhq.agent.compile.visitor.WrapMethodClassVisitor;
/*     */ import com.github.chenhq.agent.util.Streams;
/*     */ import org.objectweb.asm.ClassReader;
/*     */ import org.objectweb.asm.ClassVisitor;
/*     */ import org.objectweb.asm.ClassWriter;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.PrintStream;
/*     */ import java.net.URL;
/*     */ import java.security.MessageDigest;
/*     */ import java.text.MessageFormat;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Properties;
/*     */ import java.util.jar.Attributes;
/*     */ import java.util.jar.JarEntry;
/*     */ import java.util.jar.JarFile;
/*     */ import java.util.jar.JarOutputStream;
/*     */ import java.util.jar.Manifest;
/*     */ import java.util.regex.Pattern;
/*     */
/*     */ public class ClassRemapper
/*     */ {
/*     */   public static final String NEW_RELIC_APP_VERSION = "New-Relic-App-Version";
/*     */   public static final String NEW_RELIC_VERSION_MARKER = "New-Relic-Version";
/*     */   private static final String NEW_RELIC_BACKUP_HASH_MARKER = "New-Relic-Backup-Hash";
/*     */   private static final String MANIFEST_MF_PATH = "META-INF/MANIFEST.MF";
/*     */   private final Log log;
/*     */   private final File outputDirectory;
/*     */   private final File jarBackupDirectory;
/*     */   private final ClassRemapperConfig config;
/*     */   private String appVersion;
/*     */   private final InstrumentationContext context;
/*     */   volatile int modificationCount;
/*     */
/*     */   public ClassRemapper(File directory)
/*     */     throws FileNotFoundException, IOException, ClassNotFoundException
/*     */   {
/*  58 */     this(directory, null);
/*     */   }
/*     */
/*     */   public ClassRemapper(File directory, File jarBackupDirectory) throws FileNotFoundException, IOException, ClassNotFoundException {
/*  62 */     this(new DefaultLogImpl(null), ClassLoader.getSystemClassLoader(), directory, jarBackupDirectory);
/*     */   }
/*     */
/*     */   public ClassRemapper(Log log, ClassLoader classLoader, File directory, File jarBackupDirectory)
/*     */     throws FileNotFoundException, IOException, ClassNotFoundException
/*     */   {
/*  68 */     if (log == null) {
/*  69 */       log = new DefaultLogImpl(null);
/*     */     }
/*     */
/*  72 */     this.log = log;
/*  73 */     this.outputDirectory = directory;
/*  74 */     this.jarBackupDirectory = jarBackupDirectory;
/*     */
/*  76 */     this.config = new ClassRemapperConfig(log);
/*     */
/*  78 */     this.context = new InstrumentationContext(this.config, log);
/*     */
/*  80 */     Enumeration manifests = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
/*     */
/*  82 */     while (manifests.hasMoreElements()) {
/*  83 */       Properties props = new Properties();
/*  84 */       InputStream stream = ((URL)manifests.nextElement()).openStream();
/*     */       try {
/*  86 */         props.load(stream);
/*  87 */         this.appVersion = props.getProperty("New-Relic-App-Version");
/*  88 */         if (this.appVersion != null)
/*     */         {
/*  94 */           stream.close(); break; }  } catch (IOException e) { stream.close(); } finally { stream.close(); }
/*     */
/*     */     }
/*     */
/*  98 */     if (this.appVersion == null)
/*  99 */       throw new FileNotFoundException("Could not find MANIFEST.MF with New-Relic-App-Version");
/*     */   }
/*     */
/*     */   public void rewriteClasses(File dir)
/*     */   {
/* 111 */     if (!dir.isDirectory()) {
/* 112 */       throw new RuntimeException("Expected " + dir.getAbsolutePath() + " to be a directory");
/*     */     }
/* 114 */     for (File file : dir.listFiles())
/* 115 */       if (file.isDirectory())
/*     */       {
/* 119 */         rewriteClasses(file);
/* 120 */       } else if (file.getName().endsWith(".class"))
/*     */       {
/*     */         try
/*     */         {
/* 125 */           rewriteClass(file);
/*     */         } catch (Throwable ex) {
/* 127 */           this.log.error(ex.getMessage(), ex);
/*     */         }
/*     */       }
/*     */   }
/*     */
/*     */   public void rewriteJars(File dir, boolean recurse)
/*     */   {
/* 139 */     if (!dir.isDirectory()) {
/* 140 */       throw new RuntimeException("Expected " + dir.getAbsolutePath() + " to be a directory");
/*     */     }
/*     */
/* 143 */     for (File file : dir.listFiles())
/* 144 */       if ((file.isDirectory()) && (recurse))
/*     */       {
/* 148 */         rewriteJars(dir, recurse);
/* 149 */       } else if (file.getName().endsWith(".jar"))
/*     */       {
/*     */         try
/*     */         {
/* 154 */           rewriteJar(file);
/*     */         }
/*     */         catch (Throwable ex) {
/* 157 */           this.log.error(ex.getMessage(), ex);
/*     */         }
/*     */       }
/*     */   }
/*     */
/*     */   public boolean rewriteJar(File file) throws Exception
/*     */   {
/* 164 */     return rewriteJar(file, 0);
/*     */   }
/*     */
/*     */   private String generateHash(File file)
/*     */     throws Exception
/*     */   {
/* 171 */     MessageDigest messageDigest = MessageDigest.getInstance("SHA");
/* 172 */     FileInputStream fin = new FileInputStream(file);
/*     */     try {
/* 174 */       byte[] buf = new byte[8192];
/*     */       while (true) {
/* 176 */         int n = fin.read(buf);
/* 177 */         if (n <= 0) break;
/* 178 */         messageDigest.update(buf, 0, n);
/*     */       }
/*     */
/* 184 */       StringBuilder sb = new StringBuilder();
/* 185 */       byte[] digest = messageDigest.digest();
/* 186 */       for (int i = 0; i < digest.length; i++) {
/* 187 */         sb.append(Integer.toHexString(digest[i] & 0xFF));
/*     */       }
/* 189 */       return sb.toString();
/*     */     }
/*     */     finally {
/* 192 */       fin.close();
/*     */     }
/*     */   }
/*     */
/*     */   private boolean rewriteJar(File file, int depth)
/*     */     throws Exception
/*     */   {
/* 207 */     if (Pattern.matches("^android-support-v[^\\.]+\\.jar$", file.getName())) {
/* 208 */       this.log.info("skipping android support jar: " + file.getPath());
/* 209 */       return false;
/*     */     }
/*     */
/* 212 */     this.log.info("process jar file: " + file.getPath());
/*     */
/* 217 */     if (this.jarBackupDirectory == null) {
/* 218 */       this.log.error("no jar backup directory specified! exiting ...");
/*     */
/* 222 */       System.exit(1);
/*     */     }
/*     */
/* 228 */     JarFile jarFile = new JarFile(file);
/* 229 */     ByteArrayOutputStream bytes = new ByteArrayOutputStream();
/* 230 */     boolean anyModified = false;
/*     */     try {
/* 232 */       Manifest manifest = jarFile.getManifest();
/* 233 */       if (manifest == null)
/*     */       {
/* 237 */         this.log.info("creating a manifest file");
/* 238 */         manifest = new Manifest();
/* 239 */         manifest.getMainAttributes().putValue("New-Relic-Version", this.appVersion);
/* 240 */         manifest.getMainAttributes().putValue("New-Relic-Backup-Hash", generateHash(file));
/*     */       }
/* 242 */       else if (manifest.getMainAttributes().getValue("New-Relic-Version") != null) {
/* 243 */         this.log.warning("jar has already been instrumented by New Relic: " + file);
/* 244 */         return false;
/*     */       }
/*     */
/* 247 */       JarOutputStream jos = new JarOutputStream(bytes);
/*     */
/* 252 */       Enumeration entries = jarFile.entries();
/* 253 */       while (entries.hasMoreElements()) {
/* 254 */         JarEntry entry = (JarEntry)entries.nextElement();
/* 255 */         InputStream is = new BufferedInputStream(jarFile.getInputStream(entry));
/*     */         try {
/* 257 */           if (entry.getName().equals("META-INF/MANIFEST.MF"))
/*     */           {
/* 261 */             JarEntry newEntry = new JarEntry(entry.getName());
/* 262 */             jos.putNextEntry(newEntry);
/* 263 */             manifest.write(jos);
/* 264 */             jos.closeEntry();
/*     */
/* 309 */             is.close(); continue;
/*     */           }
/* 267 */           JarEntry newEntry = new JarEntry(entry.getName());
/* 268 */           jos.putNextEntry(newEntry);
/* 269 */           if ((!entry.getName().contains("com/newrelic/agent/")) && (entry.getName().endsWith(".class")))
/*     */           {
/* 273 */             ByteArrayOutputStream classBytes = new ByteArrayOutputStream();
/* 274 */             Streams.copy(is, classBytes);
/* 275 */             classBytes.close();
/* 276 */             ClassData classData = visitClassBytes(classBytes.toByteArray());
/* 277 */             ByteArrayInputStream byteStream = new ByteArrayInputStream(classData.getMainClassBytes());
/*     */             try {
/* 279 */               Streams.copy(byteStream, jos);
/*     */             }
/*     */             finally
/*     */             {
/*     */             }
/*     */
/* 287 */             anyModified |= classData.isModified();
/*     */
/* 292 */             if (classData.isShimPresent()) {
/* 293 */               jos.closeEntry();
/*     */
/* 295 */               JarEntry shimEntry = new JarEntry(classData.getShimClassName() + ".class");
/* 296 */               jos.putNextEntry(shimEntry);
/* 297 */               ByteArrayInputStream shimByteStream = new ByteArrayInputStream(classData.getShimClassBytes());
/*     */               try {
/* 299 */                 Streams.copy(shimByteStream, jos);
/*     */               }
/*     */               finally {
/*     */               }
/*     */             }
/*     */           }
/*     */           else {
/* 306 */             Streams.copy(is, jos);
/*     */           }
/*     */         }
/*     */         finally
/*     */         {
/*     */         }
/* 312 */         jos.flush();
/* 313 */         jos.closeEntry();
/*     */       }
/* 315 */       jos.close();
/*     */     }
/*     */     finally
/*     */     {
/* 319 */       jarFile.close();
/*     */     }
/*     */
/* 325 */     if (anyModified)
/*     */     {
/* 329 */       this.log.info("write jar file: " + file.getName());
/* 330 */       Streams.copyBytesToFile(file, bytes.toByteArray());
/* 331 */       return true;
/*     */     }
/*     */
/* 334 */     this.log.info("no classes modified: jar will not be written");
/* 335 */     return false;
/*     */   }
/*     */
/*     */   public boolean rewriteClass(File file)
/*     */     throws Exception
/*     */   {
/* 345 */     if (this.outputDirectory == null) {
/* 346 */       throw new RuntimeException("No output directory specified when attempting to process " + file.getAbsolutePath());
/*     */     }
/*     */
/* 349 */     if ((file.getAbsolutePath().contains("com/newrelic")) || (file.getAbsolutePath().endsWith("$$NewRelicShim$$1.class"))) {
/* 350 */       return false;
/*     */     }
/*     */
/* 353 */     ClassData classData = visitClassBytes(getBytes(file));
/*     */
/* 355 */     if ((classData.getMainClassBytes() != null) && (classData.isModified())) {
/* 356 */       this.log.info(MessageFormat.format("[{0}] modified classfile {1}", new Object[] { this.context.getClassName().replaceAll("/", "."), file.getName() }));
/* 357 */       Streams.copyBytesToFile(file, classData.getMainClassBytes());
/*     */
/* 359 */       if (classData.isShimPresent()) {
/* 360 */         FileOutputStream out = new FileOutputStream(this.outputDirectory.getAbsolutePath() + File.separator + classData.getShimClassName() + ".class");
/*     */         try {
/* 362 */           out.write(classData.getShimClassBytes());
/*     */         }
/*     */         finally {
/* 365 */           out.close();
/*     */         }
/*     */       }
/*     */
/* 369 */       return true;
/*     */     }
/*     */
/* 372 */     return false;
/*     */   }
/*     */
/*     */   private byte[] getBytes(File file)
/*     */     throws Exception
/*     */   {
/* 382 */     ByteArrayOutputStream originalBytes = new ByteArrayOutputStream((int)file.length());
/*     */
/* 384 */     InputStream inStream = null;
/*     */     try {
/* 386 */       inStream = new BufferedInputStream(new FileInputStream(file));
/* 387 */       Streams.copy(inStream, originalBytes, true);
/*     */     } catch (Throwable t) {
/* 389 */       throw new Exception("Unable to read file: " + file.getName(), t);
/*     */     }
/* 391 */     return originalBytes.toByteArray();
/*     */   }
/*     */
/*     */   private ClassData visitClassBytes(byte[] bytes)
/*     */   {
/*     */     try
/*     */     {
/* 401 */       ClassReader cr = new ClassReader(bytes);
/* 402 */       ClassWriter cw = new ClassWriter(cr, 1);
/*     */
/* 404 */       this.context.reset();
/*     */
/* 409 */       cr.accept(new PrefilterClassVisitor(this.context, this.log), 0);
/*     */
/* 411 */       if (!this.context.hasTag("Lcom/newrelic/agent/android/instrumentation/Instrumented;"))
/*     */       {
/* 416 */         ClassVisitor cv = new AnnotatingClassVisitor(cw, this.context, this.log);
/*     */
/* 418 */         cv = new WrapMethodClassVisitor(cv, this.context, this.log);
/* 419 */         cv = new ContextInitializationClassVisitor(cv, this.context);
/* 420 */         cr.accept(cv, 8);
/*     */       }
/*     */       else {
/* 423 */         this.log.warning(MessageFormat.format("[{0}] class is already instrumented! skipping ...", new Object[] { this.context.getFriendlyClassName() }));
/*     */       }
/*     */
/* 426 */       return this.context.newClassData(cw.toByteArray());
/*     */     } catch (Throwable t) {
/* 428 */       this.log.error(t.getMessage(), t);
/* 429 */     }return new ClassData(bytes, false);
/*     */   }
/*     */
/*     */   private static final class DefaultLogImpl implements Log
/*     */   {
/*     */     public void info(String message)
/*     */     {
/* 436 */       System.out.println("[info] " + message);
/*     */     }
/*     */
/*     */     public void warning(String message)
/*     */     {
/* 441 */       System.err.println("[warn] " + message);
/*     */     }
/*     */
/*     */     public void warning(String message, Throwable cause)
/*     */     {
/* 446 */       System.err.println("[warn] " + message);
/* 447 */       cause.printStackTrace(System.err);
/*     */     }
/*     */
/*     */     public void error(String message)
/*     */     {
/* 452 */       System.err.println("[error] " + message);
/*     */     }
/*     */
/*     */     public void error(String message, Throwable cause)
/*     */     {
/* 457 */       System.err.println("[error] " + message);
/* 458 */       cause.printStackTrace(System.err);
/*     */     }
/*     */
/*     */     public void debug(String message)
/*     */     {
/* 463 */       System.out.println("[debug] " + message);
/*     */     }
/*     */   }
/*     */ }

/* Location:           /home/think/Downloads/newrelic-android-4.120.0/lib/class.rewriter.jar
 * Qualified Name:     com.newrelic.agent.compile.ClassRemapper
 * JD-Core Version:    0.6.2
 */