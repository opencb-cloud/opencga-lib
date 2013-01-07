package org.bioinfo.gcsa.lib.account.db;

import java.util.List;

import org.bioinfo.gcsa.lib.account.beans.ObjectItem;
import org.bioinfo.gcsa.lib.account.beans.Plugin;
import org.bioinfo.gcsa.lib.account.beans.Bucket;
import org.bioinfo.gcsa.lib.account.beans.Session;
import org.bioinfo.gcsa.lib.account.db.AccountManagementException;

public interface AccountManager {

	/*
	 * User methods
	 */
	public void createAccount(String accountId, String password, String accountName, String email, Session session)
			throws AccountManagementException;

	public String createAnonymousAccount(Session session) throws AccountManagementException;

	public String login(String accountId, String password, Session session) throws AccountManagementException;

	public void logout(String accountId, String sessionId) throws AccountManagementException;

	public void logoutAnonymous (String accountId, String sessionId);
	
	// public String getUserByAccountId(String accountId, String sessionId);

	public String getUserByEmail(String email, String sessionId);

	public String getAccountBySessionId(String accountId, String sessionId, String lastActivity)
			throws AccountManagementException;

	public void changePassword(String accountId, String sessionId, String password, String nPassword1, String nPassword2)
			throws AccountManagementException;

	public void changeEmail(String accountId, String sessionId, String nEmail) throws AccountManagementException;

	public void resetPassword(String acccountId, String email) throws AccountManagementException;

	/*
	 * Project methods
	 */

	public boolean checkSessionId(String accountId, String sessionId);

	public Session getSession(String accountId, String sessionId);

	public String getAllBucketsBySessionId(String accountId, String sessionId) throws AccountManagementException;

	public void createBucket(Bucket project, String accountId, String sessionId) throws AccountManagementException;

	// add file to project
	public void createObjectToBucket(String project, String accountId, String sessionId, ObjectItem data)
			throws AccountManagementException;

	public void deleteDataFromBucket(String project, String accountId, String sessionId, String dataId)
			throws AccountManagementException;

	/*
	 * Job methods
	 */
	public String createJob(String jobName, String jobFolder, String project, String toolName, List<String> dataList,
			String commandLine, String sessionId);

	public String getJobFolder(String project, String jobId, String sessionId);

	public List<Plugin> getUserAnalysis(String sessionId) throws AccountManagementException;
	
	public void incJobVisites(String accountId, String jobId) throws AccountManagementException;

	/*
	 * Data methods
	 */
	public String getDataPath(String projectId, String dataId, String sessionId);

	/*
	 * Utils
	 */
	public List<Bucket> jsonToBucketList(String json);

	public ObjectItem getDataFromBucket(String bucket, String accountId, String sessionId, String dataId)
			throws AccountManagementException;

}
