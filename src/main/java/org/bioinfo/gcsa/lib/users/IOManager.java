package org.bioinfo.gcsa.lib.users;

import java.io.File;
import java.io.IOException;
import java.org.bioinfo.gcsa.lib.users.persistence.UserManagementException;

import org.bioinfo.commons.io.utils.FileUtils;


public class IOManager {
	private Runtime execute = Runtime.getRuntime();
	private Process process;
	private String pathGCSA = "/home/echirivella/TESTGCSA"; //Esto fichero propiedades

	public void createProject(String accountId) throws UserManagementException{
		try {
			FileUtils.checkFile(new File(pathGCSA + accountId));
		} catch (IOException e2) {
			new UserManagementException("The Project exits");
		}

		try {
			createFolder(pathGCSA + accountId + "jobs");
		} catch (InterruptedException e1) {
			new UserManagementException("The thread is interrupted ");
		} catch (IOException e1) {
			new UserManagementException("IOException");//TODO
		}

		try {
			createFolder(pathGCSA + accountId + "plugins");
		} catch (InterruptedException e1) {
			new UserManagementException("The thread is interrupted ");
		} catch (IOException e1) {
			new UserManagementException("IOException");//TODO
		}
	}

	private void createFolder(String path) throws InterruptedException,
			IOException {
		StringBuilder commandToExecute = new StringBuilder("mkdir -p " + path);
		String[] command = { "/bin/bash", "-c", commandToExecute.toString() };

		process = execute.exec(command);

		process.waitFor();

		if (process.exitValue() != 0) {

		}
	}
}
