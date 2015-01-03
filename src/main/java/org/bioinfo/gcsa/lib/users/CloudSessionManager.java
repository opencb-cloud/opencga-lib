package org.bioinfo.gcsa.lib.users;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.gcsa.lib.users.beans.Project;
import org.bioinfo.gcsa.lib.users.beans.Plugin;
import org.bioinfo.gcsa.lib.users.beans.Session;
import org.bioinfo.gcsa.lib.users.persistence.AccountFileManager;
import org.bioinfo.gcsa.lib.users.persistence.AccountManagementException;
import org.bioinfo.gcsa.lib.users.persistence.AccountManager;
import org.bioinfo.gcsa.lib.users.persistence.AccountMongoDBManager;

public class CloudSessionManager {

	private AccountManager accountManager;
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
				accountManager = new AccountFileManager(properties);
			} else {
				accountManager = new AccountMongoDBManager(properties);
			}

			logger.info(properties.toString());
		} else {
			logger.error("properties file not found");
		}
	}

	/*******************************/
	public void createAccount(String accountId, String password, String accountName, String email, String sessionIp)
			throws AccountManagementException {
		checkStr(accountId, "accountId");
		checkStr(password, "password");
		checkStr(accountName, "accountName");
		checkEmail(email);
		checkStr(sessionIp, "sessionIp");
		Session session = new Session(sessionIp);

		accountManager.createAccount(accountId, password, accountName, email, session);
	}

	public String login(String accountId, String password, String sessionIp) throws AccountManagementException {
		Session session = new Session(sessionIp);
		return accountManager.login(accountId, password, session);
	}

	public void logout(String accountId, String sessionId) throws AccountManagementException {
		accountManager.logout(accountId, sessionId);
	}

	public void changePassword(String accountId, String sessionId, String password, String nPassword1, String nPassword2)
			throws AccountManagementException {
		accountManager.changePassword(accountId, sessionId, password, nPassword1, nPassword2);
	}

	public void changeEmail(String accountId, String sessionId, String nEmail) throws AccountManagementException {
		checkEmail(nEmail);
		accountManager.changeEmail(accountId, sessionId, nEmail);
	}

	public void resetPassword(String accountId, String email) throws AccountManagementException {
		checkEmail(email);
		accountManager.resetPassword(accountId, email);
	}

	public String getAccountInfo(String accountId, String sessionId, String lastActivity)
			throws AccountManagementException {
		return accountManager.getAccountBySessionId(accountId, sessionId, lastActivity);
	}
	
	
	
	public String getDataPath(String dataId, String sessionId) {
		return accountManager.getDataPath(dataId, sessionId);
	}
	
	public void createProject(Project project, String accountId, String sessionId) throws AccountManagementException {
		accountManager.createProject(project, accountId, sessionId);
	}

	public String getAccountProjects(String accountId, String sessionId) throws AccountManagementException {
		return accountManager.getAllProjectsBySessionId(accountId, sessionId);
	}

	public String createJob(String jobName, String jobFolder, String project, String toolName, List<String> dataList,
			String commandLine, String sessionId) {
		return accountManager.createJob(jobName, jobFolder, project, toolName, dataList, commandLine, sessionId);
	}
	
	public String getJobFolder(String project, String jobId, String sessionId) {
		return accountManager.getJobFolder(project, jobId, sessionId);
	}
	
	public List<Plugin> getUserAnalysis(String sessionId) throws AccountManagementException {
		return accountManager.getUserAnalysis(sessionId);
	}
	/********************/
	private void checkEmail(String email) throws AccountManagementException {
		String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
				+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		Pattern pattern = Pattern.compile(EMAIL_PATTERN);
		if (!pattern.matcher(email).matches()) {
			throw new AccountManagementException("email not valid");
		}
	}

	private void checkStr(String str, String name) throws AccountManagementException {
		if (str == null || str.equals("")) {
			throw new AccountManagementException("parameter '" + name + "' is null or empty: " + str + ".");
		}
	}
}
