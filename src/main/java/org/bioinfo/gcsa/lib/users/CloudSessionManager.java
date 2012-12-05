package org.bioinfo.gcsa.lib.users;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.gcsa.lib.users.beans.Plugin;
import org.bioinfo.gcsa.lib.users.beans.Session;
import org.bioinfo.gcsa.lib.users.persistence.UserFileManager;
import org.bioinfo.gcsa.lib.users.persistence.UserManagementException;
import org.bioinfo.gcsa.lib.users.persistence.UserManager;
import org.bioinfo.gcsa.lib.users.persistence.UserMongoDBManager;

public class CloudSessionManager {

	private UserManager userManager;
	private Logger logger;
	public static Properties properties;

	public CloudSessionManager() throws FileNotFoundException, IOException, UserManagementException {
		this(System.getenv("GCSA_HOME"));
	}

	public CloudSessionManager(String gcsaHome) throws FileNotFoundException, IOException, UserManagementException {
		logger = new Logger();
		properties = new Properties();

		File propertiesFile = new File(gcsaHome + "/conf/account.properties");
		if (gcsaHome != null && propertiesFile.exists()) {
			properties.load(new FileInputStream(propertiesFile));
			if (properties.getProperty("GCSA.ACCOUNT.MODE").equals("file")) {
				userManager = new UserFileManager(properties);
			} else {
				userManager = new UserMongoDBManager(properties);
			}

			logger.info(properties.toString());
		} else {
			logger.error("properties file not found");
		}
	}

	public String getUserByAccountId(String accountId, String sessionId) {
		userManager.checkSessionId(accountId, sessionId);

		return null;
	}

	public String createProject(String projectId, String accountId, String sessionId) throws UserManagementException {
		userManager.checkSessionId(accountId, sessionId);

		return null;
	}

	public void createUser(String accountId, String password, String accountName, String email, String sessionIp)
			throws UserManagementException {
		Session session = new Session(sessionIp);
		userManager.createUser(accountId, password, accountName, email, session);
	}
	
	public String getDataPath(String dataId, String sessionId) {
		return userManager.getDataPath(dataId, sessionId);
	}
	
	public String createJob(String jobName, String jobFolder, String project, String toolName, List<String> dataList,
			String commandLine, String sessionId) {
		return userManager.createJob(jobName, jobFolder, project, toolName, dataList, commandLine, sessionId);
	}
	
	public String getJobFolder(String project, String jobId, String sessionId) {
		return userManager.getJobFolder(project, jobId, sessionId);
	}
	
	public List<Plugin> getUserAnalysis(String sessionId) throws UserManagementException {
		return userManager.getUserAnalysis(sessionId);
	}

	@Deprecated
	public UserManager getUserManager() {
		return userManager;
	}

}
