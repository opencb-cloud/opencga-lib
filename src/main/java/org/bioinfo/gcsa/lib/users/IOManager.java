package org.bioinfo.gcsa.lib.users;

import java.io.File;
import java.io.IOException;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.gcsa.lib.users.persistence.UserManagementException;


public class IOManager {
	private Runtime execute = Runtime.getRuntime();
	private Process process;
	private String pathGCSA = "/home/echirivella/TESTGCSA/";

	public void createAccountId(String accountId) throws UserManagementException{
		try {
			FileUtils.checkFile(new File(pathGCSA + accountId));
		} catch (IOException e2) {
			new UserManagementException("The Project exits");
		}

		try {
			System.out.println("Puedo escribir? " + new File(pathGCSA).canWrite());
			System.out.println("Puedo ejecutar? " + new File(pathGCSA).canExecute());
			System.out.println("Puedo leer? " + new File(pathGCSA).canRead());
			createAccountFolder(pathGCSA + accountId + "/jobs");
		} catch (InterruptedException e1) {
			new UserManagementException("The thread is interrupted ");
		} catch (IOException e1) {
			new UserManagementException("IOException");//TODO
		}

		try {
			createAccountFolder(pathGCSA + accountId + "/plugins");
		} catch (InterruptedException e1) {
			new UserManagementException("The thread is interrupted ");
		} catch (IOException e1) {
			new UserManagementException("IOException");//TODO
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
