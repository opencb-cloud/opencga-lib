package org.bioinfo.gcsa.lib.account.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.commons.io.utils.IOUtils;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.gcsa.lib.account.beans.Data;

public class IOManager {

	private Logger logger;
	private Properties properties;

	private String home;
	private String accounts;
	private String tmp;

	public IOManager(Properties properties) {
		logger = new Logger();
		logger.setLevel(Logger.INFO_LEVEL);
		this.properties = properties;
		home = System.getenv(properties.getProperty("GCSA.ENV.HOME"));
		accounts = home + properties.getProperty("GCSA.ACCOUNT.PATH");
		tmp = properties.getProperty("TMP.PATH");
	}

	public void createScaffoldAccountId(String accountId) throws IOManagementException {

		if (!new File(accounts).exists()) {
			try {
				FileUtils.createDirectory(accounts);
			} catch (IOException e1) {
				FileUtils.deleteDirectory(new File(accounts));
				throw new IOManagementException("IOException" + e1.toString());
			}
		}

		System.out.println("---------------->>>>> PATHGCSA: " + accounts);
		if (new File(accounts).exists() && new File(accounts).canWrite() && new File(accounts).canRead()
				&& new File(accounts).canExecute()) {
			try {
				FileUtils.createDirectory(accounts + "/" + accountId);
				logger.info("account scaffold created");
			} catch (IOException e1) {
				FileUtils.deleteDirectory(new File(accounts + "/" + accountId));
				FileUtils.deleteDirectory(new File(accounts));
				throw new IOManagementException("IOException" + e1.toString());
			}

			try {
				FileUtils.createDirectory(accounts + "/" + accountId + "/analysis");
			} catch (IOException e1) {
				FileUtils.deleteDirectory(new File(accounts + "/" + accountId + "/analysis"));
				FileUtils.deleteDirectory(new File(accounts + "/" + accountId));
				FileUtils.deleteDirectory(new File(accounts));
				throw new IOManagementException("IOException" + e1.toString());
			}

			try {
				FileUtils.createDirectory(accounts + "/" + accountId + "/buckets");
			} catch (IOException e1) {
				FileUtils.deleteDirectory(new File(accounts + "/" + accountId + "/buckets"));
				FileUtils.deleteDirectory(new File(accounts + "/" + accountId + "/analysis"));
				FileUtils.deleteDirectory(new File(accounts + "/" + accountId));
				FileUtils.deleteDirectory(new File(accounts));
				throw new IOManagementException("IOException" + e1.toString());
			}

			try {
				FileUtils.createDirectory(accounts + "/" + accountId + "/buckets/default");
			} catch (IOException e1) {
				throw new IOManagementException("IOException" + e1.toString());
			}

			try {
				FileUtils.createDirectory(accounts + "/" + accountId + "/buckets/default/jobs");
			} catch (IOException e1) {
				FileUtils.deleteDirectory(new File(accounts + "/" + accountId + "/buckets/default/jobs"));
				FileUtils.deleteDirectory(new File(accounts + "/" + accountId));
				FileUtils.deleteDirectory(new File(accounts));
				throw new IOManagementException("IOException" + e1.toString());
			}

		} else {
			throw new IOManagementException("ERROR: The bucket has not been created ");
		}

	}

	public void createBucketFolder(String accountId, String bucket) throws IOManagementException {
		try {
			System.out.println("--------------->     " + accounts + "/" + accountId + "/buckets/" + bucket + "/jobs");
			FileUtils.createDirectory(accounts + "/" + accountId + "/buckets/" + bucket);
			FileUtils.createDirectory(accounts + "/" + accountId + "/buckets/" + bucket + "/jobs");
		} catch (IOException e1) {
			FileUtils.deleteDirectory(new File(accounts + "/" + accountId + "/buckets/" + bucket + "/jobs"));
			throw new IOManagementException("IOException" + e1.toString());
		}
	}

	public void createJobFolder(String accountId, String bucket, String jobId) throws IOManagementException {
		String path = accounts + "/" + accountId + "/buckets/" + bucket + "/jobs";
		if (new File(path).exists() && new File(path).canWrite()) {
			try {
				FileUtils.createDirectory(path + "/" + jobId);
			} catch (IOException e) {
				throw new IOManagementException("IOException" + e.toString());
			}
		}
	}

	public void removeJobFolder(String accountId, String bucket, String jobId) throws IOManagementException {
		File path = new File(accounts + "/" + accountId + "/buckets/" + bucket + "/jobs/" + jobId);
		FileUtils.deleteDirectory(path);
	}

	public String createData(String bucket, String accountId, Data data, InputStream fileData, String objectname,
			boolean parents) throws IOManagementException {
		String idStr = objectname.replace(":", "/");
		String fileName = getDataName(idStr);

		// CREATING A RANDOM TEMP FOLDER
		File rndFolder = null;
		String randomFolder = tmp + "/" + StringUtils.randomString(20);
		File tmpFile = new File(randomFolder + "/" + fileName);

		String userFileStr = getBucketPath(accountId, bucket) + "/" + idStr;
		File userFile = new File(userFileStr);

		// if parents its
		// true, folders
		// will be
		// autocreated
		logger.info("IOManager: "+tmpFile.getAbsolutePath());
		logger.info("IOManager: "+userFile.getAbsolutePath());
		if (!parents && !userFile.getParentFile().exists()) {
			throw new IOManagementException("no such folder");
		}

		if (userFile.exists()) {
			userFileStr = renameExistingFile(userFileStr);
			userFile = new File(userFileStr);
			idStr = userFileStr.replace(getBucketPath(accountId, bucket) + "/", "");
			fileName = getDataName(idStr);
		}

		data.setId(idStr);
		data.setFileName(fileName);

		logger.info(tmpFile.getAbsolutePath());

		try {
			FileUtils.createDirectory(randomFolder);
			rndFolder = new File(randomFolder);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOManagementException("Could not create the upload temp directory");
		}
		// COPYING TO DISK

		try {
			IOUtils.write(tmpFile, fileData);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOManagementException("Could not write the file on disk");
		}
		// COPYING FROM TEMP TO ACCOUNT DIR
		try {
			logger.info(userFile.getAbsolutePath());
			FileUtils.touch(userFile);
			FileUtils.copy(tmpFile, userFile);
			data.setDiskUsage(Long.toString(userFile.length()));

		} catch (IOException e) {
			e.printStackTrace();
			throw new IOManagementException("Copying from tmp folder to account folder");
		}
		FileUtils.deleteDirectory(rndFolder);
		return idStr;
	}

	public String deleteData(String bucket, String accountId, String objectname) {
		String idStr = objectname.replace(":", "/");
		String userFileStr = getBucketPath(accountId, bucket) + "/" + idStr;
		logger.info("IOManager: "+userFileStr);
		logger.info(userFileStr);
		FileUtils.deleteDirectory(new File(userFileStr));
		return idStr;
	}

	private String getAccountPath(String accountId) {
		return accounts + "/" + accountId;
	}

	private String getBucketPath(String accountId, String bucketId) {
		return getAccountPath(accountId) + "/buckets/" + bucketId.toLowerCase();
	}

	// public String getDataPath(String wsDataId){
	// wsDataId.replaceAll(":", "/")
	// }
	public String getDataPath(String accountId, String bucketId, String dataId) {
		dataId = dataId.replaceAll(":", "/");
		return getBucketPath(accountId, bucketId) + "/" + dataId;
	}

	private String renameExistingFile(String name) {
		File file = new File(name);
		if (file.exists()) {
			String fileName = FileUtils.removeExtension(name);
			String fileExt = FileUtils.getExtension(name);
			String newname = null;
			if (fileName != null && fileExt != null) {
				newname = fileName + "-copy" + fileExt;
			} else {
				newname = name + "-copy";
			}
			return renameExistingFile(newname);
		} else {
			return name;
		}
	}

	private String getDataName(String objectname) {
		String[] tokens = objectname.split("/");
		return tokens[(tokens.length - 1)];
	}
}
