package org.bioinfo.gcsa.lib.analysis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.bioinfo.commons.exec.Command;
import org.bioinfo.commons.exec.SingleProcess;
import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.gcsa.Config;
import org.bioinfo.gcsa.lib.utils.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class SgeManager {

	private static Logger logger = Logger.getLogger(SgeManager.class);
	private static Properties analysisProperties = Config.getAnalysisProperties();

	public static void queueJob(String toolName, String wumJobId, int wumUserId, String outdir, String commandLine,
			String queue) throws Exception {
		// init sge job
		String sgeCommandLine = "qsub -N " + getSgeJobName(toolName, wumJobId) + " -o " + outdir + "/sge_out.log -e "
				+ outdir + "/sge_err.log -q " + queue + " -b y " + commandLine;
		logger.info("SgeManager: Enqueuing job: " + sgeCommandLine);

		// thrown command to shell
		Command sgeCommand = new Command(sgeCommandLine);
		SingleProcess sp = new SingleProcess(sgeCommand);
		sp.getRunnableProcess().run();
	}

	public static void queueJob(String toolName, String wumJobId, int wumUserId, String outdir, String commandLine)
			throws Exception {

		// init sge job
		String sgeCommandLine = "qsub -N " + getSgeJobName(toolName, wumJobId) + " -o " + outdir + "/sge_out.log -e "
				+ outdir + "/sge_err.log -q " + getQueueName(toolName) + " -b y " + commandLine;
		logger.info("SgeManager: Enqueuing job: " + sgeCommandLine);

		// thrown command to shell
		Command sgeCommand = new Command(sgeCommandLine);
		SingleProcess sp = new SingleProcess(sgeCommand);
		sp.getRunnableProcess().run();
	}

	private static String getSgeJobName(String toolName, String wumJobId) {
		return toolName.replace(" ", "_") + "_" + wumJobId;
	}

	private static String getQueueName(String toolName) throws Exception {
		String defaultQueue = getDefaultQueue();
		logger.debug("SgeManager: default queue: " + defaultQueue);

		// get all available queues
		List<String> queueList = getQueueList();
		logger.debug("SgeManager: available queues: " + queueList);

		// search corresponding queue
		String selectedQueue = defaultQueue;
		String queueProperty;
		for (String queue : queueList) {
			if (!queue.equalsIgnoreCase(defaultQueue)) {
				queueProperty = "SGE." + queue.toUpperCase() + ".TOOLS";
				if (analysisProperties.containsKey(queueProperty)) {
					if (belongsTheToolToQueue(analysisProperties.getProperty(queueProperty), toolName)) {
						selectedQueue = queue;
					}
				}
			}
		}
		logger.info("SgeManager: selected queue for tool '" + toolName + "': " + selectedQueue);
		return selectedQueue;
	}

	private static String getDefaultQueue() throws Exception {
		if (analysisProperties.containsKey("SGE.DEFAULT.QUEUE")) {
			return analysisProperties.getProperty("SGE.DEFAULT.QUEUE");
		} else {
			throw new Exception("SGE.DEFAULT.QUEUE is not defined!");
		}
	}

	private static List<String> getQueueList() {
		if (analysisProperties.containsKey("SGE.AVAILABLE.QUEUES")) {
			return StringUtils.toList(analysisProperties.getProperty("SGE.AVAILABLE.QUEUES"), ",");
		} else {
			return new ArrayList<String>();
		}
	}

	private static boolean belongsTheToolToQueue(String tools, String toolName) {
		List<String> toolList = StringUtils.toList(tools, ",");
		// System.err.println("Tool list : " + toolList);
		return toolList.contains(toolName);
	}

	public static String status(String jobId) throws Exception {
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
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String aux = "";
			while ((aux = br.readLine()) != null) {
				stdOut.append(aux);
			}
			xml = stdOut.toString();
			br.close();
		} catch (Exception e) {
			logger.error(e.toString());
			throw new Exception("ERROR: can't get status for job " + jobId + ".");
		}

		if (xml != null) {
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(new InputSource(new StringReader(xml)));
				doc.getDocumentElement().normalize();
				NodeList nodeLst = doc.getElementsByTagName("job_list");

				for (int s = 0; s < nodeLst.getLength(); s++) {
					Node fstNode = nodeLst.item(s);

					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("JB_name");
						Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
						NodeList fstNm = fstNmElmnt.getChildNodes();
						String jobName = ((Node) fstNm.item(0)).getNodeValue();
						if (jobName.contains(jobId)) {
							NodeList lstNmElmntLst = fstElmnt.getElementsByTagName("state");
							Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
							NodeList lstNm = lstNmElmnt.getChildNodes();
							status = ((Node) lstNm.item(0)).getNodeValue();
						}
					}
				}
			} catch (Exception e) {
				logger.error(e.toString());
				throw new Exception("ERROR: can't get status for job " + jobId + ".");
			}
		}

		if (!status.equals("unknown")){
			status = stateDic.get(status);
		}
		else{
			String command = "qacct -j *"+ jobId +"*";
			logger.info(command);
			Process p = Runtime.getRuntime().exec(command);
		    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    String line;
		    String exitStatus = "";
		    String failed = "";
		    while ((line = in.readLine()) != null) {
		    	if(line.contains("exit_status")){
		    		exitStatus = line.replace("exit_status", "").trim();
		    		logger.info(line);
		    	}
		    	if(line.contains("failed")){
		    		failed = line.replace("failed", "").trim();
		    		logger.info(line);
		    	}
		    }
		    p.waitFor();
		    in.close();
		    
		    if("0".equals(exitStatus)){
		    	status = "finished";
		    }else{
		    	status = "execution error";
		    }
		    if(!"0".equals(failed)){
		    	status = "queue error";
		    }
		}
		return status;
	}

}
