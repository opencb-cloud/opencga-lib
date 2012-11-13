package org.bioinfo.gcsa.lib.users;

import java.io.File;
import java.io.IOException;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.gcsa.lib.users.persistence.UserManagementException;

public class IOManager {
	private Runtime execute = Runtime.getRuntime();
	private Process process;
	private String pathGCSA = "/home/echirivella/TESTGCSA/";

	public void createAccountId(String accountId)
			throws UserManagementException {
		if (new File(pathGCSA).exists() && new File(pathGCSA).canWrite()
				&& new File(pathGCSA).canRead()
				&& new File(pathGCSA).canExecute()) {
			try {
				createAccountFolder(pathGCSA + accountId + "/jobs");
			} catch (InterruptedException e1) {
				throw new UserManagementException("The thread is interrupted "
						+ e1.toString());
			} catch (IOException e1) {
				throw new UserManagementException("IOException" + e1.toString());
			}

			try {
				createAccountFolder(pathGCSA + accountId + "/plugins");
			} catch (InterruptedException e1) {
				throw new UserManagementException("The thread is interrupted "
						+ e1.toString());
			} catch (IOException e1) {
				throw new UserManagementException("IOException" + e1.toString());
			}

		} else {
			throw new UserManagementException(
					"ERROR: The project has not been created ");
		}

	}

	private void createAccountFolder(String path) throws InterruptedException,
			IOException {
		StringBuilder commandToExecute = new StringBuilder("mkdir -p " + path);
		System.out.println(commandToExecute);
		String[] command = { "/bin/bash", "-c", commandToExecute.toString() };

		process = execute.exec(command);

		process.waitFor();

		if (process.exitValue() != 0) {
			System.out.println("HEMOS SALIDO CON " + process.exitValue());
		}
	}
}
