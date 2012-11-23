package org.bioinfo.gcsa.lib.analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bioinfo.commons.Config;
import org.bioinfo.commons.exec.Command;
import org.bioinfo.commons.exec.SingleProcess;
import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.gcsa.lib.analysis.SgeManager;
import org.bioinfo.gcsa.lib.analysis.beans.Analysis;
import org.bioinfo.gcsa.lib.analysis.beans.Execution;
import org.bioinfo.gcsa.lib.analysis.beans.InputParam;
import org.bioinfo.gcsa.lib.analysis.beans.Option;
import org.bioinfo.gcsa.lib.users.CloudSessionManager;
import org.bioinfo.gcsa.lib.users.persistence.UserManagementException;

import com.google.gson.Gson;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class AnalysisJobExecuter {
	
	protected Gson gson = new Gson();
	protected Config config;
	protected Logger logger;
	protected String homePath;
	protected String analysisName;
	protected String analysisRootPath;
	protected String analysisPath;
	protected String executionName;
	protected String manifestFile;
	protected String sessionId;
	protected CloudSessionManager cloudSessionManager;
	
	public AnalysisJobExecuter() throws IOException, UserManagementException {
		homePath = System.getenv("GCSA_HOME");
		config = new Config(homePath + "/conf/analysis.properties");
		analysisRootPath = config.getProperty("ANALYSIS.BINARIES.PATH");
		cloudSessionManager = new CloudSessionManager();
		gson = new Gson();
		logger = new Logger();
		logger.setLevel(Integer.parseInt(config.getProperty("ANALYSIS.LOG.LEVEL")));
	}
	
	public AnalysisJobExecuter(String analysis) throws IOException, UserManagementException {
		homePath = System.getenv("GCSA_HOME");
		config = new Config(homePath + "/conf/analysis.properties");
		analysisRootPath = config.getProperty("ANALYSIS.BINARIES.PATH");
		cloudSessionManager = new CloudSessionManager();
		
		gson = new Gson();
		logger = new Logger();
		logger.setLevel(Integer.parseInt(config.getProperty("ANALYSIS.LOG.LEVEL")));
		
		analysisName = analysis;
		executionName = null;
		if(analysisName.contains(".")) {
			executionName = analysisName.split("\\.")[1];
			analysisName = analysisName.split("\\.")[0];
		}
		
		analysisPath = homePath + "/" + analysisRootPath + "/" + analysisName + "/";
		manifestFile = analysisPath + "manifest.json";
	}
	
	public String execute(Map<String, List<String>> params) { //TODO probar cuando funcione lo de usuarios
		System.out.println("params received in execute: "+params);
		
		if(params.containsKey("sessionid")) {
			sessionId = params.get("sessionid").get(0);
			params.remove("sessionid");
		}
		else {
			return "ERROR: Session is not initialized yet.";
		}
		
		if(analysisName == null || analysisName.equals("")) {
			return "ERROR: Analysis name not provided.";
		}
		
		System.out.println("Manifest file: " + manifestFile);
		
		try {
			FileUtils.checkFile(manifestFile);
		} catch (IOException e) {
			e.printStackTrace();
			return "Manifest for " + analysisName + " not found.";
		}
		
		Analysis analysis = parseJsonToAnalysis();
		
		if(analysis == null) {
			return "Invalid manifest.json for " + analysisName + ".";
		}
		
		/** Jquery put this parameter and it is sent to the tool **/
		if(params.containsKey("_")){
			params.remove("_");
		}
		
		// create job
//		int jobId = 0;
		String jobId = cloudSessionManager.createJob("", "", new ArrayList<String>(), sessionId);
		String jobFolder = "/tmp/testoutdir/";
		//TODO crear job
//		int jobId = wni.createJob(jobName, toolName, ListUtils.toString(dataList,","), sessionId);
//		String jobFolder = wni.getJobFolder(jobId, sessionId);
		logger.debug("AnalysisJobExecuter: execute, 'jobId': "+jobId+", 'jobFolder': "+jobFolder);
		
		Execution execution = getExecution(analysis);
		if(execution == null) {
			return "ERROR: Executable not found.";
		}
		
		// set command in binary path
		String binaryPath = null;
		binaryPath = analysisPath + execution.getExecutable();
		
		params.put(execution.getOutputParam(), Arrays.asList(jobFolder));
		
		// Check required params
		List<Option> validParams = execution.getValidParams();
		validParams.addAll(analysis.getGlobalParams());
		if(checkRequiredParams(params, validParams)) {
			params = new HashMap<String, List<String>>(removeUnknownParams(params, validParams));
		}
		else {
			return "ERROR: missing some required params.";
		}
		
		for(InputParam inputParam: execution.getInputParams()) {
			if(params.containsKey(inputParam)) {
				//TODO obtener el path del input param a partir del dataId recibido
				String dataPath = "/fake/data/path";
				
				params.put(inputParam.getName(), Arrays.asList(dataPath));
			}
		}
		
		// set command line
		String commandLine = binaryPath + createCommandLine(params);
		logger.debug("AnalysisJobExecuter: execute, command line: " + commandLine);

		executeCommandLine(commandLine, jobId, jobFolder);
		
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
	
	private void executeCommandLine(String commandLine, String jobId, String jobFolder) {
//		try {
//			logger.debug("AnalysisJobExecuter: execute, creating form.txt, input_params.txt and cli.txt");
//			IOUtils.write(new File(jobFolder + "/input_params.txt"), MapUtils.toString(params));
//			IOUtils.write(new File(jobFolder + "/cli.txt"), commandLine);
//		} catch (IOException ioe){
//			logger.error(ioe.toString());
//			ioe.printStackTrace();
//		}
		
		// read execution param
		String jobExecutor = config.getProperty("ANALYSIS.JOB.EXECUTOR");

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
				sgeManager.queueJob(analysisName, jobId, 0, jobFolder, commandLine);
			} catch (Exception e) {
				e.printStackTrace();
				//return "ERROR: could not queue job: " + e.getMessage();
			}
		}
	}
	
	private Analysis parseJsonToAnalysis() {
		Analysis analysis = null;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(manifestFile));
			analysis = gson.fromJson(br, Analysis.class);

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return analysis;
	}
	
	private Execution getExecution(Analysis analysis) {
		Execution execution = null;
		
		if(executionName != null) {
			for(Execution exe : analysis.getExecutions()) {
				if(exe.getId().equalsIgnoreCase(executionName)) {
					execution = exe;
					break;
				}
			}
		}
		else {
			execution = analysis.getExecutions().get(0);
		}
		
		return execution;
	}
	
	public String help(String baseUrl) {
		Analysis analysis = null;
		
		try {
			FileUtils.checkFile(manifestFile);
		} catch (IOException e) {
			e.printStackTrace();
			return "Manifest for " + analysisName + " not found.";
		}
		
		analysis = parseJsonToAnalysis();
		
		if(analysis == null) {
			return "Invalid manifest.json for " + analysisName + ".";
		}
		else {
			String execName = "";
			if(executionName != null) execName = "."+executionName;
			StringBuilder sb = new StringBuilder();
			sb.append("Analysis: "+analysis.getName()+"\n");
			sb.append("Description: "+analysis.getDescription()+"\n");
			sb.append("Version: "+analysis.getVersion()+"\n\n");
			sb.append("Author: "+analysis.getAuthor().getName()+"\n");
			sb.append("Email: "+analysis.getAuthor().getEmail()+"\n");
			if(!analysis.getWebsite().equals("")) sb.append("Website: "+analysis.getWebsite()+"\n");  
			if(!analysis.getPublication().equals("")) sb.append("Publication: "+analysis.getPublication()+"\n");  
			sb.append("\nUsage: \n");
			sb.append(baseUrl+"analysis/"+analysisName+execName+"/{action}?{params}\n\n");
			sb.append("\twhere: \n");
			sb.append("\t\t{action} = [run, help, params, test, status]\n");
			sb.append("\t\t{params} = "+baseUrl+"analysis/"+analysisName+execName+"/params\n");
			return sb.toString();
		}
	}
	
	public String params() {
		Analysis analysis = null;
		
		try {
			FileUtils.checkFile(manifestFile);
		} catch (IOException e) {
			e.printStackTrace();
			return "Manifest for " + analysisName + " not found.";
		}
		
		analysis = parseJsonToAnalysis();
		
		if(analysis == null) {
			return "Invalid manifest.json for " + analysisName + ".";
		}
		
		Execution execution = getExecution(analysis);
		if(execution == null) {
			return "ERROR: Executable not found.";
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("Valid params for "+analysis.getName()+":\n\n");
		for(Option param : execution.getValidParams()) {
			String required = "";
			if(param.isRequired()) required = "*"; 
			sb.append("\t"+param.getName()+": "+param.getDescription()+" "+required+"\n");
		}
		sb.append("\n\t*: required parameters.\n");
		return sb.toString();
	}
	
	public String test() { //TODO probar cuando funcione lo de usuarios
		Analysis analysis = null;
		
		try {
			FileUtils.checkFile(manifestFile);
		} catch (IOException e) {
			e.printStackTrace();
			return "Manifest for " + analysisName + " not found.";
		}
		
		analysis = parseJsonToAnalysis();
		
		if(analysis == null) {
			return "Invalid manifest.json for " + analysisName + ".";
		}
		
		Execution execution = getExecution(analysis);
		if(execution == null) {
			return "ERROR: Executable not found.";
		}
		
		// create job
		String jobId = cloudSessionManager.createJob("", "", new ArrayList<String>(), sessionId);
		String jobFolder = "/tmp/";
		//TODO crear job
//		int jobId = wni.createJob(jobName, toolName, ListUtils.toString(dataList,","), sessionId);
//		String jobFolder = wni.getJobFolder(jobId, sessionId);
		
//		executeCommandLine(execution.getTestCmd(), jobId, jobFolder);
		
		return String.valueOf(jobId);
	}
	
	public String status(String jobId) {
		String status = "unknown";
		Map<String, String> stateDic = new HashMap<String, String>();
		stateDic.put("r", "running");
		stateDic.put("t", "transferred");
		stateDic.put("qw", "queued");
		stateDic.put("Eqw", "error");
		
		String xml = null;
		try {
			Process p = Runtime.getRuntime().exec("qstat -xml");
			StringBuilder stdOut = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader (p.getInputStream()));

			String aux = "";
			while ((aux = br.readLine()) != null) {
				stdOut.append(aux);
			}
			xml = stdOut.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(xml != null) {
			try {
//			File file = new File("/tmp/qstat.xml");
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(new InputSource(new StringReader(xml)));
				doc.getDocumentElement().normalize();
//				System.out.println("Root element " + doc.getDocumentElement().getNodeName());
				NodeList nodeLst = doc.getElementsByTagName("job_list");
//				System.out.println("Information of all jobs");
				
				for (int s = 0; s < nodeLst.getLength(); s++) {
					Node fstNode = nodeLst.item(s);
					
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("JB_name");
						Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
						NodeList fstNm = fstNmElmnt.getChildNodes();
//						System.out.println("Job Name : "  + ((Node) fstNm.item(0)).getNodeValue());
						String jobName = ((Node) fstNm.item(0)).getNodeValue();
						if(jobName.contains("j"+jobId+"_")) {
							NodeList lstNmElmntLst = fstElmnt.getElementsByTagName("state");
							Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
							NodeList lstNm = lstNmElmnt.getChildNodes();
//							System.out.println("State : " + ((Node) lstNm.item(0)).getNodeValue());
							status = ((Node) lstNm.item(0)).getNodeValue();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(!status.equals("unknown")) status = stateDic.get(status);
		
		return status;
	}
}
