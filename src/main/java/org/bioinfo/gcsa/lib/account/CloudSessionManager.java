package org.bioinfo.gcsa.lib.account;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.gcsa.lib.account.beans.ObjectItem;
import org.bioinfo.gcsa.lib.account.beans.Plugin;
import org.bioinfo.gcsa.lib.account.beans.Bucket;
import org.bioinfo.gcsa.lib.account.beans.Session;
import org.bioinfo.gcsa.lib.account.db.AccountFileManager;
import org.bioinfo.gcsa.lib.account.db.AccountManagementException;
import org.bioinfo.gcsa.lib.account.db.AccountManager;
import org.bioinfo.gcsa.lib.account.db.AccountMongoDBManager;
import org.bioinfo.gcsa.lib.account.io.IOManagementException;
import org.bioinfo.gcsa.lib.account.io.IOManager;
import org.bioinfo.gcsa.lib.storage.alignment.BamManager;
import org.bioinfo.infrared.lib.common.Region;
import org.dom4j.DocumentException;

public class CloudSessionManager {

	private AccountManager accountManager;
	private Logger logger;
	private IOManager ioManager;
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
			ioManager = new IOManager(properties);

			logger.info(properties.toString());
		} else {
			logger.error("properties file not found");
		}
	}

	/*******************************/
	public void createAccount(String accountId, String password, String accountName, String email, String sessionIp)
			throws AccountManagementException {
		checkStr(accountId, "accountId");
		checkStr(password, "password");
		checkStr(accountName, "accountName");
		checkEmail(email);
		checkStr(sessionIp, "sessionIp");
		Session session = new Session(sessionIp);

		accountManager.createAccount(accountId, password, accountName, email, session);
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
		return accountManager.getAccountBySessionId(accountId, sessionId, lastActivity);
	}

	public String getDataPath(String accountId, String bucketId, String dataId) {
		return ioManager.getDataPath(accountId, bucketId, dataId);
	}

	public void createBucket(Bucket bucket, String accountId, String sessionId) throws AccountManagementException,
			IOManagementException {
		checkStr(bucket.getName(), "bucketName");
		checkStr(accountId, "accountId");
		checkStr(sessionId, "sessionId");

		ioManager.createBucketFolder(accountId, bucket.getName());
		try {
			accountManager.createBucket(bucket, accountId, sessionId);
		} catch (AccountManagementException e) {
			ioManager.deleteBucketFolder(accountId, bucket.getName());
			throw e;
		}
	}

	public String createObjectToBucket(String bucket, String accountId, String sessionId, ObjectItem object,
			InputStream fileData, String objectname, boolean parents) throws AccountManagementException,
			IOManagementException {
		checkStr(bucket, "bucket");
		checkStr(accountId, "accountId");
		checkStr(sessionId, "sessionId");
		checkStr(objectname, "objectname");
		checkObj(object, "object");

		String dataId = ioManager.createData(bucket, accountId, object, fileData, objectname, parents);
		logger.info(dataId);
		try {
			accountManager.createObjectToBucket(bucket, accountId, sessionId, object);
			return dataId;
		} catch (AccountManagementException e) {
			ioManager.deleteData(bucket, accountId, objectname);
			throw e;
		}
	}

	public String createFolderToBucket(String bucket, String accountId, String sessionId, ObjectItem object,
			String objectname, boolean parents) throws AccountManagementException, IOManagementException {
		checkStr(bucket, "bucket");
		checkStr(accountId, "accountId");
		checkStr(sessionId, "sessionId");
		checkStr(objectname, "objectname");
		checkObj(object, "object");

		String objectId = ioManager.createFolder(bucket, accountId, object, objectname, parents);
		logger.info(objectId);
		try {
			accountManager.createObjectToBucket(bucket, accountId, sessionId, object);
			return objectId;
		} catch (AccountManagementException e) {
			ioManager.deleteData(bucket, accountId, objectname);
			throw e;
		}
	}

	public void deleteDataFromBucket(String bucket, String accountId, String sessionId, String objectname)
			throws AccountManagementException, IOManagementException {
		checkStr(bucket, "bucket");
		checkStr(accountId, "accountId");
		checkStr(sessionId, "sessionId");
		checkStr(objectname, "objectname");

		String dataId = ioManager.deleteData(bucket, accountId, objectname);
		accountManager.deleteDataFromBucket(bucket, accountId, sessionId, dataId);
		logger.info(dataId);

	}

	public String region(String bucket, String accountId, String sessionId, String objectname, String regionStr,
			Map<String, List<String>> params) throws AccountManagementException, IOManagementException, IOException {

		checkStr(bucket, "bucket");
		checkStr(accountId, "accountId");
		checkStr(sessionId, "sessionId");
		checkStr(objectname, "objectname");

		String dataPath = ioManager.getDataPath(accountId, bucket, objectname);
		String dataId = objectname.replaceAll(":", "/");
		ObjectItem data = accountManager.getDataFromBucket(bucket, accountId, sessionId, dataId);
		checkStr(regionStr, "regionStr");
		Region region = Region.parseRegion(regionStr);
		checkObj(region, "region");

		String result = "";
		switch (data.getFileFormat()) {
		case "bam":
			Boolean viewAsPairs = false;
			if (params.get("view_as_pairs") != null) {
				viewAsPairs = true;
			}
			Boolean showSoftclipping = false;
			if (params.get("show_softclipping") != null) {
				showSoftclipping = true;
			}

			BamManager bamManager = new BamManager();
			result = bamManager.getByRegion(dataPath, region.getChromosome(), region.getStart(), region.getEnd(),
					viewAsPairs, showSoftclipping);

			break;
		}

		return result;
	}

	public String getJobResultFromBucket(String bucket, String accountId, String sessionId, String jobId)
			throws IOException, DocumentException {
		// TODO check all
		return ioManager.getJobResultFromBucket(bucket, accountId, sessionId, jobId);
	}

	public String getFileTableFromJob(String bucket, String accountId, String sessionId, String jobId, String filename,
			String start, String limit, String colNames, String colVisibility, String callback, String sort)
			throws IOManagementException, IOException, AccountManagementException {
		// TODO check all
		checkStr(bucket, "bucket");
		checkStr(accountId, "accountId");
		checkStr(sessionId, "sessionId");
		checkStr(filename, "filename");

		int first = Integer.parseInt(start);
		int end = first + Integer.parseInt(limit);
		String[] colnamesArray = colNames.split(",");
		String[] colvisibilityArray = colVisibility.split(",");

		return ioManager.getFileTableFromJob(bucket, accountId, sessionId, jobId, filename, first, end, colnamesArray,
				colvisibilityArray, callback, sort);
	}

	public DataInputStream getFileFromJob(String bucket, String accountId, String sessionId, String jobId,
			String filename, String zip) throws IOManagementException, IOException, AccountManagementException {
		// TODO check all
		checkStr(bucket, "bucket");
		checkStr(accountId, "accountId");
		checkStr(sessionId, "sessionId");
		checkStr(filename, "filename");
		checkStr(zip, "zip");

		return ioManager.getFileFromJob(bucket, accountId, sessionId, jobId, filename, zip);
	}

	public String getAccountBuckets(String accountId, String sessionId) throws AccountManagementException {
		return accountManager.getAllBucketsBySessionId(accountId, sessionId);
	}

	public String createJob(String jobName, String jobFolder, String bucket, String toolName, List<String> dataList,
			String commandLine, String sessionId) {
		return accountManager.createJob(jobName, jobFolder, bucket, toolName, dataList, commandLine, sessionId);
	}

	public String getJobFolder(String bucket, String jobId, String sessionId) {
		return accountManager.getJobFolder(bucket, jobId, sessionId);
	}

	public List<Plugin> getUserAnalysis(String sessionId) throws AccountManagementException {
		return accountManager.getUserAnalysis(sessionId);
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
		if (str == null || str.equals("")) {
			throw new AccountManagementException("parameter '" + name + "' is null or empty: " + str + ".");
		}
	}

	private void checkObj(Object obj, String name) throws AccountManagementException {
		if (obj == null) {
			throw new AccountManagementException("parameter '" + name + "' is null.");
		}
	}
}
