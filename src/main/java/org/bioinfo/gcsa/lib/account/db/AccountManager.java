package org.bioinfo.gcsa.lib.account.db;

import java.nio.file.Path;
import java.util.List;

import org.bioinfo.gcsa.lib.account.beans.Acl;
import org.bioinfo.gcsa.lib.account.beans.Job;
import org.bioinfo.gcsa.lib.account.beans.ObjectItem;
import org.bioinfo.gcsa.lib.account.beans.AnalysisPlugin;
import org.bioinfo.gcsa.lib.account.beans.Bucket;
import org.bioinfo.gcsa.lib.account.beans.Session;
import org.bioinfo.gcsa.lib.account.db.AccountManagementException;
import org.bioinfo.gcsa.lib.account.io.IOManagementException;

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

	public void deleteObjectFromBucket(String accountId, String bucketId, Path objectId, String sessionId)
			throws AccountManagementException;

	public void shareObject(String accountId, String bucketId, Path objectId, Acl acl, String sessionId)
			throws AccountManagementException;

	/*
	 * Job methods
	 */
	public void createJob(String accountId, String projectId, Job job, String sessionId) throws AccountManagementException;

	public void deleteJob(String accountId, String jobId, String sessionId) throws AccountManagementException;

	public String getJob(String accountId, String jobId, String sessionId) throws AccountManagementException;

	public Path getJobPath(String accountId, String projectId, String jobId, String sessionId) throws AccountManagementException;

	public String getJobStatus(String accountId, String projectId, String jobId, String sessionId) throws AccountManagementException;

	public void incJobVisites(String accountId, String projectId, String jobId, String sessionId) throws AccountManagementException;

	public void setJobCommandLine(String accountId, String projectId, String jobId, String commandLine) throws AccountManagementException;

	/*
	 * Utils
	 */
	public List<AnalysisPlugin> getUserAnalysis(String sessionId) throws AccountManagementException;

	public List<Bucket> jsonToBucketList(String json);

	public ObjectItem getObjectFromBucket(String accountId, String bucketId, Path objectId, String sessionId)
			throws AccountManagementException;

	public String getAccountIdBySessionId(String sessionId);

}
