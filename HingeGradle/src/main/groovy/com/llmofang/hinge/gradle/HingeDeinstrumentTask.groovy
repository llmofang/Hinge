package com.llmofang.hinge.gradle

import org.gradle.api.tasks.TaskAction

/**
 * Created by xu on 2014/12/26.
 */
class HingeDeinstrumentTask extends HingeTask{
    @TaskAction
    def hingeDeinstrumentTask(){
        try {
            def extraArgs = System.getProperty("Hinge.AgentArgs")
            def agentArgs = "deinstrument=true"
            if (extraArgs) {
                agentArgs = agentArgs + ";" + extraArgs;
            }
            this.getLogger().info(this.getPid().toString() + "[llmofang] Attaching to process for deinstrumentation ");
            this.injectAgent(agentArgs);
        }catch(Exception e)
        {
            this.getLogger().error("[llmofang] Error encountered while loading the agent",e);
            throw new RuntimeException(e);
        }

    }

}
