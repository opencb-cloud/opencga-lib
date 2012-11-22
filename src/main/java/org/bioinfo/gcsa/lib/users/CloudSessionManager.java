package org.bioinfo.gcsa.lib.users;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.bioinfo.gcsa.lib.users.persistence.UserFileManager;
import org.bioinfo.gcsa.lib.users.persistence.UserManagementException;
import org.bioinfo.gcsa.lib.users.persistence.UserManager;
import org.bioinfo.gcsa.lib.users.persistence.UserMongoDBManager;

public class CloudSessionManager {

	public static UserManager userManager;

	public static Properties properties;

	public CloudSessionManager() throws FileNotFoundException, IOException, UserManagementException {
		this(System.getenv("GCSA_HOME"));
	}

	public CloudSessionManager(String gcsaHome) throws FileNotFoundException,
			IOException, UserManagementException {
		// read config file
		System.err.println ("-----------------> creado CloudSessionManager");
		properties = new Properties();
		properties.load(new FileInputStream(gcsaHome + "/conf/account.properties"));
		if (properties.getProperty("GCSA.ACCOUNT.MODE").equals("file")) {
			userManager = new UserFileManager();
		} else {
			 userManager = new UserMongoDBManager();
		}
		System.out.println(properties.toString());
	}

	public String getUserByAccountId(String accountId, String sessionId) {
		userManager.checkSessionId(accountId, sessionId);

		return null;
	}

	public String createProject(String projectId, String accountId,
			String sessionId) throws UserManagementException {
		userManager.checkSessionId(accountId, sessionId);

		return null;
	}

}
