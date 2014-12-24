package com.llmofang.hinge.agent.compile;

import com.github.chenhq.agent.compile.visitor.AnnotatingClassVisitor;
import com.github.chenhq.agent.compile.visitor.ContextInitializationClassVisitor;
import com.github.chenhq.agent.compile.visitor.PrefilterClassVisitor;
import com.github.chenhq.agent.compile.visitor.WrapMethodClassVisitor;
import com.github.chenhq.agent.util.Streams;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

public class ClassRemapper {
	public static final String NEW_RELIC_APP_VERSION = "New-Relic-App-Version";
	public static final String NEW_RELIC_VERSION_MARKER = "New-Relic-Version";
	private static final String NEW_RELIC_BACKUP_HASH_MARKER = "New-Relic-Backup-Hash";
	private static final String MANIFEST_MF_PATH = "META-INF/MANIFEST.MF";
	private final Log log;
	private final File outputDirectory;
	private final File jarBackupDirectory;
	private final ClassRemapperConfig config;
	private String appVersion;
	private final InstrumentationContext context;
	volatile int modificationCount;

	public ClassRemapper(File directory) throws FileNotFoundException,
			IOException, ClassNotFoundException {
		this(directory, null);
	}

	public ClassRemapper(File directory, File jarBackupDirectory)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		this(new DefaultLogImpl(), ClassLoader.getSystemClassLoader(),
				directory, jarBackupDirectory);
	}

	public ClassRemapper(Log log, ClassLoader classLoader, File directory,
			File jarBackupDirectory) throws FileNotFoundException, IOException,
			ClassNotFoundException {
		if (log == null) {
			log = new DefaultLogImpl();
		}

		this.log = log;
		this.outputDirectory = directory;
		this.jarBackupDirectory = jarBackupDirectory;

		this.config = new ClassRemapperConfig(log);

		this.context = new InstrumentationContext(this.config, log);

		Enumeration manifests = getClass().getClassLoader().getResources(
				"META-INF/MANIFEST.MF");

		while (manifests.hasMoreElements()) {
			Properties props = new Properties();
			InputStream stream = ((URL) manifests.nextElement()).openStream();
			try {
				props.load(stream);
				this.appVersion = props.getProperty("New-Relic-App-Version");
				if (this.appVersion != null) {
					stream.close();
					break;
				}
			} catch (IOException e) {
				stream.close();
			} finally {
				stream.close();
			}

		}

		if (this.appVersion == null)
			throw new FileNotFoundException(
					"Could not find MANIFEST.MF with New-Relic-App-Version");
	}

	public void rewriteClasses(File dir) {
		if (!dir.isDirectory()) {
			throw new RuntimeException("Expected " + dir.getAbsolutePath()
					+ " to be a directory");
		}
		for (File file : dir.listFiles())
			if (file.isDirectory()) {
				rewriteClasses(file);
			} else if (file.getName().endsWith(".class")) {
				try {
					rewriteClass(file);
				} catch (Throwable ex) {
					this.log.error(ex.getMessage(), ex);
				}
			}
	}

	public void rewriteJars(File dir, boolean recurse) {
		if (!dir.isDirectory()) {
			throw new RuntimeException("Expected " + dir.getAbsolutePath()
					+ " to be a directory");
		}

		for (File file : dir.listFiles())
			if ((file.isDirectory()) && (recurse)) {
				rewriteJars(dir, recurse);
			} else if (file.getName().endsWith(".jar")) {
				try {
					rewriteJar(file);
				} catch (Throwable ex) {
					this.log.error(ex.getMessage(), ex);
				}
			}
	}

	public boolean rewriteJar(File file) throws Exception {
		return rewriteJar(file, 0);
	}

	private String generateHash(File file) throws Exception {
		MessageDigest messageDigest = MessageDigest.getInstance("SHA");
		FileInputStream fin = new FileInputStream(file);
		try {
			byte[] buf = new byte[8192];
			while (true) {
				int n = fin.read(buf);
				if (n <= 0)
					break;
				messageDigest.update(buf, 0, n);
			}

			StringBuilder sb = new StringBuilder();
			byte[] digest = messageDigest.digest();
			for (int i = 0; i < digest.length; i++) {
				sb.append(Integer.toHexString(digest[i] & 0xFF));
			}
			return sb.toString();
		} finally {
			fin.close();
		}
	}

	private boolean rewriteJar(File file, int depth) throws Exception {
		if (Pattern.matches("^android-support-v[^\\.]+\\.jar$", file.getName())) {
			this.log.info("skipping android support jar: " + file.getPath());
			return false;
		}

		this.log.info("process jar file: " + file.getPath());

		if (this.jarBackupDirectory == null) {
			this.log.error("no jar backup directory specified! exiting ...");

			System.exit(1);
		}

		JarFile jarFile = new JarFile(file);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		boolean anyModified = false;
		try {
			Manifest manifest = jarFile.getManifest();
			if (manifest == null) {
				this.log.info("creating a manifest file");
				manifest = new Manifest();
				manifest.getMainAttributes().putValue("New-Relic-Version",
						this.appVersion);
				manifest.getMainAttributes().putValue("New-Relic-Backup-Hash",
						generateHash(file));
			} else if (manifest.getMainAttributes().getValue(
					"New-Relic-Version") != null) {
				this.log.warning("jar has already been instrumented by New Relic: "
						+ file);
				return false;
			}

			JarOutputStream jos = new JarOutputStream(bytes);

			Enumeration entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = (JarEntry) entries.nextElement();
				InputStream is = new BufferedInputStream(
						jarFile.getInputStream(entry));
				try {
					if (entry.getName().equals("META-INF/MANIFEST.MF")) {
						JarEntry newEntry = new JarEntry(entry.getName());
						jos.putNextEntry(newEntry);
						manifest.write(jos);
						jos.closeEntry();

						is.close();
						continue;
					}
					JarEntry newEntry = new JarEntry(entry.getName());
					jos.putNextEntry(newEntry);
					if ((!entry.getName().contains("com/newrelic/agent/"))
							&& (entry.getName().endsWith(".class"))) {
						ByteArrayOutputStream classBytes = new ByteArrayOutputStream();
						Streams.copy(is, classBytes);
						classBytes.close();
						ClassData classData = visitClassBytes(classBytes
								.toByteArray());
						ByteArrayInputStream byteStream = new ByteArrayInputStream(
								classData.getMainClassBytes());
						try {
							Streams.copy(byteStream, jos);
						} finally {
						}

						anyModified |= classData.isModified();

						if (classData.isShimPresent()) {
							jos.closeEntry();

							JarEntry shimEntry = new JarEntry(
									classData.getShimClassName() + ".class");
							jos.putNextEntry(shimEntry);
							ByteArrayInputStream shimByteStream = new ByteArrayInputStream(
									classData.getShimClassBytes());
							try {
								Streams.copy(shimByteStream, jos);
							} finally {
							}
						}
					} else {
						Streams.copy(is, jos);
					}
				} finally {
				}
				jos.flush();
				jos.closeEntry();
			}
			jos.close();
		} finally {
			jarFile.close();
		}

		if (anyModified) {
			this.log.info("write jar file: " + file.getName());
			Streams.copyBytesToFile(file, bytes.toByteArray());
			return true;
		}

		this.log.info("no classes modified: jar will not be written");
		return false;
	}

	public boolean rewriteClass(File file) throws Exception {
		if (this.outputDirectory == null) {
			throw new RuntimeException(
					"No output directory specified when attempting to process "
							+ file.getAbsolutePath());
		}

		if ((file.getAbsolutePath().contains("com/newrelic"))
				|| (file.getAbsolutePath().endsWith("$$NewRelicShim$$1.class"))) {
			return false;
		}

		ClassData classData = visitClassBytes(getBytes(file));

		if ((classData.getMainClassBytes() != null) && (classData.isModified())) {
			this.log.info(MessageFormat.format(
					"[{0}] modified classfile {1}",
					new Object[] {
							this.context.getClassName().replaceAll("/", "."),
							file.getName() }));
			Streams.copyBytesToFile(file, classData.getMainClassBytes());

			if (classData.isShimPresent()) {
				FileOutputStream out = new FileOutputStream(
						this.outputDirectory.getAbsolutePath() + File.separator
								+ classData.getShimClassName() + ".class");
				try {
					out.write(classData.getShimClassBytes());
				} finally {
					out.close();
				}
			}

			return true;
		}

		return false;
	}

	private byte[] getBytes(File file) throws Exception {
		ByteArrayOutputStream originalBytes = new ByteArrayOutputStream(
				(int) file.length());

		InputStream inStream = null;
		try {
			inStream = new BufferedInputStream(new FileInputStream(file));
			Streams.copy(inStream, originalBytes, true);
		} catch (Throwable t) {
			throw new Exception("Unable to read file: " + file.getName(), t);
		}
		return originalBytes.toByteArray();
	}

	private ClassData visitClassBytes(byte[] bytes) {
		try {
			ClassReader cr = new ClassReader(bytes);
			ClassWriter cw = new ClassWriter(cr, 1);

			this.context.reset();

			cr.accept(new PrefilterClassVisitor(this.context, this.log), 0);

			if (!this.context
					.hasTag("Lcom/newrelic/agent/android/instrumentation/Instrumented;")) {
				ClassVisitor cv = new AnnotatingClassVisitor(cw, this.context,
						this.log);

				cv = new WrapMethodClassVisitor(cv, this.context, this.log);
				cv = new ContextInitializationClassVisitor(cv, this.context);
				cr.accept(cv, 8);
			} else {
				this.log.warning(MessageFormat.format(
						"[{0}] class is already instrumented! skipping ...",
						new Object[] { this.context.getFriendlyClassName() }));
			}

			return this.context.newClassData(cw.toByteArray());
		} catch (Throwable t) {
			this.log.error(t.getMessage(), t);
		}
		return new ClassData(bytes, false);
	}

	private static final class DefaultLogImpl implements Log {
		public void info(String message) {
			System.out.println("[info] " + message);
		}

		public void warning(String message) {
			System.err.println("[warn] " + message);
		}

		public void warning(String message, Throwable cause) {
			System.err.println("[warn] " + message);
			cause.printStackTrace(System.err);
		}

		public void error(String message) {
			System.err.println("[error] " + message);
		}

		public void error(String message, Throwable cause) {
			System.err.println("[error] " + message);
			cause.printStackTrace(System.err);
		}

		public void debug(String message) {
			System.out.println("[debug] " + message);
		}
	}
}
