package org.bioinfo.gcsa.lib.users;

import java.io.File;
import java.io.IOException;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.gcsa.lib.users.persistence.UserManagementException;

public class IOManager {
	private Runtime execute = Runtime.getRuntime();
	private Process process;
	private String pathGCSA = "/home/echirivella/TESTGCSA/";

	public void createScaffoldAccountId(String accountId)
			throws UserManagementException {
		
		
		if (new File(pathGCSA).exists() && new File(pathGCSA).canWrite()
				&& new File(pathGCSA).canRead()
				&& new File(pathGCSA).canExecute()) {
			try {
				FileUtils.createDirectory(pathGCSA + accountId + "/jobs");
			} catch (IOException e1) {
				throw new UserManagementException("IOException" + e1.toString());
			}

			try {
				FileUtils.createDirectory(pathGCSA + accountId + "/plugins");
			} catch (IOException e1) {
				throw new UserManagementException("IOException" + e1.toString());
			}

		} else {
			throw new UserManagementException(
					"ERROR: The project has not been created ");
		}

	}

}
