package com.newrelic.agent.compile;

import com.newrelic.agent.compile.visitor.ActivityClassVisitor;
import com.newrelic.agent.compile.visitor.AnnotatingClassVisitor;
import com.newrelic.agent.compile.visitor.AsyncTaskClassVisitor;
import com.newrelic.agent.compile.visitor.ContextInitializationClassVisitor;
import com.newrelic.agent.compile.visitor.NewRelicClassVisitor;
import com.newrelic.agent.compile.visitor.PrefilterClassVisitor;
import com.newrelic.agent.compile.visitor.TraceAnnotationClassVisitor;
import com.newrelic.agent.compile.visitor.WrapMethodClassVisitor;
import com.newrelic.agent.util.Streams;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class RewriterAgent {
    public static final String VERSION = "4.120.0";
    private static final Set<String> DX_COMMAND_NAMES = Collections.unmodifiableSet(new HashSet(Arrays.asList(new String[]{"dx", "dx.bat"})));

    private static final Set<String> JAVA_NAMES = Collections.unmodifiableSet(new HashSet(Arrays.asList(new String[]{"java", "java.exe"})));

    private static final Set<String> AGENT_JAR_NAMES = Collections.unmodifiableSet(new HashSet(Arrays.asList(new String[]{"newrelic.android.fat.jar", "newrelic.android.jar", "obfuscated.jar"})));
    private static final String DISABLE_INSTRUMENTATION_SYSTEM_PROPERTY = "newrelic.instrumentation.disabled";
    private static final String INVOCATION_DISPATCHER_FIELD_NAME = "treeLock";
    private static final Class INVOCATION_DISPATCHER_CLASS = Logger.class;
    private static final String SET_INSTRUMENTATION_DISABLED_FLAG = "SET_INSTRUMENTATION_DISABLED_FLAG";
    private static final String PRINT_TO_INFO_LOG = "PRINT_TO_INFO_LOG";
    private static final String DEXER_MAIN_CLASS_NAME = "com/android/dx/command/dexer/Main";
    private static final String ANT_DEX_EXEC_TASK = "com/android/ant/DexExecTask";
    private static final String ECLIPSE_BUILD_HELPER = "com/android/ide/eclipse/adt/internal/build/BuildHelper";
    private static final String MAVEN_DEX_MOJO = "com/jayway/maven/plugins/android/phase08preparepackage/DexMojo";
    private static final String PROCESS_BUILDER_CLASS_NAME = "java/lang/ProcessBuilder";
    private static final String PROCESS_CLASS_METHOD_NAME = "processClass";
    private static final String EXECUTE_DX_METHOD_NAME = "executeDx";
    private static final String PRE_DEX_LIBRARIES_METHOD_NAME = "preDexLibraries";
    private static final String START_METHOD_NAME = "start";
    private static String agentArgs;
    private static Map<String, String> agentOptions = Collections.emptyMap();

    private static final HashSet<String> EXCLUDED_PACKAGES = new HashSet() {
    };

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        premain(agentArgs, instrumentation);
    }

    public static String getVersion() {
        return VERSION;
    }

    public static Map<String, String> getAgentOptions() {
        return agentOptions;
    }

    /**
     * premain函数
     * @param agentArgs
     * @param instrumentation
     */
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        //agentArgs = agentArgs;

        Throwable argsError = null;
        try {
            agentOptions = parseAgentArgs(agentArgs);
        } catch (Throwable t) {
            argsError = t;
        }

        String logFileName = (String) agentOptions.get("logfile");

        Log log = logFileName == null ? new SystemErrLog(agentOptions) : new FileLogImpl(agentOptions, logFileName);
        if (argsError != null) {
            log.error("Agent args error: " + agentArgs, argsError);
        }
        log.debug("Bootstrapping New Relic Android class rewriter");


        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);
        log.debug("Agent running in pid " + pid + " arguments: " + agentArgs);
        try {
            NewRelicClassTransformer classTransformer;

            if (agentOptions.containsKey("deinstrument")) {
                log.info("Deinstrumenting...");
                //NoOpClassTransformer不执行任何操作的ClassTransformer
                classTransformer = new NoOpClassTransformer();
            } else {
                classTransformer = new DexClassTransformer(log);
                createInvocationDispatcher(log);
            }

            //注册classTransformer， 并设置canRetransform为true
            instrumentation.addTransformer(classTransformer, true);

            List classes = new ArrayList();
            for (Class clazz : instrumentation.getAllLoadedClasses()) {
                //如果class为 "com/android/dx/command/dexer/Main"||"com/android/ant/DexExecTask"||"com/android/ide/eclipse/adt/internal/build/BuildHelper"
                //"com/jayway/maven/plugins/android/phase08preparepackage/DexMojo"||"java/lang/ProcessBuilder" 则返回为true
                if (classTransformer.modifies(clazz)) {
                    classes.add(clazz);
                }
            }

            if (!classes.isEmpty()) {
                //TODO
                // Retransformation will only be supported if
                // the Can-Retransform-Classes manifest attribute is set to true in the agent JAR file
                if (instrumentation.isRetransformClassesSupported()) {
                    log.debug("Retransform classes: " + classes);
                    instrumentation.retransformClasses((Class[]) classes.toArray(new Class[classes.size()]));
                } else {
                    log.error("Unable to retransform classes: " + classes);
                }

            }

            if (!agentOptions.containsKey("deinstrument"))
                //???error
                redefineClass(instrumentation, classTransformer, ProcessBuilder.class);
        } catch (Throwable ex) {
            log.error("Agent startup error", ex);
            throw new RuntimeException(ex);
        }
    }


    /**
     * 重新定义class
     * @param instrumentation
     * @param classTransformer
     * @param klass
     * @throws IOException
     * @throws IllegalClassFormatException
     * @throws ClassNotFoundException
     * @throws UnmodifiableClassException
     */
    private static void redefineClass(Instrumentation instrumentation, ClassFileTransformer classTransformer, Class<?> klass)
            throws IOException, IllegalClassFormatException, ClassNotFoundException, UnmodifiableClassException {
        //processBuilder.class
        String internalClassName = klass.getName().replace('.', '/');
        String classPath = internalClassName + ".class";

        ClassLoader cl = klass.getClassLoader() == null ? RewriterAgent.class.getClassLoader() : klass.getClassLoader();
        InputStream stream = cl.getResourceAsStream(classPath);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Streams.copy(stream, output);

        stream.close();
        byte[] newBytes = classTransformer.transform(klass.getClassLoader(), internalClassName, klass, null, output.toByteArray());

        ClassDefinition def = new ClassDefinition(klass, newBytes);
        instrumentation.redefineClasses(new ClassDefinition[]{def});
    }

    private static Map<String, String> parseAgentArgs(String agentArgs) {
        if (agentArgs == null) {
            return Collections.emptyMap();
        }
        Map options = new HashMap();
        for (String arg : agentArgs.split(";")) {
            String[] keyValue = arg.split("=");
            if (keyValue.length == 2)
                options.put(keyValue[0], keyValue[1]);
            else {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
        }

        return options;
    }

    private static ClassAdapter createDexerMainClassAdapter(ClassVisitor cw,final Log log) {

        return new ClassAdapterBase(log, cw, new HashMap() {
            {

                put(new Method("processClass", "(Ljava/lang/String;[B)Z"), new MethodVisitorFactory()
                {
                    public MethodVisitor create(MethodVisitor mv, int access, String name, String desc)
                    {
                        return new RewriterAgent.BaseMethodVisitor(mv, access, name, desc)
                        {
                            protected void onMethodEnter() {
                                log.debug("Found onMethodEnter in processClass");

                                this.builder.loadInvocationDispatcher().loadInvocationDispatcherKey(RewriterAgent.getProxyInvocationKey(DEXER_MAIN_CLASS_NAME, this.methodName)).loadArgumentsArray(this.methodDesc).invokeDispatcher(false);
                                checkCast(Type.getType(Byte[].class));
                                storeArg(1);
                            }
                        };
                    }
                });
            }
        });
    }

    private static ClassAdapter createEclipseBuildHelperClassAdapter(ClassVisitor cw,final Log log) {

        return new ClassAdapterBase(log, cw,new HashMap<Method, MethodVisitorFactory>(){
            {
                put(new Method("executeDx", "(Lorg/eclipse/jdt/core/IJavaProject;Ljava/util/Collection;Ljava/lang/String;)V"), new MethodVisitorFactory()
                {
                    public MethodVisitor create(MethodVisitor mv, int access, String name, String desc)
                    {
                        return new RewriterAgent.SafeInstrumentationMethodVisitor(mv, access, name, desc)
                        {
                            protected void onMethodEnter()
                            {
                               log.debug("Found onMethodEnter in executeDx");

                                this.builder.loadInvocationDispatcher().loadInvocationDispatcherKey("SET_INSTRUMENTATION_DISABLED_FLAG").loadArray(new Runnable[] { new Runnable()
                                {
                                    public void run()
                                    {
                                        loadArg(0);
                                        visitLdcInsn("com.newrelic.agent.android.Agent");
                                        invokeInterface(Type.getObjectType("org/eclipse/jdt/core/IJavaProject"), new Method("findType", "(Ljava/lang/String;)Lorg/eclipse/jdt/core/IType;"));
                                    }
                                }
                                }).invokeDispatcher();
                            }
                        };
                    }
                });

            }
        });
    }

    private static ClassAdapter createAntTaskClassAdapter(ClassVisitor cw, Log log) {
        String agentFileFieldName = "NewRelicAgentFile";
        Map methodVisitors = new HashMap() {
            {
                put(new Method("preDexLibraries", "(Ljava/util/List;)V"), new MethodVisitorFactory() {

                            public MethodVisitor create(MethodVisitor mv, int access, String name, String desc)
                            {
                                return new BaseMethodVisitor(mv, access, name, desc) {

                                    protected void onMethodEnter()
                                    {
                                        builder.loadInvocationDispatcher().loadInvocationDispatcherKey(RewriterAgent.getProxyInvocationKey("com/android/ant/DexExecTask", methodName)).loadArray(new Runnable[] {
                                                new Runnable() {

                                                    public void run()
                                                    {
                                                        loadArg(0);
                                                    }
                                                }

                                        }).invokeDispatcher(false);
                                        loadThis();
                                        swap();
                                        putField(Type.getObjectType("com/android/ant/DexExecTask"), "NewRelicAgentFile", Type.getType(Object.class));
                                    }
                                };
                            }
                        }
                );
                put(new Method("runDx", "(Ljava/util/Collection;Ljava/lang/String;Z)V"), new MethodVisitorFactory() {

                            public MethodVisitor create(MethodVisitor mv, int access, String name, String desc)
                            {
                                return new SafeInstrumentationMethodVisitor(mv, access, name, desc) {

                                    protected void onMethodEnter()
                                    {
                                        builder.loadInvocationDispatcher().loadInvocationDispatcherKey("SET_INSTRUMENTATION_DISABLED_FLAG").loadArray(new Runnable[] {
                                                new Runnable() {

                                                    public void run()
                                                    {
                                                        loadThis();
                                                        getField(Type.getObjectType("com/android/ant/DexExecTask"), "NewRelicAgentFile", Type.getType(Object.class));
                                                    }
                                                }

                                        }).invokeDispatcher();
                                    }
                                };
                            }
                        }
                );
            }
        };
        return new ClassAdapterBase(log, cw, methodVisitors) {
            public void visitEnd() {
                super.visitEnd();
                visitField(2, "NewRelicAgentFile", Type.getType(Object.class).getDescriptor(), null, null);
            }
        };
    }

    private static ClassAdapter createProcessBuilderClassAdapter(ClassVisitor cw, Log log) {
        return new ClassAdapter(cw) {
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

                if ("start".equals(name)) {
                    mv = new SkipInstrumentedMethodsMethodVisitor(new RewriterAgent.BaseMethodVisitor(mv, access, name, desc) {
                        protected void onMethodEnter() {
                            this.builder.loadInvocationDispatcher().loadInvocationDispatcherKey(RewriterAgent.getProxyInvocationKey("java/lang/ProcessBuilder", this.methodName)).loadArray(new Runnable[]{new Runnable() {
                                public void run() {
                                    loadThis();
                                    invokeVirtual(Type.getObjectType("java/lang/ProcessBuilder"), new org.objectweb.asm.commons.Method("command", "()Ljava/util/List;"));
                                }
                            }
                            }).invokeDispatcher();
                        }

                    });
                }

                return mv;
            }
        };
    }

    private static ClassAdapter createMavenClassAdapter(ClassVisitor cw, Log log,final String agentJarPath) {
        Map methodVisitors = new HashMap() {
            {

                put(new Method("runDex", "(Lcom/jayway/maven/plugins/android/CommandExecutor;Ljava/io/File;Ljava/util/Set;)V"), new MethodVisitorFactory()
                {
                    public MethodVisitor create(MethodVisitor mv, int access, String name, String desc)
                    {
                        return new GeneratorAdapter(mv, access, name, desc)
                        {
                            public void visitMethodInsn(int opcode, String owner, String name, String desc)
                            {
                                if (("executeCommand".equals(name)) && ("(Ljava/lang/String;Ljava/util/List;Ljava/io/File;Z)V".equals(desc)))
                                {
                                    int arg3 = newLocal(Type.BOOLEAN_TYPE);
                                    storeLocal(arg3);
                                    int arg2 = newLocal(Type.getType(File.class));
                                    storeLocal(arg2);

                                    dup();

                                    push(0);

                                    String agentCommand = "-javaagent:" + agentJarPath;
                                    //RewriterAgent.access$800？？？猜是RewriterAgent.getVersion()
                                    if (RewriterAgent.getVersion()!= null) {
                                        agentCommand = agentCommand + "=" + RewriterAgent.getVersion();
                                    }

                                    new RewriterAgent.BytecodeBuilder(this).printToInfoLogFromBytecode("Maven agent jar: " + agentCommand);

                                    visitLdcInsn(agentCommand);
                                    invokeInterface(Type.getType(List.class), new Method("add", "(ILjava/lang/Object;)V"));

                                    loadLocal(arg2);
                                    loadLocal(arg3);
                                }
                                super.visitMethodInsn(opcode, owner, name, desc);
                            }
                        };
                    }
                });
            }
        };
        return new ClassAdapterBase(log, cw, methodVisitors);
    }

    private static String getAgentJarPath()
            throws URISyntaxException {
        return new File(RewriterAgent.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getAbsolutePath();
    }

    private static void createInvocationDispatcher(Log log)
            throws Exception {
        //获取Logger字段treeLock
        ///The fields relating to parent-child relationships and levels
        // are managed under a separate lock, the treeLock.
        Field field = INVOCATION_DISPATCHER_CLASS.getDeclaredField("treeLock");
        //修改为允许外部访问
        field.setAccessible(true);


        Field modifiersField = Field.class.getDeclaredField("modifiers");
        //修改Field的modifiers方法为允许外部访问
        modifiersField.setAccessible(true);
        //修改获取Logger字段treeLock，删除final属性(FINAL=16）
        modifiersField.setInt(field, field.getModifiers() & 0xFFFFFFEF);

        if ((field.get(null) instanceof InvocationDispatcher))  // 判断Logger类的静态变量treeLock是否是InvocationDispatcher的实例。
            log.info("Detected cached instrumentation.");
        else
            // 设置Logger类的静态变量treeLock为InvocationDispatcher实例
            field.set(null, new InvocationDispatcher(log));
    }

    private static String getProxyInvocationKey(String className, String methodName) {
        return className + "." + methodName;
    }

    private static class InvocationDispatcher
            implements InvocationHandler {
        private final Log log;
        private final ClassRemapperConfig config;
        private final  InstrumentationContext context;
        private final Map<String, InvocationHandler> invocationHandlers;
        private boolean writeDisabledMessage = true;
        private final String agentJarPath;
        private boolean disableInstrumentation = false;

        public InvocationDispatcher(final Log log)
                throws FileNotFoundException, IOException, ClassNotFoundException, URISyntaxException {
            this.log = log;
            this.config = new ClassRemapperConfig(log);
            this.context = new InstrumentationContext(this.config, log);
            this.agentJarPath = RewriterAgent.getAgentJarPath();
            this.invocationHandlers = Collections.unmodifiableMap(new HashMap() {
                {
                    //access$700 → getProxyInvocationKey
                    put(RewriterAgent.getProxyInvocationKey("com/android/dx/command/dexer/Main", "processClass"), new InvocationHandler() {
                        public Object invoke(Object proxy, Method method, Object[] args)
                                throws Throwable {
                            byte[] bytes = (byte[]) args[1];

                            //access$1100→isInstrumentationDisabled()
                            //invokestatic com.newrelic.agent.compile.RewriterAgent$InvocationDispatcher.access$1100(com.newrelic.agent.compile.RewriterAgent$InvocationDispatcher) : boolean [39]
                            if (isInstrumentationDisabled()) {
                                // invokestatic com.newrelic.agent.compile.RewriterAgent$InvocationDispatcher.access$1200(com.newrelic.agent.compile.RewriterAgent$InvocationDispatcher) : boolean [42]
                                if (writeDisabledMessage) {
                                    // invokestatic com.newrelic.agent.compile.RewriterAgent$InvocationDispatcher.access$1202(com.newrelic.agent.compile.RewriterAgent$InvocationDispatcher, boolean) : boolean [46]
                                    //new ClassData(bytes, false);
                                    writeDisabledMessage = false;
                                    log.info("Instrumentation disabled, no agent present");
                                }
                                return bytes;
                            }
                            //RewriterAgent.InvocationDispatcher.access$1202(RewriterAgent.InvocationDispatcher.1.this.this$0, true);→ new ClassData(bytes,false);
                            //new ClassData(bytes, true);
                            writeDisabledMessage = true;
                            //RewriterAgent.InvocationDispatcher.access$1300→(this.invoke(proxy,method,args)
                            synchronized context {
                                //RewriterAgent.InvocationDispatcher.access$1400→RewriterAgent.InvocationDispatcher.visitClassBytes(bytes);
                                //ClassData classData = InvocationDispatcher.visitClassBytes(bytes);
                                ClassData classData = visitClassBytes(bytes);

                                if ((classData != null) && (classData.getMainClassBytes() != null) && (classData.isModified())) {
                                    return classData.getMainClassBytes();
                                }
                            }

                            return bytes;
                        }
                    });
                    put(RewriterAgent.getProxyInvocationKey("com/android/dx/command/dexer/Main", "processClass"), new InvocationHandler() {
                                public Object invoke(Object proxy, Method method, Object[] args)
                                        throws Throwable {
                                    byte[] bytes = (byte[]) args[1];

                                    //access$1100→isInstrumentationDisabled()
                                    //invokestatic com.newrelic.agent.compile.RewriterAgent$InvocationDispatcher.access$1100(com.newrelic.agent.compile.RewriterAgent$InvocationDispatcher) : boolean [39]
                                    if (InvocationDispatcher.isInstrumentationDisabled()) {
                                        if (isExcludedPackage(agentJarPath)) {
                                            //??
                                            new ClassData(bytes, false);
                                            log.info("Instrumentation disabled, no agent present");
                                        }
                                        return bytes;
                                    }
                                    //RewriterAgent.InvocationDispatcher.access$1202(RewriterAgent.InvocationDispatcher.1.this.this$0, true);→ new ClassData(bytes,false);
                                    //??
                                    new ClassData(bytes, true);
                                    //RewriterAgent.InvocationDispatcher.access$1300→(this.invoke(proxy,method,args)
                                    synchronized (this.invoke(proxy, method, args)) {
                                        //RewriterAgent.InvocationDispatcher.access$1400→RewriterAgent.InvocationDispatcher.visitClassBytes(bytes);
                                        ClassData classData = InvocationDispatcher.visitClassBytes(bytes);

                                        if ((classData != null) && (classData.getMainClassBytes() != null) && (classData.isModified())) {
                                            return classData.getMainClassBytes();
                                        }
                                    }

                                    return bytes;
                                }
                            }
                    );
                    put(RewriterAgent.getProxyInvocationKey("com/android/ant/DexExecTask", "preDexLibraries"), new InvocationHandler()
                    {
                        public Object invoke(Object proxy, Method method, Object[] args)
                                throws Throwable
                        {
                            List files = (List)args[0];
                            for (File file : files) {
                                //RewriterAgent.access$1500().contains(file.getName().toLowerCase())→RewriterAgent.getAgentOptions().containsKey
                                if (RewriterAgent.getAgentOptions().containsKey(file.getName().toLowerCase())) {
                                    log.info("Detected the New Relic Android agent in an Ant build (" + file.getPath() + ")");
                                    return file;
                                }
                            }
                            log.debug("Ant preDexLibraries: " + files);
                            log.info("No New Relic agent detected in Ant build");
                            return null;
                        }
                    });
                    put("SET_INSTRUMENTATION_DISABLED_FLAG", new InvocationHandler()
                    {
                        public Object invoke(Object proxy, Method method, Object[] args)
                                throws Throwable
                        {
                            RewriterAgent.InvocationDispatcher.access$1602(RewriterAgent.InvocationDispatcher.1.this.this$0, (args != null) && (args[0] == null));

                            RewriterAgent.InvocationDispatcher.1.this.val$log.debug("DisableInstrumentation: " + RewriterAgent.InvocationDispatcher.access$1600(RewriterAgent.InvocationDispatcher.1.this.this$0) + " (" + args + ")");
                            return null;
                        }
                    });
                    put("PRINT_TO_INFO_LOG", new InvocationHandler()
                    {
                        public Object invoke(Object proxy, Method method, Object[] args)
                                throws Throwable
                        {
                           log.info(args[0].toString());
                            return null;
                        }
                    });
                    put(RewriterAgent.getProxyInvocationKey("java/lang/ProcessBuilder", "start"), new InvocationHandler()
                    {
                        public Object invoke(Object proxy, Method method, Object[] args)
                                throws Throwable
                        {
                            List list = (List)args[0];

                            String command = (String)list.get(0);

                            File commandFile = new File(command);

                            if (RewriterAgent.InvocationDispatcher.isInstrumentationDisabled()) {
                            log.info("Instrumentation disabled, no agent present.  Command: " + commandFile.getName());
                            log.debug("Execute: " + list.toString());
                            return null;
                        }

                            String javaagentString = null;
                            if (RewriterAgent.getAgentOptions().containsKey(commandFile.getName().toLowerCase()))
                                //
                                javaagentString = "-Jjavaagent:" + RewriterAgent.InvocationDispatcher.access$1800(RewriterAgent.InvocationDispatcher.1.this.this$0);
                            else if (RewriterAgent.access$1900().contains(commandFile.getName().toLowerCase())) {
                                javaagentString = "-javaagent:" + RewriterAgent.InvocationDispatcher.access$1800(RewriterAgent.InvocationDispatcher.1.this.this$0);
                        }

                            if (javaagentString != null) {
                                //RewriterAgent.access$800()→RewriterAgent.getAgentJarPath()
                                if (RewriterAgent.getAgentJarPath()!= null) {
                                    javaagentString = javaagentString + "=" + RewriterAgent.getAgentJarPath();
                                }
                                list.add(1, quoteProperty(javaagentString));
                            }

                            log.debug("Execute: " + list.toString());
                            return null;
                        }

                        private String quoteProperty(String string) {
                            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                                return "\"" + string + "\"";
                            }
                            return string;
                        }
                    });
                }

            });
        }

        private static boolean isInstrumentationDisabled() {
            return (this.disableInstrumentation) || (System.getProperty("newrelic.instrumentation.disabled") != null);
        }

        private boolean isExcludedPackage(String packageName) {
            for (String name : RewriterAgent.EXCLUDED_PACKAGES) {
                if (packageName.contains(name)) {
                    return true;
                }
            }

            return false;
        }

        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args)
                throws Throwable {
            InvocationHandler handler = (InvocationHandler) this.invocationHandlers.get(proxy);
            if (handler == null) {
                this.log.error("Unknown invocation type: " + proxy + ".  Arguments: " + Arrays.asList(args));
                return null;
            }
            try {
                return handler.invoke(proxy, method, args);
            } catch (Throwable t) {
                this.log.error("Error:" + t.getMessage(), t);
            }
            return null;
        }

        private  ClassData visitClassBytes(byte[] bytes) {
            String className = "an unknown class";
            try {
                ClassReader cr = new ClassReader(bytes);
                ClassWriter cw = new ClassWriter(cr, 1);

                context.reset();

                cr.accept(new PrefilterClassVisitor(this.context, this.log), 7);

                className = this.context.getClassName();

                if (!this.context.hasTag("Lcom/newrelic/agent/android/instrumentation/Instrumented;")) {
                    ClassVisitor cv = cw;

                    if (this.context.getClassName().startsWith("com/newrelic/agent/android")) {
                        cv = new NewRelicClassVisitor(cv, this.context, this.log);
                    } else if (this.context.getClassName().startsWith("android/support/")) {
                        cv = new ActivityClassVisitor(cv, this.context, this.log);
                    } else {
                        if (isExcludedPackage(this.context.getClassName())) {
                            return null;
                        }
                        cv = new AnnotatingClassVisitor(cv, this.context, this.log);
                        cv = new ActivityClassVisitor(cv, this.context, this.log);
                        cv = new AsyncTaskClassVisitor(cv, this.context, this.log);
                        cv = new TraceAnnotationClassVisitor(cv, this.context, this.log);
                        cv = new WrapMethodClassVisitor(cv, this.context, this.log);
                    }
                    cv = new ContextInitializationClassVisitor(cv, this.context);
                    cr.accept(cv, 12);
                } else {
                    this.log.warning(MessageFormat.format("[{0}] class is already instrumented! skipping ...", new Object[]{this.context.getFriendlyClassName()}));
                }

                return this.context.newClassData(cw.toByteArray());
            } catch (SkipException ex) {
                return null;
            } catch (HaltBuildException e) {
                throw new RuntimeException(e);
            } catch (Throwable t) {
                this.log.error("Unfortunately, an error has occurred while processing " + className + ". Please copy your build logs and the jar containing this class and send a message to support@newrelic.com, thanks!\n" + t.getMessage(), t);
            }
            return new ClassData(bytes, false);
        }
    }





    private static abstract class BaseMethodVisitor extends AdviceAdapter {
        protected final String methodName;
        protected final RewriterAgent.BytecodeBuilder builder = new RewriterAgent.BytecodeBuilder(this);

        protected BaseMethodVisitor(MethodVisitor mv, int access, String methodName, String desc) {
            super(mv, access, methodName, desc);
            this.methodName = methodName;
        }

        public void visitEnd() {
            super.visitAnnotation(Type.getDescriptor(InstrumentedMethod.class), false);
            super.visitEnd();
        }
    }

    private static class BytecodeBuilder {
        private final GeneratorAdapter mv;

        public BytecodeBuilder(GeneratorAdapter adapter) {
            this.mv = adapter;
        }

        public BytecodeBuilder loadNull() {
            this.mv.visitInsn(1);
            return this;
        }

        public BytecodeBuilder loadInvocationDispatcher() {
            this.mv.visitLdcInsn(Type.getType(RewriterAgent.INVOCATION_DISPATCHER_CLASS));
            this.mv.visitLdcInsn("treeLock");
            this.mv.invokeVirtual(Type.getType(Class.class), new org.objectweb.asm.commons.Method("getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;"));

            this.mv.dup();
            this.mv.visitInsn(4);
            this.mv.invokeVirtual(Type.getType(Field.class), new org.objectweb.asm.commons.Method("setAccessible", "(Z)V"));

            this.mv.visitInsn(1);

            this.mv.invokeVirtual(Type.getType(Field.class), new org.objectweb.asm.commons.Method("get", "(Ljava/lang/Object;)Ljava/lang/Object;"));

            return this;
        }

        public BytecodeBuilder loadArgumentsArray(String methodDesc) {
            org.objectweb.asm.commons.Method method = new org.objectweb.asm.commons.Method("dummy", methodDesc);
            this.mv.push(method.getArgumentTypes().length);
            Type objectType = Type.getType(Object.class);
            this.mv.newArray(objectType);

            for (int i = 0; i < method.getArgumentTypes().length; i++) {
                this.mv.dup();
                this.mv.push(i);
                this.mv.loadArg(i);
                this.mv.arrayStore(objectType);
            }
            return this;
        }

        public BytecodeBuilder loadArray(Runnable[] r) {
            this.mv.push(r.length);
            Type objectType = Type.getObjectType("java/lang/Object");
            this.mv.newArray(objectType);

            for (int i = 0; i < r.length; i++) {
                this.mv.dup();
                this.mv.push(i);
                r[i].run();
                this.mv.arrayStore(objectType);
            }

            return this;
        }

        public BytecodeBuilder printToInfoLogFromBytecode(final String message) {
            loadInvocationDispatcher();

            this.mv.visitLdcInsn("PRINT_TO_INFO_LOG");
            this.mv.visitInsn(1);

            loadArray(new Runnable[]{new Runnable() {
                public void run() {
                    RewriterAgent.BytecodeBuilder.this.mv.visitLdcInsn(message);
                }
            }
            });
            invokeDispatcher();
            return this;
        }

        public BytecodeBuilder invokeDispatcher() {
            return invokeDispatcher(true);
        }

        public BytecodeBuilder invokeDispatcher(boolean popReturnOffStack) {
            this.mv.invokeInterface(Type.getType(InvocationHandler.class), new org.objectweb.asm.commons.Method("invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;"));
            if (popReturnOffStack) {
                this.mv.pop();
            }
            return this;
        }

        public BytecodeBuilder loadInvocationDispatcherKey(String key) {
            this.mv.visitLdcInsn(key);

            this.mv.visitInsn(1);

            return this;
        }
    }

    private static abstract class SafeInstrumentationMethodVisitor extends RewriterAgent.BaseMethodVisitor {
        protected SafeInstrumentationMethodVisitor(MethodVisitor mv, int access, String methodName, String desc) {
            super(mv, access, methodName, desc);
        }

        protected final void onMethodExit(int opcode) {
            this.builder.loadInvocationDispatcher().loadInvocationDispatcherKey("SET_INSTRUMENTATION_DISABLED_FLAG").loadNull().invokeDispatcher();

            super.onMethodExit(opcode);
        }
    }

    private static final class DexClassTransformer
            implements RewriterAgent.NewRelicClassTransformer {
        private Log log;
        private final Map<String, ClassVisitorFactory> classVisitors;

        public DexClassTransformer(final Log log)
                throws URISyntaxException {
            final String agentJarPath;
            try {
                agentJarPath = RewriterAgent.getAgentJarPath();
            } catch (URISyntaxException e) {
                log.error("Unable to get the path to the New Relic class rewriter jar", e);
                throw e;
            }

            this.log = log;

            this.classVisitors = new HashMap() {
                {
                    put(DEXER_MAIN_CLASS_NAME, new ClassVisitorFactory(true) {
                        public ClassAdapter create(ClassVisitor cv) {
                            return RewriterAgent.createDexerMainClassAdapter(cv,log);
                        }
                    });
                    put(ANT_DEX_EXEC_TASK, new ClassVisitorFactory(false) {
                        public ClassAdapter create(ClassVisitor cv) {
                            return RewriterAgent.createAntTaskClassAdapter(cv, log);
                        }
                    });
                    put(ECLIPSE_BUILD_HELPER, new ClassVisitorFactory(true) {
                        public ClassAdapter create(ClassVisitor cv) {
                            return RewriterAgent.createEclipseBuildHelperClassAdapter(cv, log);
                        }
                    });
                    put(MAVEN_DEX_MOJO, new ClassVisitorFactory(true) {
                        public ClassAdapter create(ClassVisitor cv) {
                            return RewriterAgent.createMavenClassAdapter(cv,log,agentJarPath);
                        }
                    });
                    put(PROCESS_BUILDER_CLASS_NAME, new ClassVisitorFactory(true) {
                        public ClassAdapter create(ClassVisitor cv) {
                            return RewriterAgent.createProcessBuilderClassAdapter(cv,log);
                        }
                    });
                }
            };
        }

        public boolean modifies(Class<?> clazz) {
            Type t = Type.getType(clazz);
            //返回classVisitors中是否含有包含这个类内部名为key的数据
            //是否包含 "com/android/dx/command/dexer/Main"||"com/android/ant/DexExecTask"||"com/android/ide/eclipse/adt/internal/build/BuildHelper"
            //"com/jayway/maven/plugins/android/phase08preparepackage/DexMojo"||"java/lang/ProcessBuilder"
            return this.classVisitors.containsKey(t.getInternalName());
        }

        public byte[] transform(ClassLoader classLoader, String className, Class<?> clazz, ProtectionDomain protectionDomain, byte[] bytes)
                throws IllegalClassFormatException {
            ClassVisitorFactory factory = (ClassVisitorFactory) this.classVisitors.get(className);
            if (factory != null) {
                if ((clazz != null) && (!factory.isRetransformOkay())) {
                    this.log.error("Cannot instrument " + className);
                    return null;
                }
                this.log.debug("Patching " + className);
                try {
                    ClassReader cr = new ClassReader(bytes);
                    ClassWriter cw = new PatchedClassWriter(3, classLoader);

                    ClassAdapter adapter = factory.create(cw);
                    cr.accept(adapter, 4);

                    return cw.toByteArray();
                } catch (SkipException ex) {
                } catch (Exception ex) {
                    this.log.error("Error transforming class " + className, ex);
                }
            }

            return null;
        }
    }


    private static final class NoOpClassTransformer
            implements RewriterAgent.NewRelicClassTransformer {
        private static HashSet<String> classVisitors = new HashSet() {
            {
                add("com/android/dx/command/dexer/Main");
                add("com/android/ant/DexExecTask");
                add("com/android/ide/eclipse/adt/internal/build/BuildHelper");
                add("com/jayway/maven/plugins/android/phase08preparepackage/DexMojo");
                add("java/lang/ProcessBuilder");
            }
        };

        public byte[] transform(ClassLoader classLoader, String s, Class<?> aClass, ProtectionDomain protectionDomain, byte[] bytes)
                throws IllegalClassFormatException {
            return null;
        }

        public boolean modifies(Class<?> clazz) {
            Type t = Type.getType(clazz);
            return classVisitors.contains(t.getInternalName());
        }
    }

    private static abstract interface NewRelicClassTransformer extends ClassFileTransformer {
        public abstract boolean modifies(Class<?> paramClass);
    }
}

