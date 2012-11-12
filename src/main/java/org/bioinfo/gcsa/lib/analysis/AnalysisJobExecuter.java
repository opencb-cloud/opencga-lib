package org.bioinfo.gcsa.lib.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.bioinfo.commons.Config;
import org.bioinfo.commons.exec.Command;
import org.bioinfo.commons.exec.SingleProcess;
import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.commons.io.utils.IOUtils;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.commons.utils.ListUtils;
import org.bioinfo.commons.utils.MapUtils;
import org.bioinfo.gcsa.lib.analysis.plugin.Execution;
import org.bioinfo.gcsa.lib.analysis.plugin.Option;
import org.bioinfo.gcsa.lib.analysis.plugin.Plugin;

import com.google.gson.Gson;

public class AnalysisJobExecuter {
	
	protected Gson gson = new Gson();
	protected Config config;
	protected Logger logger;
	
	public AnalysisJobExecuter() throws IOException {
		config = new Config("/tmp/config/analysis.properties");
		gson = new Gson();
		logger = new Logger();
		logger.setLevel(Logger.INFO_LEVEL);
	}
	
	public String execute(Map<String, List<String>> params) {
		System.out.println("params received in execute: "+params);
		
		String sessionId, pluginName, executionId;
		Plugin plugin = null;
		
		if(params.containsKey("sessionId")) {
			sessionId = params.get("sessionId").get(0);
			params.remove("sessionId");
		}
		else {
			return "ERROR: Session is not initialized yet.";
		}
		
		if(params.containsKey("plugin")) {
			pluginName = params.get("plugin").get(0);
			params.remove("plugin");
		}
		else {
			return "ERROR: Plugin name not provided.";
		}
		
		if(params.containsKey("executionId")) {
			executionId = params.get("executionId").get(0);
			params.remove("executionId");
		}
		else {
			return "ERROR: Execution ID not provided.";
		}
		
		String pluginsPath = config.getProperty("PLUGINS.PATH");
		String pluginPath = pluginsPath + "/" + pluginName + "/";
		String manifestFile = pluginPath + "manifest.json";
		
		System.out.println("Manifest file: " + manifestFile);
		
		try {
			FileUtils.checkFile(manifestFile);
		} catch (IOException e) {
			e.printStackTrace();
			return "Manifest for " + pluginName + " not found.";
		}
		
		// Parse JSON to Plugin object
		try {
			BufferedReader br = new BufferedReader(new FileReader(manifestFile));
			plugin = gson.fromJson(br, Plugin.class);
			
		} catch (IOException e) {
			e.printStackTrace();
			return "Invalid manifest.json for " + pluginName + ".";
		}
		
		/** Jquery put this parameter and it is sent to the tool **/
		if(params.containsKey("_")){
			params.remove("_");
		}
		
		// create job
		int jobId = 0;
		String jobFolder = "/tmp/testoutdir/";
		//TODO crear job
//		int jobId = wni.createJob(jobName, toolName, ListUtils.toString(dataList,","), sessionId);
//		String jobFolder = wni.getJobFolder(jobId, sessionId);
		logger.debug("AnalysisJobExecuter: execute, 'jobId': "+jobId+", 'jobFolder': "+jobFolder);

		// set command in binary path
		String binaryPath = null;
		Execution execution = null;
		for(Execution exe : plugin.getExecutions()) {
			if(exe.getId().equalsIgnoreCase(executionId)) {
				execution = exe;
				break;
			}
		}
		if(execution == null) {
			return "ERROR: Executable not found.";
		}
		binaryPath = pluginPath + execution.getExecutable();
		
		params.put(execution.getOutParam(), Arrays.asList(jobFolder));
		
		for(String inputParam: execution.getInputParam()) {
			if(!params.containsKey(inputParam)) {
				return "ERROR: Input param '" + inputParam + "' is required.";
			}
			else {
				//TODO obtener el path del input param a partir del dataId recibido
				String dataPath = "";
				
				params.put(inputParam, Arrays.asList(dataPath));
			}
		}
		
		// Check required params
		List<Option> validParams = execution.getOptions();
		if(checkRequiredParams(params, validParams)) {
			params = new HashMap<String, List<String>>(removeUnknownParams(params, validParams));
		}
		else {
			return "ERROR: missing some required params.";
		}
		
		// set command line
		String commandLine = binaryPath + createCommandLine(params);
		logger.debug("AnalysisJobExecuter: execute, command line: " + commandLine);
		
//		try {
//			logger.debug("AnalysisJobExecuter: execute, creating form.txt, input_params.txt and cli.txt");
//			IOUtils.write(new File(jobFolder + "/input_params.txt"), MapUtils.toString(params));
//			IOUtils.write(new File(jobFolder + "/cli.txt"), commandLine);
//		} catch (IOException ioe){
//			logger.error(ioe.toString());
//			ioe.printStackTrace();
//		}

		// read execution param
		String jobExecutor = config.getProperty("JOB.EXECUTOR");

		// local execution
		if(jobExecutor == null || jobExecutor.trim().equalsIgnoreCase("LOCAL")) {
			logger.debug("AnalysisJobExecuter: execute, running by SingleProcess");

//			Command com = new Command(commandLine);
//			SingleProcess sp = new SingleProcess(com);
//			sp.getRunnableProcess().run();
		}
		// sge execution
		else {			
			logger.debug("AnalysisJobExecuter: execute, running by SgeManager");

			SgeManager sgeManager = new SgeManager(config);
			try {
				sgeManager.queueJob(pluginName, jobId, 0, jobFolder, commandLine);
			} catch (Exception e) {
				e.printStackTrace();
				//return "ERROR: could not queue job: " + e.getMessage();
			}
		}

//		GlobalId globalId = new GlobalId(jobId, sessionId);
//
//		logger.debug("AnalysisJobExecuter: execute, global id = " + globalId.toString());
//
//		return globalId.toString();
		return commandLine;
	}
	
	private boolean checkRequiredParams(Map<String, List<String>> params, List<Option> validParams) {
		for(Option param : validParams) {
			if(param.isRequired() && !params.containsKey(param.getName())) {
				return false;
			}
		}
		return true;
	}
	
	private Map<String, List<String>> removeUnknownParams(Map<String, List<String>> params, List<Option> validOptions) {
		Set<String> validKeyParams = new HashSet<String>();
		for(Option param : validOptions) {
			validKeyParams.add(param.getName());
		}
		
		Map<String, List<String>> paramsCopy = new HashMap<String, List<String>>(params);
		for(String param : params.keySet()) {
			if(!validKeyParams.contains(param)) {
				paramsCopy.remove(param);
			}
		}
		
		return paramsCopy;
	}
	
	private String createCommandLine(Map<String, List<String>> params) {
		System.out.println("params received in createCommandLine: "+params);

		StringBuilder cmdLine = new StringBuilder();
		
		if(params.containsKey("tool")) {
			String tool = params.get("tool").get(0);
			cmdLine.append(" --tool ").append(tool);
			params.remove("tool");
		}
		
		for(String key: params.keySet()) {
			//if (!key.equalsIgnoreCase("sessionid")) {
			
			// Removing renato param
			if(!key.equals("renato")){
				if(key.length() == 1){
					cmdLine.append(" -").append(key);
				}
				else{
					cmdLine.append(" --").append(key);
				}
				if (params.get(key)!=null) {
					cmdLine.append(" ").append(params.get(key).get(0));
				}
			}
			//}
		}
		return cmdLine.toString();
	}
}
