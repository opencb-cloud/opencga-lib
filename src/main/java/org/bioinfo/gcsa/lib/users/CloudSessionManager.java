package org.bioinfo.gcsa.lib.users;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.gcsa.lib.users.beans.Project;
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

	/*******************************/
	public void createUser(String accountId, String password, String accountName, String email, String sessionIp)
			throws UserManagementException {
		Session session = new Session(sessionIp);
		userManager.createUser(accountId, password, accountName, email, session);
	}

	public String login(String accountId, String password, String sessionIp) throws UserManagementException {
		Session session = new Session(sessionIp);
		return userManager.login(accountId, password, session);
	}

	public String getAccountInfo(String accountId, String sessionId, String lastActivity) {
		return userManager.getAccountBySessionId(accountId, sessionId, lastActivity);
	}

	public void createProject(Project project, String accountId, String sessionId) throws UserManagementException {
		userManager.createProject(project, accountId, sessionId);
	}

	public void logout(String accountId, String sessionId) throws UserManagementException {
		userManager.logout(accountId, sessionId);
	}

	public String getAccountProjects(String accountId, String sessionId) throws UserManagementException {
		return userManager.getAllProjectsBySessionId(accountId, sessionId);
	}

	public void changePassword(String accountId, String sessionId, String password, String nPassword1, String nPassword2)
			throws UserManagementException {
		userManager.changePassword(accountId, sessionId, password, nPassword1, nPassword2);
	}

	public void changeEmail(String accountId, String sessionId, String nEmail) throws UserManagementException {
		userManager.changeEmail(accountId, sessionId, nEmail);
	}

	public void resetPassword(String accountId, String email) throws UserManagementException {
		userManager.resetPassword(accountId, email);
	}

	@Deprecated
	public UserManager getUserManager() {
		return userManager;
	}

}
