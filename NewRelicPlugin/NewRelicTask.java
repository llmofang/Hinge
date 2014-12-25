import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.codehaus.groovy.runtime.BytecodeInterface8;
import org.gradle.api.DefaultTask;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;


public class NewRelicTask extends DefaultTask
implements GroovyObject {
	Logger logger;
	 public NewRelicTask()
	    {
		   logger=NewRelicGradlePlugin.getLogger();
	        MetaClass metaClass=getMetaClass();
	        return;
	    }

	 public Object getPid()
	    {
			String  naemOfRunningVM=ManagementFactory.getRuntimeMXBean().getName();
	        int p=naemOfRunningVM.indexOf("@");
	        return naemOfRunningVM.substring(0,p);
	    }
	 
	 public Object getJarFilePath()
	    {
		 try
	        {
	        String s;
			String  jarFilePath=com.newrelic.agent.compile.RewriterAgent.getProtectionDomain.getCodeSource.getLocation.toURI.getPath;
	        String obj=new File(jarFilePath).getCanonicalPath();
	        jarFilePath=obj.toString();
			logger.info(("[newrelic] Found New Relic instrumentation within "+jarFilePath));
			s = jarFilePath;
	            return s;
	        }
	        catch(URISyntaxException e)
	        {
	            logger.error("[newrelic] Unable to find New Relic instrumentation jar");
	            throw new RuntimeException(e);
	        }
	        catch(IOException e)
	        {
	            logger.error("[newrelic] Unable to find New Relic instrumentation jar"); 
	            throw new RuntimeException(e);
	        }
	    }
	 
	 public void injectAgent(String agentArgs) throws AgentLoadException, AgentInitializationException, IOException
	{
		VirtualMachine vm=null;
		try {
			if(1==1){
				vm=VirtualMachine.attach(this.getPid().toString());
			}else{
				
				vm=VirtualMachine.attach(this.getPid().toString());
			}
			if(2==2){
				vm.loadAgent(this.getJarFilePath().toString(),agentArgs);
			}else{
				vm.loadAgent(this.getJarFilePath().toString(),agentArgs);
			}
			
		} catch (AttachNotSupportedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		 vm.detach();
	}

	public MetaClass getMetaClass() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getProperty(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object invokeMethod(String arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMetaClass(MetaClass arg0) {
		// TODO Auto-generated method stub
		
	}
}
