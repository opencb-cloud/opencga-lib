package org.bioinfo.gcsa.lib.users.persistence;

import java.io.InputStream;
import java.util.List;

import org.bioinfo.gcsa.lib.users.beans.Project;
import org.bioinfo.gcsa.lib.users.beans.Session;



public class UserFileManager implements UserManager {

	public void createAnonymousUser(String accountId, String password,
			String email) {
	}

	public String login(String accountId, String password, Session session) {
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

	@Override
	public void createUser(String accountId, String password,
			String accountName, String email, Session session)
			throws UserManagementException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String testPipe(String accountId, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public String logout(String accountId, String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createFileToProject(String project, String fileName, InputStream fileData, String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

}
