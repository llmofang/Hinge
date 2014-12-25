// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   NewRelicGradlePlugin.groovy

package com.newrelic.agent.android;

import groovy.lang.*;
import java.lang.ref.SoftReference;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.runtime.*;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.callsite.CallSiteArray;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

public class NewRelicGradlePlugin implements Plugin
{


    public void apply(Project project1)
    {
//        project1;
//        JVM INSTR new #41  <Class Reference>;
//        JVM INSTR dup_x1 ;
//        JVM INSTR swap ;
//        Reference();
//        Reference project;
//        project;
//        CallSite acallsite[] = $getCallSiteArray();
    	logger=project1.getLogger();
    	
    	logger.info("[newrelic] New Relic plugin loaded.");
    	project1.apply(arg0);
    	
        class _apply_closure1 extends Closure
            implements GeneratedClosure
        {

            public Object doCall(Object it)
            {
                if(DefaultTypeTransformation.booleanUnbox(acallsite1[0].call(it, "android")))
                {
                	project1.getTasks().create("newRelicInstrumentTask", NewRelicInstrumentTask.Class);
                	project1.getTasks().create("newRelicDeinstrumentTask", NewRelicDeinstrumentTask().Class);
                    project1.apply(_apply_closure1_closure2);
                    class _apply_closure1_closure2 extends Closure
                        implements GeneratedClosure
                    {

                        public Object doCall(Object variant)
                        {
                            CallSite acallsite2[] = $getCallSiteArray();
                            
                            acallsite2[0].call(acallsite2[1].callGetProperty(variant), "newRelicInstrumentTask");
                            acallsite2[2].call(acallsite2[3].callGetProperty(variant), "newRelicDeinstrumentTask");
                            return acallsite2[4].call(acallsite2[5].callGroovyObjectGetProperty(this), acallsite2[6].call(acallsite2[7].call("[newrelic] Added instrumentation tasks to ", acallsite2[8].callGetProperty(variant)), " variant."));
                        }

                        protected MetaClass $getStaticMetaClass()
                        {
                            if(getClass() != $get$$class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1_closure2())
                                return ScriptBytecodeAdapter.initMetaClass(this);
                            ClassInfo classinfo = $staticClassInfo;
                            if(classinfo == null)
                                $staticClassInfo = classinfo = ClassInfo.getClassInfo(getClass());
                            return classinfo.getMetaClass();
                        }

                        public static void __$swapInit()
                        {
                            CallSite acallsite2[] = $getCallSiteArray();
                            $callSiteArray = null;
                        }

                        private static void $createCallSiteArray_1(String as[])
                        {
                            as[0] = "dependsOn";
                            as[1] = "dex";
                            as[2] = "finalizedBy";
                            as[3] = "dex";
                            as[4] = "info";
                            as[5] = "logger";
                            as[6] = "plus";
                            as[7] = "plus";
                            as[8] = "name";
                        }

                        private static CallSiteArray $createCallSiteArray()
                        {
                            String as[] = new String[9];
                            $createCallSiteArray_1(as);
                            return new CallSiteArray($get$$class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1_closure2(), as);
                        }

                        private static CallSite[] $getCallSiteArray()
                        {
                            CallSiteArray callsitearray;
                            if($callSiteArray == null || (callsitearray = (CallSiteArray)$callSiteArray.get()) == null)
                            {
                                callsitearray = $createCallSiteArray();
                                $callSiteArray = new SoftReference(callsitearray);
                            }
                            return callsitearray.array;
                        }

                        private static Class $get$$class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1_closure2()
                        {
                            return $class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1_closure2 == null && ($class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1_closure2 = _mthclass$("com.newrelic.agent.android.NewRelicGradlePlugin$_apply_closure1_closure2")) == null ? $class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1_closure2 : $class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1_closure2;
                        }

                        static Class _mthclass$(String s)
                        {
                            try
                            {
                                return Class.forName(s);
                            }
                            catch(ClassNotFoundException classnotfoundexception)
                            {
                                throw new NoClassDefFoundError(classnotfoundexception.getMessage());
                            }
                        }

                        private static ClassInfo $staticClassInfo;
                        public static transient boolean __$stMC;
                        private static SoftReference $callSiteArray;
                        private static Class $class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1_closure2;

                        static 
                        {
                            __$swapInit();
                        }

                    public _apply_closure1_closure2(Object _outerInstance, Object _thisObject)
                    {
                        CallSite acallsite[] = $getCallSiteArray();
                        super(_outerInstance, _thisObject);
                    }
                    }

                    acallsite1[3].call(acallsite1[4].callGetProperty(acallsite1[5].callGroovyObjectGetProperty(this)), new _apply_closure1_closure2(this, getThisObject()));
                    class _apply_closure1_closure3 extends Closure
                        implements GeneratedClosure
                    {

                        public Object doCall(Object variant)
                        {
                            CallSite acallsite2[] = $getCallSiteArray();
                            acallsite2[0].call(acallsite2[1].callGetProperty(variant), "newRelicInstrumentTask");
                            acallsite2[2].call(acallsite2[3].callGetProperty(variant), "newRelicDeinstrumentTask");
                            return acallsite2[4].call(acallsite2[5].callGroovyObjectGetProperty(this), acallsite2[6].call(acallsite2[7].call("[newrelic] Added instrumentation tasks to ", acallsite2[8].callGetProperty(variant)), " variant."));
                        }

                        protected MetaClass $getStaticMetaClass()
                        {
                            if(getClass() != $get$$class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1_closure3())
                                return ScriptBytecodeAdapter.initMetaClass(this);
                            ClassInfo classinfo = $staticClassInfo;
                            if(classinfo == null)
                                $staticClassInfo = classinfo = ClassInfo.getClassInfo(getClass());
                            return classinfo.getMetaClass();
                        }

                        public static void __$swapInit()
                        {
                            CallSite acallsite2[] = $getCallSiteArray();
                            $callSiteArray = null;
                        }

                        private static void $createCallSiteArray_1(String as[])
                        {
                            as[0] = "dependsOn";
                            as[1] = "dex";
                            as[2] = "finalizedBy";
                            as[3] = "dex";
                            as[4] = "info";
                            as[5] = "logger";
                            as[6] = "plus";
                            as[7] = "plus";
                            as[8] = "name";
                        }

                        private static CallSiteArray $createCallSiteArray()
                        {
                            String as[] = new String[9];
                            $createCallSiteArray_1(as);
                            return new CallSiteArray($get$$class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1_closure3(), as);
                        }

                        private static CallSite[] $getCallSiteArray()
                        {
                            CallSiteArray callsitearray;
                            if($callSiteArray == null || (callsitearray = (CallSiteArray)$callSiteArray.get()) == null)
                            {
                                callsitearray = $createCallSiteArray();
                                $callSiteArray = new SoftReference(callsitearray);
                            }
                            return callsitearray.array;
                        }

                        private static Class $get$$class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1_closure3()
                        {
                            return $class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1_closure3 == null && ($class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1_closure3 = _mthclass$("com.newrelic.agent.android.NewRelicGradlePlugin$_apply_closure1_closure3")) == null ? $class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1_closure3 : $class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1_closure3;
                        }

                        static Class _mthclass$(String s)
                        {
                            try
                            {
                                return Class.forName(s);
                            }
                            catch(ClassNotFoundException classnotfoundexception)
                            {
                                throw new NoClassDefFoundError(classnotfoundexception.getMessage());
                            }
                        }

                        private static ClassInfo $staticClassInfo;
                        public static transient boolean __$stMC;
                        private static SoftReference $callSiteArray;
                        private static Class $class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1_closure3;

                        static 
                        {
                            __$swapInit();
                        }

                    public _apply_closure1_closure3(Object _outerInstance, Object _thisObject)
                    {
                        CallSite acallsite[] = $getCallSiteArray();
                        super(_outerInstance, _thisObject);
                    }
                    }

                    return acallsite1[6].call(acallsite1[7].callGetProperty(acallsite1[8].callGroovyObjectGetProperty(this)), new _apply_closure1_closure3(this, getThisObject()));
                } else
                {
                    return null;
                }
            }

            public Project getProject()
            {
                CallSite acallsite1[] = $getCallSiteArray();
                return (Project)ScriptBytecodeAdapter.castToType(project.get(), $get$$class$org$gradle$api$Project());
            }

            public Object doCall()
            {
                CallSite acallsite1[] = $getCallSiteArray();
                return acallsite1[9].callCurrent(this, ScriptBytecodeAdapter.createPojoWrapper(null, $get$$class$java$lang$Object()));
            }

            protected MetaClass $getStaticMetaClass()
            {
                if(getClass() != $get$$class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1())
                    return ScriptBytecodeAdapter.initMetaClass(this);
                ClassInfo classinfo = $staticClassInfo;
                if(classinfo == null)
                    $staticClassInfo = classinfo = ClassInfo.getClassInfo(getClass());
                return classinfo.getMetaClass();
            }

            public static void __$swapInit()
            {
                CallSite acallsite1[] = $getCallSiteArray();
                $callSiteArray = null;
            }

            private static void $createCallSiteArray_1(String as[])
            {
                as[0] = "hasProperty";
                as[1] = "task";
                as[2] = "task";
                as[3] = "all";
                as[4] = "applicationVariants";
                as[5] = "android";
                as[6] = "all";
                as[7] = "testVariants";
                as[8] = "android";
                as[9] = "doCall";
            }

            private static CallSiteArray $createCallSiteArray()
            {
                String as[] = new String[10];
                $createCallSiteArray_1(as);
                return new CallSiteArray($get$$class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1(), as);
            }

            private static CallSite[] $getCallSiteArray()
            {
                CallSiteArray callsitearray;
                if($callSiteArray == null || (callsitearray = (CallSiteArray)$callSiteArray.get()) == null)
                {
                    callsitearray = $createCallSiteArray();
                    $callSiteArray = new SoftReference(callsitearray);
                }
                return callsitearray.array;
            }

            private static Class $get$$class$org$gradle$api$Project()
            {
                return $class$org$gradle$api$Project == null && ($class$org$gradle$api$Project = _mthclass$("org.gradle.api.Project")) == null ? $class$org$gradle$api$Project : $class$org$gradle$api$Project;
            }

            private static Class $get$$class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1()
            {
                return $class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1 == null && ($class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1 = _mthclass$("com.newrelic.agent.android.NewRelicGradlePlugin$_apply_closure1")) == null ? $class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1 : $class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1;
            }

            private static Class $get$$class$java$lang$Object()
            {
                return $class$java$lang$Object == null && ($class$java$lang$Object = _mthclass$("java.lang.Object")) == null ? $class$java$lang$Object : $class$java$lang$Object;
            }

            private static Class $get$$class$com$newrelic$agent$android$NewRelicDeinstrumentTask()
            {
                return $class$com$newrelic$agent$android$NewRelicDeinstrumentTask == null && ($class$com$newrelic$agent$android$NewRelicDeinstrumentTask = _mthclass$("com.newrelic.agent.android.NewRelicDeinstrumentTask")) == null ? $class$com$newrelic$agent$android$NewRelicDeinstrumentTask : $class$com$newrelic$agent$android$NewRelicDeinstrumentTask;
            }

            private static Class $get$$class$com$newrelic$agent$android$NewRelicInstrumentTask()
            {
                return $class$com$newrelic$agent$android$NewRelicInstrumentTask == null && ($class$com$newrelic$agent$android$NewRelicInstrumentTask = _mthclass$("com.newrelic.agent.android.NewRelicInstrumentTask")) == null ? $class$com$newrelic$agent$android$NewRelicInstrumentTask : $class$com$newrelic$agent$android$NewRelicInstrumentTask;
            }

            static Class _mthclass$(String s)
            {
                try
                {
                    return Class.forName(s);
                }
                catch(ClassNotFoundException classnotfoundexception)
                {
                    throw new NoClassDefFoundError(classnotfoundexception.getMessage());
                }
            }

            private Reference project;
            private static ClassInfo $staticClassInfo;
            public static transient boolean __$stMC;
            private static SoftReference $callSiteArray;
            private static Class $class$org$gradle$api$Project;
            private static Class $class$com$newrelic$agent$android$NewRelicGradlePlugin$_apply_closure1;
            private static Class $class$java$lang$Object;
            private static Class $class$com$newrelic$agent$android$NewRelicDeinstrumentTask;
            private static Class $class$com$newrelic$agent$android$NewRelicInstrumentTask;

            static 
            {
                __$swapInit();
            }

            public _apply_closure1(Object _outerInstance, Object _thisObject, Reference project)
            {
                CallSite acallsite[] = $getCallSiteArray();
                super(_outerInstance, _thisObject);
                Reference reference = project;
                this.project = reference;
                Reference _tmp = reference;
            }
        }

        acallsite[2].call((Project)project.get(), (Project)project.get(), new _apply_closure1(this, this, project));
        return;
    }

    public static Logger getLogger()
    {
        CallSite acallsite[] = $getCallSiteArray();
        return (Logger)logger;
    }

    public Object this$dist$invoke$1(String name, Object args)
    {
        CallSite acallsite[] = $getCallSiteArray();
        return ScriptBytecodeAdapter.invokeMethodOnCurrentN($get$$class$com$newrelic$agent$android$NewRelicGradlePlugin(), this, (String)ScriptBytecodeAdapter.castToType(new GStringImpl(new Object[] {
            name
        }, new String[] {
            "", ""
        }), $get$$class$java$lang$String()), ScriptBytecodeAdapter.despreadList(new Object[0], new Object[] {
            args
        }, new int[] {
            0
        }));
    }

    public void this$dist$set$1(String name, Object value)
    {
        CallSite acallsite[] = $getCallSiteArray();
        Object obj = value;
        ScriptBytecodeAdapter.setGroovyObjectField(obj, $get$$class$com$newrelic$agent$android$NewRelicGradlePlugin(), this, (String)ScriptBytecodeAdapter.castToType(new GStringImpl(new Object[] {
            name
        }, new String[] {
            "", ""
        }), $get$$class$java$lang$String()));
        Object _tmp = obj;
    }

    public Object this$dist$get$1(String name)
    {
        CallSite acallsite[] = $getCallSiteArray();
        return ScriptBytecodeAdapter.getGroovyObjectField($get$$class$com$newrelic$agent$android$NewRelicGradlePlugin(), this, (String)ScriptBytecodeAdapter.castToType(new GStringImpl(new Object[] {
            name
        }, new String[] {
            "", ""
        }), $get$$class$java$lang$String()));
    }

    protected MetaClass $getStaticMetaClass()
    {
        if(getClass() != $get$$class$com$newrelic$agent$android$NewRelicGradlePlugin())
            return ScriptBytecodeAdapter.initMetaClass(this);
        ClassInfo classinfo = $staticClassInfo;
        if(classinfo == null)
            $staticClassInfo = classinfo = ClassInfo.getClassInfo(getClass());
        return classinfo.getMetaClass();
    }

    public MetaClass getMetaClass()
    {
        metaClass;
        if(metaClass == null) goto _L2; else goto _L1
_L1:
        return;
_L2:
        JVM INSTR pop ;
        metaClass = $getStaticMetaClass();
        return metaClass;
    }

    public void setMetaClass(MetaClass metaclass)
    {
        metaClass = metaclass;
    }

    public Object invokeMethod(String s, Object obj)
    {
        return getMetaClass().invokeMethod(this, s, obj);
    }

    public Object getProperty(String s)
    {
        return getMetaClass().getProperty(this, s);
    }

    public void setProperty(String s, Object obj)
    {
        getMetaClass().setProperty(this, s, obj);
    }

    public static void __$swapInit()
    {
        CallSite acallsite[] = $getCallSiteArray();
        $callSiteArray = null;
    }

    public volatile void apply(Object obj)
    {
        apply((Project)obj);
    }

    public void super$1$wait()
    {
        super.wait();
    }

    public String super$1$toString()
    {
        return super.toString();
    }

    public void super$1$wait(long l)
    {
        super.wait(l);
    }

    public void super$1$wait(long l, int i)
    {
        super.wait(l, i);
    }

    public void super$1$notify()
    {
        super.notify();
    }

    public void super$1$notifyAll()
    {
        super.notifyAll();
    }

    public Class super$1$getClass()
    {
        return super.getClass();
    }

    public Object super$1$clone()
    {
        return super.clone();
    }

    public boolean super$1$equals(Object obj)
    {
        return super.equals(obj);
    }

    public int super$1$hashCode()
    {
        return super.hashCode();
    }

    public void super$1$finalize()
    {
        super.finalize();
    }

    private static void $createCallSiteArray_1(String as[])
    {
        as[0] = "getLogger";
        as[1] = "info";
        as[2] = "configure";
    }

    private static CallSiteArray $createCallSiteArray()
    {
        String as[] = new String[3];
        $createCallSiteArray_1(as);
        return new CallSiteArray($get$$class$com$newrelic$agent$android$NewRelicGradlePlugin(), as);
    }

    private static CallSite[] $getCallSiteArray()
    {
        CallSiteArray callsitearray;
        if($callSiteArray == null || (callsitearray = (CallSiteArray)$callSiteArray.get()) == null)
        {
            callsitearray = $createCallSiteArray();
            $callSiteArray = new SoftReference(callsitearray);
        }
        return callsitearray.array;
    }

    private static Class $get$$class$com$newrelic$agent$android$NewRelicGradlePlugin()
    {
        return $class$com$newrelic$agent$android$NewRelicGradlePlugin == null && ($class$com$newrelic$agent$android$NewRelicGradlePlugin = _mthclass$("com.newrelic.agent.android.NewRelicGradlePlugin")) == null ? $class$com$newrelic$agent$android$NewRelicGradlePlugin : $class$com$newrelic$agent$android$NewRelicGradlePlugin;
    }

    private static Class $get$$class$org$gradle$api$logging$Logger()
    {
        return $class$org$gradle$api$logging$Logger == null && ($class$org$gradle$api$logging$Logger = _mthclass$("org.gradle.api.logging.Logger")) == null ? $class$org$gradle$api$logging$Logger : $class$org$gradle$api$logging$Logger;
    }

    private static Class $get$$class$java$lang$String()
    {
        return $class$java$lang$String == null && ($class$java$lang$String = _mthclass$("java.lang.String")) == null ? $class$java$lang$String : $class$java$lang$String;
    }

    static Class _mthclass$(String s)
    {
        try
        {
            return Class.forName(s);
        }
        catch(ClassNotFoundException classnotfoundexception)
        {
            throw new NoClassDefFoundError(classnotfoundexception.getMessage());
        }
    }

    private static Logger logger;
    private static ClassInfo $staticClassInfo;
    public static transient boolean __$stMC;
    private transient MetaClass metaClass;
    public static long __timeStamp;
    public static long __timeStamp__239_neverHappen1416597641990;
    private static SoftReference $callSiteArray;
    private static Class $class$com$newrelic$agent$android$NewRelicGradlePlugin;
    private static Class $class$org$gradle$api$logging$Logger;
    private static Class $class$java$lang$String;

    static 
    {
        __$swapInit();
        Long long1 = (Long)DefaultTypeTransformation.box(0L);
        __timeStamp__239_neverHappen1416597641990 = long1.longValue();
        Long _tmp = long1;
        Long long2 = (Long)DefaultTypeTransformation.box(0x149d3cc9b06L);
        __timeStamp = long2.longValue();
        Long _tmp1 = long2;
    }
}
