package org.bioinfo.gcsa.lib.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.bioinfo.commons.exec.Command;
import org.bioinfo.commons.exec.SingleProcess;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.commons.utils.StringUtils;

public class SgeManager {

	private Properties config;
	private Logger logger;

	public SgeManager(Properties defaultConfig){
		this.config = defaultConfig;

		// create logger object, level is read from 'wum.properties': WUM.LOG.LEVEL
		logger = new Logger();
		logger.setLevel(Integer.parseInt(defaultConfig.getProperty("SGE.LOG.LEVEL")));
	}

	public void queueJob(String toolName, String wumJobId, int wumUserId, String outdir, String commandLine, String queue) throws Exception{
		// init sge job
		String sgeCommandLine = "qsub -N " + getSgeJobName(toolName,wumJobId) + " -o " + outdir + "/sge_out.log -e " + outdir + "/sge_err.log -q " + queue + " -b y " + commandLine;
		logger.info("SgeManager: Enqueuing job: "+sgeCommandLine);

		// thrown command to shell
		Command sgeCommand = new Command(sgeCommandLine);
		SingleProcess sp = new SingleProcess(sgeCommand);
		sp.getRunnableProcess().run();
	}
	
	public void queueJob(String toolName, String wumJobId, int wumUserId, String outdir, String commandLine) throws Exception{

		// init sge job
		String sgeCommandLine = "qsub -N " + getSgeJobName(toolName,wumJobId) + " -o " + outdir + "/sge_out.log -e " + outdir + "/sge_err.log -q " + getQueueName(toolName) + " -b y " + commandLine;
		logger.info("SgeManager: Enqueuing job: "+sgeCommandLine);

		// thrown command to shell
		Command sgeCommand = new Command(sgeCommandLine);
		SingleProcess sp = new SingleProcess(sgeCommand);
		sp.getRunnableProcess().run();
	}

	private String getSgeJobName(String toolName,String wumJobId){
		return toolName.replace(" ", "_") + "_" +wumJobId;
	}

	private String getQueueName(String toolName) throws Exception{
		String defaultQueue = getDefaultQueue();
		logger.debug("SgeManager: default queue: " + defaultQueue);

		// get all available queues
		List<String> queueList = getQueueList();
		logger.debug("SgeManager: available queues: " + queueList);

		// search corresponding queue
		String selectedQueue = defaultQueue;
		String queueProperty;
		for(String queue: queueList){
			if(!queue.equalsIgnoreCase(defaultQueue)){
				queueProperty =  "SGE." + queue.toUpperCase() + ".TOOLS";
				if(config.containsKey(queueProperty)){					
					if(belongsTheToolToQueue(config.getProperty(queueProperty), toolName)){
						selectedQueue = queue; 
					}
				}
			}
		}
		logger.info("SgeManager: selected queue for tool '"+toolName+"': " + selectedQueue);
		return selectedQueue;
	}

	private String getDefaultQueue() throws Exception{
		if(config.containsKey("SGE.DEFAULT.QUEUE")){
			return config.getProperty("SGE.DEFAULT.QUEUE");
		} else {
			throw new Exception("SGE.DEFAULT.QUEUE is not defined!");
		}		
	}

	private List<String> getQueueList(){		
		if(config.containsKey("SGE.AVAILABLE.QUEUES")) {			
			return StringUtils.toList(config.getProperty("SGE.AVAILABLE.QUEUES"),",");	
		} else {
			return new ArrayList<String>();
		}
	}

	private boolean belongsTheToolToQueue(String tools, String toolName){
		List<String> toolList = StringUtils.toList(tools,",");
		//		System.err.println("Tool list : " + toolList);
		return toolList.contains(toolName);
	}

}
