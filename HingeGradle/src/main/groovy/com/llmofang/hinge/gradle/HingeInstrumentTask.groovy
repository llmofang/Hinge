package com.llmofang.hinge.gradle

import com.google.common.io.BaseEncoding
import org.gradle.api.tasks.TaskAction

/**
 * Created by xu on 2014/12/26.
 */
class HingeInstrumentTask extends HingeTask{
    @TaskAction
    def hingInstrumentTask(){
        def extraArgs=System.getProperty('Hinge.AgentArgs')
        def encodedProjectRoot=BaseEncoding.base64().encode(this.getProject().getProjectDir().getCanonicalPath().getBytes())
        def agentArgs="projectRoot="+encodedProjectRoot
        if(extraArgs)
        {
            agentArgs=agentArgs+";"+extraArgs
        }
        try {
            this.getLogger().info(this.getPid().toString() + "[llmofang] Attaching to process for instrumentation ")
            this.injectAgent(agentArgs)
        }catch (Exception e){
            this.getLogger().error("[llmofang] Error encountered while loading the New Relic agent",e)
            throw new RuntimeException(e);
        }

    }

}
