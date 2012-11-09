package org.bioinfo.gcsa.lib.users;


import org.bioinfo.gcsa.lib.users.beans.Project;
import org.bioinfo.gcsa.lib.users.persistence.UserManagementException;

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
