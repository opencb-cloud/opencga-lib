package org.bioinfo.gcsa.lib.users;

import java.io.File;
import java.io.IOException;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.gcsa.lib.users.persistence.UserManagementException;

public class IOManager {
	private String pathGCSA = CloudSessionManager.properties.getProperty("GCSA.USERS.PATH");

	public void createScaffoldAccountId(String accountId)
			throws UserManagementException {
		
		System.out.println("---------------->>>>> PATHGCSA: " + pathGCSA);
		if (new File(pathGCSA).exists() && new File(pathGCSA).canWrite()
				&& new File(pathGCSA).canRead()
				&& new File(pathGCSA).canExecute()) {
			try {
				FileUtils.createDirectory(pathGCSA + accountId);
				System.out.println("account creada");
			} catch (IOException e1) {
				throw new UserManagementException("IOException" + e1.toString());
			}
			
			try {
				FileUtils.createDirectory(pathGCSA + accountId + "/jobs");
			} catch (IOException e1) {
				throw new UserManagementException("IOException" + e1.toString());
			}

			try {
				FileUtils.createDirectory(pathGCSA + accountId + "/analysis");
			} catch (IOException e1) {
				throw new UserManagementException("IOException" + e1.toString());
			}
			
			try {
				FileUtils.createDirectory(pathGCSA + accountId + "/projects");
			} catch (IOException e1) {
				throw new UserManagementException("IOException" + e1.toString());
			}
			
			try {
				FileUtils.createDirectory(pathGCSA + accountId + "/projects/default");
			} catch (IOException e1) {
				throw new UserManagementException("IOException" + e1.toString());
			}

		} else {
			throw new UserManagementException(
					"ERROR: The project has not been created ");
		}

	}

}
