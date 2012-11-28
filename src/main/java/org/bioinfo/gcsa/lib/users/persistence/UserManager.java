package org.bioinfo.gcsa.lib.users.persistence;

import java.io.InputStream;
import java.util.List;

import org.bioinfo.gcsa.lib.users.beans.Data;
import org.bioinfo.gcsa.lib.users.beans.Project;
import org.bioinfo.gcsa.lib.users.beans.Session;

public interface UserManager {

	/*
	 * User methods
	 */
	public void createUser(String accountId, String password,String accountName, String email,Session session) throws UserManagementException;
	
	public void createAnonymousUser(String accountId, String password, String email);
	
	public String login(String accountId, String password, Session session);
	
	public String logout(String accountId, String sessionId);
	
//	public String testPipe(String accountId, String password); //Pruebas, hay que borrarlo
	
	public String getUserByAccountId(String accountId, String sessionId);
	
	public String getUserByEmail(String email, String sessionId);
	
	public String getUserBySessionId(String sessionId);
	
	
	/*
	 * Project methods
	 */
	
	public boolean checkSessionId(String accountId, String sessionId);
	
	public Session getSessionId(String accountId, String sessionId);
	
	public String getAllProjectsBySessionId(String accountId, String sessionId);
	
	public String createProject(Project project, String accountId, String sessionId);

	//add file to project
	public String createFileToProject(String project, String fileName, InputStream fileData, String sessionId);
	
	
	/*
	 * Job methods
	 */
	public String createJob(String jobName, String jobFolder, String toolName, List<String> dataList, String sessionId);
	
	
	/*
	 * Utils
	 */
	public List<Project> jsonToProjectList(String json);

	public String getJobFolder(String jobId, String sessionId);
	
}
