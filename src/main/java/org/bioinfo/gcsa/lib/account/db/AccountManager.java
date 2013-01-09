package org.bioinfo.gcsa.lib.account.db;

import java.nio.file.Path;
import java.util.List;

import org.bioinfo.gcsa.lib.account.beans.Job;
import org.bioinfo.gcsa.lib.account.beans.ObjectItem;
import org.bioinfo.gcsa.lib.account.beans.AnalysisPlugin;
import org.bioinfo.gcsa.lib.account.beans.Bucket;
import org.bioinfo.gcsa.lib.account.beans.Session;
import org.bioinfo.gcsa.lib.account.db.AccountManagementException;

public interface AccountManager {

	/*
	 * Account methods
	 */
	public void createAccount(String accountId, String password, String accountName, String email, Session session)
			throws AccountManagementException;

	public String createAnonymousAccount(String accountId, String password, Session session)
			throws AccountManagementException;

	public String login(String accountId, String password, Session session) throws AccountManagementException;

	public void logout(String accountId, String sessionId) throws AccountManagementException;

	public void logoutAnonymous(String accountId, String sessionId);

	// public String getUserByAccountId(String accountId, String sessionId);

	// public String getUserByEmail(String email, String sessionId);

	public String getAccountInfo(String accountId, String sessionId, String lastActivity)
			throws AccountManagementException;

	public void changePassword(String accountId, String sessionId, String password, String nPassword1, String nPassword2)
			throws AccountManagementException;

	public void changeEmail(String accountId, String sessionId, String nEmail) throws AccountManagementException;

	public void resetPassword(String acccountId, String email) throws AccountManagementException;

	/*
	 * Project methods
	 */

	// public boolean checkSessionId(String accountId, String sessionId);

	public Session getSession(String accountId, String sessionId);

	public String getAllBucketsBySessionId(String accountId, String sessionId) throws AccountManagementException;

	public void createBucket(String accountId, Bucket bucket, String sessionId) throws AccountManagementException;

	// add file to project
	public void createObjectToBucket(String accountId, String bucketId, ObjectItem objectItem, String sessionId)
			throws AccountManagementException;

	public void deleteObjectFromBucket(String accountId, String bucketId, Path objectId,  String sessionId)
			throws AccountManagementException;

	/*
	 * Job methods
	 */
	public void createJob(String accountId, Job job, String sessionId) throws AccountManagementException;

	// public String getJobFolder(String project, String jobId, String
	// sessionId);

	public List<AnalysisPlugin> getUserAnalysis(String sessionId) throws AccountManagementException;

	public void incJobVisites(String accountId, String jobId) throws AccountManagementException;

	/*
	 * Utils
	 */
	public List<Bucket> jsonToBucketList(String json);

	public ObjectItem getObjectFromBucket(String accountId, String bucketId, Path objectId, String sessionId)
			throws AccountManagementException;

	public String getAccountIdBySessionId(String sessionId);

}
