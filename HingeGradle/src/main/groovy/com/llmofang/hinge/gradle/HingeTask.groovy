package com.llmofang.hinge.gradle

import com.sun.tools.attach.VirtualMachine
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import com.llmofang.hinge.agent.compile.RewriterAgent
import java.lang.management.ManagementFactory
import java.io.File


/**
 * Created by xu on 2014/12/26.
 */
class HingeTask extends  DefaultTask{
    Logger logger
     HingeTask()
    {
        logger=this.getLogger()
    }

    def getPid() {
        def nameOfRunningVm = ManagementFactory.getRuntimeMXBean().getName()
        def p=nameOfRunningVm.indexOf('@')
        return nameOfRunningVm.substring(0,p)
    }

    def getJarFilePath(){
        //HingeGradle改成agent对象
        try {
            def jarFilePath = RewriterAgent.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()
            jarFilePath = new File(jarFilePath).getCanonicalPath().toString()
            logger.info("[llmofang] Found New Relic instrumentation within " + jarFilePath)
            return jarFilePath
        }
        catch(URISyntaxException e )
        {
            logger.error("[llmofang] Unable to find instrumentation jar")
            throw new RuntimeException(e)
        }
        catch(IOException e)
        {
            logger.error("[llmofang] Unable to find  instrumentation jar")
            throw new RuntimeException(e)
        }
    }

    void injectAgent(String agentArgs){
wd
        com
         def vm=VirtualMachine.attach(this.getPid())
         vm.loadAgent(getJarFilePath(),agentArgs)
    }
}
