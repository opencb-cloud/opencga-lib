package org.bioinfo.gcsa.lib.account.db;

import java.util.List;
import java.util.Properties;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.gcsa.lib.account.beans.Data;
import org.bioinfo.gcsa.lib.account.beans.Plugin;
import org.bioinfo.gcsa.lib.account.beans.Project;
import org.bioinfo.gcsa.lib.account.beans.Session;

public class AccountFileManager implements AccountManager {

	private Logger logger;
	private Properties properties;

	private String home;
	private String accounts;
	private String tmp;

	public AccountFileManager(Properties properties) {
		logger = new Logger();
		logger.setLevel(Logger.INFO_LEVEL);
		this.properties = properties;
		home = System.getenv(properties.getProperty("GCSA.ENV.HOME"));
		accounts = home + properties.getProperty("GCSA.ACCOUNT.PATH");
		tmp = properties.getProperty("TMP.PATH");
	}

	@Override
	public void createAccount(String accountId, String password, String accountName, String email, Session session)
			throws AccountManagementException {

	}

	@Override
	public void createAnonymousAccount(Session session) throws AccountManagementException {

	}

	@Override
	public String login(String accountId, String password, Session session) {
		return null;
	}

	@Override
	public void logout(String accountId, String sessionId) {

	}

	// @Override
	// public String getUserByAccountId(String accountId, String sessionId) {
	// return null;
	// }

	@Override
	public String getUserByEmail(String email, String sessionId) {
		return null;
	}

	@Override
	public void changePassword(String accountId, String sessionId, String password, String nPassword1, String nPassword2) {
	}

	@Override
	public void changeEmail(String accountId, String sessionId, String nEmail) {

	}

	@Override
	public boolean checkSessionId(String accountId, String sessionId) {
		return false;
	}

	@Override
	public Session getSession(String accountId, String sessionId) {
		return null;
	}

	@Override
	public String getAllProjectsBySessionId(String accountId, String sessionId) {
		return null;
	}

	@Override
	public String createProject(Project project, String accountId, String sessionId) {
		return null;
	}

	@Override
	public void createDataToProject(String project, String accountId, String sessionId, Data data) throws AccountManagementException {
	}

	@Override
	public String createJob(String jobName, String jobFolder, String project, String toolName, List<String> dataList,
			String commandLine, String sessionId) {
		return null;
	}

	@Override
	public List<Project> jsonToProjectList(String json) {
		return null;
	}

	@Override
	public String getJobFolder(String project, String jobId, String sessionId) {
		return null;
	}

	@Override
	public void resetPassword(String acccountId, String email) {

	}

	@Override
	public String getAccountBySessionId(String accountId, String sessionId, String lastActivity) {
		return null;
	}

	@Override
	public List<Plugin> getUserAnalysis(String sessionId) {
		return null;
	}

	@Override
	public String getDataPath(String projectId, String dataId, String sessionId) {
		return null;
	}

	@Override
	public void logoutAnonymous(String accountId, String sessionId) {
		
	}

}
