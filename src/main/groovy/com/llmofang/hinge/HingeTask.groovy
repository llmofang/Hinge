package com.llmofang.hinge

import com.sun.tools.attach.VirtualMachine
import org.apache.http.conn.ManagedClientConnection
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction

import java.lang.management.ManagementFactory


/**
 * Created by xu on 2014/12/26.
 */
class HingeTask extends  DefaultTask{
    Logger logger
     HingeTask()
    {
        logger=this.getLogger()
    }

    def getPid(){
        def nameOfRunningVm= ManagementFactory.getRuntimeMXBean().getName()
        def p=nameOfRunningVm.indexOf('@')
        return nameOfRunningVm.substring(0,p)

    }

    def getJarFilePath(){
        //HingeGradle改成agent对象
        try {
            def jarFilePath = HingeGradlePlugin.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()
            jarFilePath = new File(jarFilePath).getCanonicalPath().toString()
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

         def vm=VirtualMachine.attach(this.getPid())
         vm.loadAgent(getJarFilePath(),agentArgs)

    }


}
