package org.bioinfo.gcsa.lib.users;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.gcsa.lib.users.beans.Project;
import org.bioinfo.gcsa.lib.users.beans.Plugin;
import org.bioinfo.gcsa.lib.users.beans.Session;
import org.bioinfo.gcsa.lib.users.persistence.AccountFileManager;
import org.bioinfo.gcsa.lib.users.persistence.AccountManagementException;
import org.bioinfo.gcsa.lib.users.persistence.AccountManager;
import org.bioinfo.gcsa.lib.users.persistence.AccountMongoDBManager;

public class CloudSessionManager {

	private AccountManager userManager;
	private Logger logger;
	public static Properties properties;

	public CloudSessionManager() throws FileNotFoundException, IOException, AccountManagementException {
		this(System.getenv("GCSA_HOME"));
	}

	public CloudSessionManager(String gcsaHome) throws FileNotFoundException, IOException, AccountManagementException {
		logger = new Logger();
		properties = new Properties();

		File propertiesFile = new File(gcsaHome + "/conf/account.properties");
		if (gcsaHome != null && propertiesFile.exists()) {
			properties.load(new FileInputStream(propertiesFile));
			if (properties.getProperty("GCSA.ACCOUNT.MODE").equals("file")) {
				userManager = new AccountFileManager(properties);
			} else {
				userManager = new AccountMongoDBManager(properties);
			}

			logger.info(properties.toString());
		} else {
			logger.error("properties file not found");
		}
	}

	/*******************************/
	public void createUser(String accountId, String password, String accountName, String email, String sessionIp)
			throws AccountManagementException {
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
	
	public List<Plugin> getUserAnalysis(String sessionId) throws AccountManagementException {
		return userManager.getUserAnalysis(sessionId);
	}

	public String login(String accountId, String password, String sessionIp) throws AccountManagementException {
		Session session = new Session(sessionIp);
		return userManager.login(accountId, password, session);
	}
	
	public void logout(String accountId, String sessionId) throws AccountManagementException {
		userManager.logout(accountId, sessionId);
	}
	
	public void changePassword(String accountId, String sessionId, String password, String nPassword1, String nPassword2)
			throws AccountManagementException {
		userManager.changePassword(accountId, sessionId, password, nPassword1, nPassword2);
	}
	
	public void changeEmail(String accountId, String sessionId, String nEmail) throws AccountManagementException {
		userManager.changeEmail(accountId, sessionId, nEmail);
	}
	
	public void resetPassword(String accountId, String email) throws AccountManagementException {
		userManager.resetPassword(accountId, email);
	}
	
	public String getAccountInfo(String accountId, String sessionId, String lastActivity) throws AccountManagementException {
		return userManager.getAccountBySessionId(accountId, sessionId, lastActivity);
	}

	public void createProject(Project project, String accountId, String sessionId) throws AccountManagementException {
		userManager.createProject(project, accountId, sessionId);
	}
	
	public String getAccountProjects(String accountId, String sessionId) throws AccountManagementException {
		return userManager.getAllProjectsBySessionId(accountId, sessionId);
	}
}
