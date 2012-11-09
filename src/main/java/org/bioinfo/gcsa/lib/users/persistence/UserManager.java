package java.org.bioinfo.gcsa.lib.users.persistence;

import java.org.bioinfo.gcsa.lib.users.beans.Project;
import java.util.List;


public interface UserManager {

	/*
	 * User methods
	 */
	public void createUser(String accountId, String password,String accountName, String email);
	
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
