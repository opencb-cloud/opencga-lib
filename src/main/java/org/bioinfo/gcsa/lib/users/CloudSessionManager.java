package org.bioinfo.gcsa.lib.users;


import org.bioinfo.gcsa.lib.users.persistence.UserFileManager;
import org.bioinfo.gcsa.lib.users.persistence.UserManagementException;
import org.bioinfo.gcsa.lib.users.persistence.UserManager;

public class CloudSessionManager {

	
	private static UserManager userManager;
	
	public CloudSessionManager() {
		// read config file
		userManager = new UserFileManager();
	}
	
	public String getUserByAccountId(String accountId, String sessionId) {
		userManager.checkSessionId(accountId, sessionId);
		
		return null;
	}
	
	public String createProject(String projectId, String accountId, String sessionId) throws UserManagementException {
		userManager.checkSessionId(accountId, sessionId);
		
		return null;
	}
}
