package org.bioinfo.gcsa.lib.users.persistence;

import java.io.InputStream;
import java.util.List;

import org.bioinfo.gcsa.lib.users.beans.Data;
import org.bioinfo.gcsa.lib.users.beans.Plugin;
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
	
	public String getUserByAccountId(String accountId, String sessionId);
	
	public String getUserByEmail(String email, String sessionId);
	
	public String getAccountBySessionId(String sessionId, String lastActivity);
	
	public String changePassword (String accountId, String sessionId, String password, String nPassword1, String nPassword2);
	
	public String changeEmail (String accountId, String sessionId, String nEmail);
	
	public String resetPassword (String acccountId, String email);
	
	
	/*
	 * Project methods
	 */
	
	public boolean checkSessionId(String accountId, String sessionId);
	
	public Session getSessionId(String accountId, String sessionId);
	
	public String getAllProjectsBySessionId(String accountId, String sessionId);
	
	public String createProject(Project project, String accountId, String sessionId);
	
	//add file to project
	public String createDataToProject(String project, String accountId, String sessionId, Data data, InputStream fileData);
	
	
	/*
	 * Job methods
	 */
	public String createJob(String jobName, String jobFolder, String project, String toolName, List<String> dataList, String commandLine, String sessionId);
	
	public String getJobFolder(String project, String jobId, String sessionId);
	
	public List<Plugin> getUserAnalysis(String sessionId) throws UserManagementException;
	
	
	/*
	 * Data methods
	 */
	public String getDataPath(String dataId, String sessionId);
	
	/*
	 * Utils
	 */
	public List<Project> jsonToProjectList(String json);

}
