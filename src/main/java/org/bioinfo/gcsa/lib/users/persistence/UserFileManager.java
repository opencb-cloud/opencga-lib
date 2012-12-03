package org.bioinfo.gcsa.lib.users.persistence;

import java.io.InputStream;
import java.util.List;

import org.bioinfo.gcsa.lib.users.beans.Project;
import org.bioinfo.gcsa.lib.users.beans.Session;



public class UserFileManager implements UserManager {

	@Override
	public void createUser(String accountId, String password,
			String accountName, String email, Session session)
			throws UserManagementException {
		
	}

	@Override
	public void createAnonymousUser(String accountId, String password,
			String email) {
		
	}

	@Override
	public String login(String accountId, String password, Session session) {
		return null;
	}

	@Override
	public String logout(String accountId, String sessionId) {
		return null;
	}

	@Override
	public String getUserByAccountId(String accountId, String sessionId) {
		return null;
	}

	@Override
	public String getUserByEmail(String email, String sessionId) {
		return null;
	}

	@Override
	public String getAccountBySessionId(String sessionId) {
		return null;
	}

	@Override
	public String changePassword(String accountId, String password,
			String nPassword1, String nPassword2) {
		return null;
	}

	@Override
	public String changeEmail(String accountId, String sessionId, String nEmail) {
		return null;
	}

	@Override
	public boolean checkSessionId(String accountId, String sessionId) {
		return false;
	}

	@Override
	public Session getSessionId(String accountId, String sessionId) {
		return null;
	}

	@Override
	public String getAllProjectsBySessionId(String accountId, String sessionId) {
		return null;
	}

	@Override
	public String createProject(Project project, String accountId,
			String sessionId) {
		return null;
	}

	@Override
	public String createFileToProject(String project, String fileName,
			InputStream fileData, String sessionId) {
		return null;
	}

	@Override
	public String createJob(String jobName, String jobFolder, String project,
			String toolName, List<String> dataList, String commandLine,
			String sessionId) {
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
	public String resetPassword(String acccountId, String email) {
		return null;
	}


}
