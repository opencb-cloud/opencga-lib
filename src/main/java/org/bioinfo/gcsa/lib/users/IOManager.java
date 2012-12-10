package org.bioinfo.gcsa.lib.users;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.gcsa.lib.users.persistence.AccountManagementException;

public class IOManager {

	private Logger logger;
	private Properties properties;

	private String home;
	private String accounts;

	public IOManager(Properties properties) {
		logger = new Logger();
		logger.setLevel(Logger.INFO_LEVEL);
		this.properties = properties;
		home = System.getenv(properties.getProperty("GCSA.ENV.HOME"));
		accounts = home + properties.getProperty("GCSA.ACCOUNT.PATH");
	}

	public void createScaffoldAccountId(String accountId) throws AccountManagementException {

		if (!new File(accounts).exists()) {
			try {
				FileUtils.createDirectory(accounts);
			} catch (IOException e1) {
				FileUtils.deleteDirectory(new File(accounts));
				throw new AccountManagementException("IOException" + e1.toString());
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
				throw new AccountManagementException("IOException" + e1.toString());
			}

			try {
				FileUtils.createDirectory(accounts + "/" + accountId + "/analysis");
			} catch (IOException e1) {
				FileUtils.deleteDirectory(new File(accounts + "/" + accountId + "/analysis"));
				FileUtils.deleteDirectory(new File(accounts + "/" + accountId));
				FileUtils.deleteDirectory(new File(accounts));
				throw new AccountManagementException("IOException" + e1.toString());
			}

			try {
				FileUtils.createDirectory(accounts + "/" + accountId + "/projects");
			} catch (IOException e1) {
				FileUtils.deleteDirectory(new File(accounts + "/" + accountId + "/projects"));
				FileUtils.deleteDirectory(new File(accounts + "/" + accountId + "/analysis"));
				FileUtils.deleteDirectory(new File(accounts + "/" + accountId));
				FileUtils.deleteDirectory(new File(accounts));
				throw new AccountManagementException("IOException" + e1.toString());
			}

			try {
				FileUtils.createDirectory(accounts + "/" + accountId + "/projects/default");
			} catch (IOException e1) {
				throw new AccountManagementException("IOException" + e1.toString());
			}

			try {
				FileUtils.createDirectory(accounts + "/" + accountId + "/projects/default/jobs");
			} catch (IOException e1) {
				FileUtils.deleteDirectory(new File(accounts + "/" + accountId + "/projects/default/jobs"));
				FileUtils.deleteDirectory(new File(accounts + "/" + accountId));
				FileUtils.deleteDirectory(new File(accounts));
				throw new AccountManagementException("IOException" + e1.toString());
			}

		} else {
			throw new AccountManagementException("ERROR: The project has not been created ");
		}

	}

	public void createProjectFolder(String accountId, String project) throws AccountManagementException {
		try {
			System.out.println("--------------->     " + accounts + "/" + accountId + "/projects/" + project + "/jobs");
			FileUtils.createDirectory(accounts + "/" + accountId + "/projects/" + project);
			FileUtils.createDirectory(accounts + "/" + accountId + "/projects/" + project + "/jobs");
		} catch (IOException e1) {
			FileUtils.deleteDirectory(new File(accounts + "/" + accountId + "/projects/" + project + "/jobs"));
			throw new AccountManagementException("IOException" + e1.toString());
		}
	}

	public void createJobFolder(String accountId, String project, String jobId) throws AccountManagementException {
		String path = accounts + "/" + accountId + "/projects/" + project + "/jobs";
		if (new File(path).exists() && new File(path).canWrite()) {
			try {
				FileUtils.createDirectory(path + "/" + jobId);
			} catch (IOException e) {
				throw new AccountManagementException("IOException" + e.toString());
			}
		}
	}

	public void removeJobFolder(String accountId, String project, String jobId) throws AccountManagementException {
		File path = new File(accounts + "/" + accountId + "/projects/" + project + "/jobs/" + jobId);
		FileUtils.deleteDirectory(path);
	}

}
