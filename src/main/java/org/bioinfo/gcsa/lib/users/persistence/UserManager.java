package org.bioinfo.gcsa.lib.users.persistence;

import java.util.List;

import org.bioinfo.gcsa.lib.users.beans.Project;
import org.bioinfo.gcsa.lib.users.persistence.UserManagementException;

public interface UserManager {

	/*
	 * User methods
	 */
	public void createUser(String accountId, String password,String accountName, String email) throws UserManagementException;
	
	public void createAnonymousUser(String accountId, String password, String email);
	
	public String login(String accountId, String password);
	
	public String getUserByAccountId(String accountId, String sessionId);
	
	public String getUserByEmail(String email, String sessionId);
	
	
	/*
	 * Project methods
	 */
	public void checkSessionId(String accountId, String sessionId);
	
	public String getAllProjectsBySessionId(String accountId, String sessionId);
	
	public void createProject(Project project, String accountId, String sessionId) throws UserManagementException;

	
	/*
	 * Utils
	 */
	public List<Project> jsonToProjectList(String json);
	
}
