package org.bioinfo.gcsa.lib.account;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.gcsa.lib.account.beans.AnalysisPlugin;
import org.bioinfo.gcsa.lib.account.beans.Bucket;
import org.bioinfo.gcsa.lib.account.beans.Job;
import org.bioinfo.gcsa.lib.account.beans.ObjectItem;
import org.bioinfo.gcsa.lib.account.beans.Session;
import org.bioinfo.gcsa.lib.account.db.AccountFileManager;
import org.bioinfo.gcsa.lib.account.db.AccountManagementException;
import org.bioinfo.gcsa.lib.account.db.AccountManager;
import org.bioinfo.gcsa.lib.account.db.AccountMongoDBManager;
import org.bioinfo.gcsa.lib.account.io.IOManagementException;
import org.bioinfo.gcsa.lib.account.io.FileIOManager;
import org.bioinfo.gcsa.lib.storage.alignment.BamManager;
import org.bioinfo.infrared.lib.common.Region;
import org.dom4j.DocumentException;

public class CloudSessionManager {

	private AccountManager accountManager;
	private Logger logger;
	private FileIOManager ioManager;
	public static Properties properties;

	public CloudSessionManager() throws FileNotFoundException, IOException, AccountManagementException {
		this(System.getenv("GCSA_HOME"));
	}

	public CloudSessionManager(String gcsaHome) throws FileNotFoundException, IOException, AccountManagementException {
		logger = new Logger();
		properties = new Properties();

		File propertiesFile = new File(gcsaHome + "/conf/account.properties");
		if (gcsaHome != null && propertiesFile.exists()) {
			properties.load(new FileInputStream(propertiesFile));
			if (properties.getProperty("GCSA.ACCOUNT.MODE").equals("file")) {
				accountManager = (AccountManager) new AccountFileManager(properties);// TODO
			} else {
				accountManager = new AccountMongoDBManager(properties);
			}
			ioManager = new FileIOManager(properties);

			logger.info(properties.toString());
		} else {
			logger.error("properties file not found");
		}
	}

	/**
	 * @throws IOManagementException
	 *****************************/
	public void createAccount(String accountId, String password, String accountName, String email, String sessionIp)
			throws AccountManagementException, IOManagementException {
		checkStr(accountId, "accountId");
		checkStr(password, "password");
		checkStr(accountName, "accountName");
		checkEmail(email);
		checkStr(sessionIp, "sessionIp");
		Session session = new Session(sessionIp);

		ioManager.createAccount(accountId);

		try {
			accountManager.createAccount(accountId, password, accountName, email, session);
		} catch (AccountManagementException e) {
			ioManager.deleteAccount(accountId);
			throw e;
		}

	}

	public String createAnonymousAccount(String sessionIp) throws AccountManagementException, IOManagementException {
		checkStr(sessionIp, "sessionIp");
		Session session = new Session(sessionIp);

		String password = StringUtils.randomString(10);
		String accountId = "anonymous_" + password;

		ioManager.createAccount(accountId);
		try {
			return accountManager.createAnonymousAccount(accountId, password, session);
		} catch (AccountManagementException e) {
			ioManager.deleteAccount(accountId);
			throw e;
		}

	}

	public String login(String accountId, String password, String sessionIp) throws AccountManagementException {
		checkStr(accountId, "accountId");
		checkStr(password, "password");
		checkStr(sessionIp, "sessionIp");
		Session session = new Session(sessionIp);
		return accountManager.login(accountId, password, session);
	}

	public void logout(String accountId, String sessionId) throws AccountManagementException {
		checkStr(accountId, "accountId");
		checkStr(sessionId, "sessionId");
		accountManager.logout(accountId, sessionId);
	}

	public void logoutAnonymous(String sessionId) throws AccountManagementException, IOManagementException {
		String accountId = "anonymous_" + sessionId;
		System.out.println("-----> el accountId del anonimo es: " + accountId + " y la sesionId: " + sessionId);

		checkStr(accountId, "accountId");
		checkStr(sessionId, "sessionId");

		// TODO check inconsistency
		ioManager.deleteAccount(accountId);
		accountManager.logoutAnonymous(accountId, sessionId);
	}

	public void changePassword(String accountId, String sessionId, String password, String nPassword1, String nPassword2)
			throws AccountManagementException {
		checkStr(accountId, "accountId");
		checkStr(sessionId, "sessionId");
		checkStr(password, "password");
		checkStr(nPassword1, "nPassword1");
		checkStr(nPassword2, "nPassword2");
		if (!nPassword1.equals(nPassword2)) {
			throw new AccountManagementException("the new pass is not the same in both fields");
		}
		accountManager.changePassword(accountId, sessionId, password, nPassword1, nPassword2);
	}

	public void changeEmail(String accountId, String sessionId, String nEmail) throws AccountManagementException {
		checkStr(accountId, "accountId");
		checkStr(sessionId, "sessionId");
		checkEmail(nEmail);
		accountManager.changeEmail(accountId, sessionId, nEmail);
	}

	public void resetPassword(String accountId, String email) throws AccountManagementException {
		checkStr(accountId, "accountId");
		checkEmail(email);
		accountManager.resetPassword(accountId, email);
	}

	public String getAccountInfo(String accountId, String sessionId, String lastActivity)
			throws AccountManagementException {
		checkStr(accountId, "accountId");
		checkStr(sessionId, "sessionId");
		// lastActivity can be null
		return accountManager.getAccountInfo(accountId, sessionId, lastActivity);
	}

	public String getObjectPath(String accountId, String bucketId, Path ObjectId) {
		return ioManager.getObjectPath(accountId, bucketId, ObjectId).toString();
	}

	public void createBucket(String accountId, Bucket bucket, String sessionId) throws AccountManagementException,
			IOManagementException {
		checkStr(bucket.getName(), "bucketName");
		checkStr(accountId, "accountId");
		checkStr(sessionId, "sessionId");

		ioManager.createBucket(accountId, bucket.getName());
		try {
			accountManager.createBucket(accountId, bucket, sessionId);
		} catch (AccountManagementException e) {
			ioManager.deleteBucket(accountId, bucket.getName());
			throw e;
		}
	}

	public String createObjectToBucket(String accountId, String bucketId, Path objectId, ObjectItem objectItem,
			InputStream fileIs, boolean parents, String sessionId) throws AccountManagementException,
			IOManagementException, IOException {
		checkStr(bucketId, "bucket");
		checkStr(accountId, "accountId");
		checkStr(sessionId, "sessionId");
		checkStr(objectId.toString(), "objectId");
		checkObj(objectItem, "objectItem");

		objectId = ioManager.createObject(accountId, bucketId, objectId, objectItem, fileIs, parents);

		// set id and name to the itemObject
		objectItem.setId(objectId.toString());
		objectItem.setFileName(objectId.getFileName().toString());

		try {
			accountManager.createObjectToBucket(accountId, bucketId, objectItem, sessionId);
			return objectId.toString();
		} catch (AccountManagementException e) {
			ioManager.deleteObject(accountId, bucketId, objectId);
			throw e;
		}
	}

	public String createFolderToBucket(String accountId, String bucketId, Path objectId, ObjectItem objectItem,
			boolean parents, String sessionId) throws AccountManagementException, IOManagementException {
		checkStr(bucketId, "bucket");
		checkStr(accountId, "accountId");
		checkStr(sessionId, "sessionId");
		checkStr(objectId.toString(), "objectId");
		checkObj(objectItem, "objectItem");

		ioManager.createFolder(accountId, bucketId, objectId, parents);

		// set id and name to the itemObject
		objectItem.setId(objectId.toString());
		objectItem.setFileName(objectId.getFileName().toString());

		try {
			accountManager.createObjectToBucket(accountId, bucketId, objectItem, sessionId);
			return objectId.toString();
		} catch (AccountManagementException e) {
			ioManager.deleteObject(bucketId, accountId, objectId);
			throw e;
		}
	}

	public void deleteDataFromBucket(String accountId, String bucketId, Path objectId, String sessionId)
			throws AccountManagementException, IOManagementException {
		checkStr(bucketId, "bucket");
		checkStr(accountId, "accountId");
		checkStr(sessionId, "sessionId");
		checkStr(objectId.toString(), "objectId");

		objectId = ioManager.deleteObject(accountId, bucketId, objectId);
		accountManager.deleteObjectFromBucket(accountId, bucketId, objectId, sessionId);

	}

	public String checkJobStatus(String accountId, String jobId, String sessionId) throws AccountManagementException {
		Path jobPath = ioManager.getJobPath(accountId, "", jobId);
		if (Files.exists(Paths.get(jobPath.toString(), "result.xml"))) {
			accountManager.incJobVisites(accountId, jobId);
			return "DONE";
		}
		return "RUNNING";
	}

	public String region(String accountId, String bucketId, Path objectId, String regionStr,
			Map<String, List<String>> params, String sessionId) throws AccountManagementException,
			IOManagementException, IOException {

		checkStr(bucketId, "bucket");
		checkStr(accountId, "accountId");
		checkStr(sessionId, "sessionId");
		checkStr(objectId.toString(), "objectId");
		checkStr(regionStr, "regionStr");
		Region region = Region.parseRegion(regionStr);
		checkObj(region, "region");

		Path fullFilePath = ioManager.getObjectPath(accountId, bucketId, objectId);
		ObjectItem objectItem = accountManager.getObjectFromBucket(accountId, bucketId, objectId, sessionId);

		String result = "";
		switch (objectItem.getFileFormat()) {
		case "bam":
			BamManager bamManager = new BamManager();
			result = bamManager.getByRegion(fullFilePath, region.getChromosome(), region.getStart(), region.getEnd(),
					params);
			break;
		}

		return result;
	}

	public String getJobResultFromBucket(String accountId, String bucketId, String jobId, String sessionId)
			throws IOException, DocumentException, IOManagementException, AccountManagementException {
		checkStr(accountId, "accountId");
		checkStr(bucketId, "bucketId");
		checkStr(jobId, "jobId");
		checkStr(sessionId, "sessionId");
		return ioManager.getJobResultFromBucket(accountId, bucketId, jobId, sessionId);
	}

	public String getFileTableFromJob(String accountId, String bucketId, String jobId, String filename, String start,
			String limit, String colNames, String colVisibility, String callback, String sort, String sessionId)
			throws IOManagementException, IOException, AccountManagementException {
		// TODO check all
		checkStr(accountId, "accountId");
		checkStr(bucketId, "bucketId");
		checkStr(filename, "filename");
		checkStr(sessionId, "sessionId");

		return ioManager.getFileTableFromJob(accountId, bucketId, jobId, filename, start, limit, colNames,
				colVisibility, callback, sort, sessionId);
	}

	public DataInputStream getFileFromJob(String accountId, String bucketId, String jobId, String filename, String zip,
			String sessionId) throws IOManagementException, IOException, AccountManagementException {
		checkStr(bucketId, "bucket");
		checkStr(accountId, "accountId");
		checkStr(sessionId, "sessionId");
		checkStr(filename, "filename");
		checkStr(zip, "zip");

		return ioManager.getFileFromJob(accountId, bucketId, jobId, filename, zip, sessionId);
	}

	public String getAccountBuckets(String accountId, String sessionId) throws AccountManagementException {
		return accountManager.getAllBucketsBySessionId(accountId, sessionId);
	}

	public String createJob(String jobName, String jobFolder, String toolName, List<String> dataList,
			String commandLine, String sessionId) throws AccountManagementException, IOManagementException {

		checkStr(jobName, "jobName");
		checkStr(toolName, "toolName");
		checkStr(sessionId, "sessionId");
		String accountId = accountManager.getAccountIdBySessionId(sessionId);

		String jobId = StringUtils.randomString(15);
		boolean jobFolderCreated = false;

		if (jobFolder == null) {
			logger.debug("PAKO jobfolder=null");
			ioManager.createJob(accountId, jobId);
			jobFolder = "jobs:" + jobId;
			jobFolderCreated = true;
		}
		checkStr(jobFolder, "jobFolder");

		Job job = new Job(jobId, jobName, jobFolder, toolName, Job.QUEUED, commandLine, "", dataList);

		try {
			accountManager.createJob(accountId, job, sessionId);
		} catch (AccountManagementException e) {
			if (jobFolderCreated) {
				ioManager.removeJob(accountId, jobId);
			}
			throw e;
		}

		return jobId;

	}

	public String getJobFolder(String accountId, String jobId) {
		return ioManager.getJobPath(accountId, null, jobId).toString();
	}

	public List<AnalysisPlugin> getUserAnalysis(String sessionId) throws AccountManagementException {
		return accountManager.getUserAnalysis(sessionId);
	}

	public void setJobCommandLine(String accountId, String jobId, String commandLine) throws AccountManagementException {
		accountManager.setJobCommandLine(accountId, jobId, commandLine);
	}

	/********************/
	private void checkEmail(String email) throws AccountManagementException {
		String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
				+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		Pattern pattern = Pattern.compile(EMAIL_PATTERN);
		if (!pattern.matcher(email).matches()) {
			throw new AccountManagementException("email not valid");
		}
	}

	private void checkStr(String str, String name) throws AccountManagementException {
		if (str == null || str.equals("") || str.equals("null")) {
			throw new AccountManagementException("parameter '" + name + "' is null or empty: " + str + ".");
		}
	}

	private void checkObj(Object obj, String name) throws AccountManagementException {
		if (obj == null) {
			throw new AccountManagementException("parameter '" + name + "' is null.");
		}
	}
}
