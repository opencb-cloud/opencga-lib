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

	public boolean checkSessionId(String accountId, String sessionId) {
		return false;
		
	}

	public String getAllProjectsBySessionId(String accountId, String sessionId) {
		return null;
	}

	public List<Project> jsonToProjectList(String json) {
		return null;
	}

	public void createUser(String accountId, String password,
			String accountName, String email) {
		
	}

	public String createProject(Project project, String accountId,
			String sessionId){
		return null;
		
	}

	@Override
	public void createUser(String accountId, String password,
			String accountName, String email, Session session)
			throws UserManagementException {
		
	}

	@Override
	public String logout(String accountId, String sessionId) {
		return null;
	}

	@Override
	public String createFileToProject(String project, String fileName, InputStream fileData, String sessionId) {
		return null;
	}

	@Override
	public String createJob(String jobName, String toolName,
			List<String> dataList, String sessionId) {
		return null;
	}


	@Override
	public Session getSessionId(String accountId, String sessionId) {
		return null;
	}
<<<<<<< HEAD
=======

	@Override
	public Set<String> getAllOldIdSessions(String accountId, String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getJobFolder(String jobId, String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUserBySessionId(String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

>>>>>>> 7441380b4db5743266b9fa709c89ac77fe4c043b
}
