// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   NewRelicDeinstrumentTask.groovy

package com.newrelic.agent.android;

import groovy.lang.Closure;
import groovy.lang.MetaClass;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.Set;

import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.runtime.*;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.callsite.CallSiteArray;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.gradle.api.*;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.internal.tasks.TaskStateInternal;
import org.gradle.api.internal.tasks.execution.TaskValidator;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.*;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.logging.StandardOutputCapture;

// Referenced classes of package com.newrelic.agent.android:
//            NewRelicTask

public class NewRelicDeinstrumentTask extends NewRelicTask
{


    public Object newRelicInsturmentTask()
    {
        Object obj2;
        String extraArgs=System.getProperty("NewRelic.AgentArgs");
        String agentArgs = "deinstrument=true";
        if(!BytecodeInterface8.isOrigZ() || __$stMC || BytecodeInterface8.disabledStandardMetaClass())
        {
            if(!extraArgs.equals(null))
            {
            	 agentArgs=agentArgs+";"+extraArgs;
                
            }
        } else
        	 if(!extraArgs.equals(null))
             {
             	 agentArgs=agentArgs+";"+extraArgs;
                 
             }
        this.getLogger().info(this.getPid().toString()+"[newrelic] Attaching to process for deinstrumentation ");
        Obj2=this.injectAgent(agentArgs);
        Exception exception1;
        try
        {
            return obj2;
        }
        // Misplaced declaration of an exception variable
        catch(Exception e)
        {
            this.getLogger().error("[newrelic] Error encountered while loading the New Relic agent",e);
            throw new RuntimeException(e);
        }
    }

   
}
