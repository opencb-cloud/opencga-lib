package org.bioinfo.gcsa.lib.users.persistence;

import java.io.InputStream;
import java.util.List;

import org.bioinfo.gcsa.lib.users.beans.Data;
import org.bioinfo.gcsa.lib.users.beans.Plugin;
import org.bioinfo.gcsa.lib.users.beans.Project;
import org.bioinfo.gcsa.lib.users.beans.Session;

public interface AccountManager {

	/*
	 * User methods
	 */
	public void createAccount(String accountId, String password,String accountName, String email,Session session) throws AccountManagementException;
	
	public void createAnonymousUser(String accountId, String password, String email);
	
	public String login(String accountId, String password, Session session) throws AccountManagementException;
	
	public void logout(String accountId, String sessionId) throws AccountManagementException;
	
//	public String getUserByAccountId(String accountId, String sessionId);
	
	public String getUserByEmail(String email, String sessionId);
	
	public String getAccountBySessionId(String accountId, String sessionId, String lastActivity) throws AccountManagementException;
	
	public void changePassword (String accountId, String sessionId, String password, String nPassword1, String nPassword2) throws AccountManagementException;
	
	public void changeEmail (String accountId, String sessionId, String nEmail) throws AccountManagementException;
	
	public void resetPassword (String acccountId, String email) throws AccountManagementException;
	
	
	/*
	 * Project methods
	 */
	
	public boolean checkSessionId(String accountId, String sessionId);
	
	public Session getSession(String accountId, String sessionId);
	
	public String getAllProjectsBySessionId(String accountId, String sessionId) throws AccountManagementException;
	
	public String createProject(Project project, String accountId, String sessionId) throws AccountManagementException;
	
	//add file to project
	public void createDataToProject(String project, String accountId, String sessionId, Data data, InputStream fileData) throws AccountManagementException;
	
	
	/*
	 * Job methods
	 */
	public String createJob(String jobName, String jobFolder, String project, String toolName, List<String> dataList, String commandLine, String sessionId);
	
	public String getJobFolder(String project, String jobId, String sessionId);
	
	public List<Plugin> getUserAnalysis(String sessionId) throws AccountManagementException;
	
	
	/*
	 * Data methods
	 */
	public String getDataPath(String projectId, String dataId, String sessionId);
	
	/*
	 * Utils
	 */
	public List<Project> jsonToProjectList(String json);


}
