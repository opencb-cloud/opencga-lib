package org.bioinfo.gcsa.lib.users.persistence;

import java.util.List;

import org.bioinfo.gcsa.lib.users.beans.Project;



public class UserFileManager implements UserManager {

	public void createUser(String accountId, String password, String email) {
	
	}

	public void createAnonymousUser(String accountId, String password,
			String email) {
		
	}

	public String login(String accountId, String password) {
		return null;
	}

	public String getUserByAccountId(String accountId, String sessionId) {
		return null;
	}

	public String getUserByEmail(String email, String sessionId) {
		return null;
	}

	public void checkSessionId(String accountId, String sessionId) {
		
	}

	public String getAllProjectsBySessionId(String accountId, String sessionId) {
		return null;
	}

	public List<Project> jsonToProjectList(String json) {
		return null;
	}

	public void createUser(String accountId, String password,
			String accountName, String email) {
		// TODO Auto-generated method stub
		
	}

	public void createProject(Project project, String accountId,
			String sessionId) throws UserManagementException {
		// TODO Auto-generated method stub
		
	}

}
