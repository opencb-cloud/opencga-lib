package org.bioinfo.gcsa.lib.users;

import java.io.File;
import java.io.IOException;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.gcsa.lib.users.persistence.UserManagementException;

public class IOManager {
	
	private String GCSA_ENV = System.getenv(CloudSessionManager.properties.getProperty("GCSA.ENV.PATH"));
	private String GCSA_ACCOUNT = GCSA_ENV+CloudSessionManager.properties.getProperty("GCSA.ACCOUNT.PATH");

	public void createScaffoldAccountId(String accountId)throws UserManagementException {
		
		if(!new File(GCSA_ACCOUNT).exists()){
			try {
				FileUtils.createDirectory(GCSA_ACCOUNT);
			} catch (IOException e1) {
				FileUtils.deleteDirectory(new File(GCSA_ACCOUNT));
				throw new UserManagementException("IOException" + e1.toString());
			}
		}
		
		System.out.println("---------------->>>>> PATHGCSA: " + GCSA_ACCOUNT);
		if (new File(GCSA_ACCOUNT).exists() && new File(GCSA_ACCOUNT).canWrite()
				&& new File(GCSA_ACCOUNT).canRead()
				&& new File(GCSA_ACCOUNT).canExecute()) {
			try {
				FileUtils.createDirectory(GCSA_ACCOUNT+ "/" + accountId);
				System.out.println("account creada");
			} catch (IOException e1) {
				FileUtils.deleteDirectory(new File(GCSA_ACCOUNT+ "/" + accountId));
				FileUtils.deleteDirectory(new File(GCSA_ACCOUNT));
				throw new UserManagementException("IOException" + e1.toString());
			}
			
			try {
				FileUtils.createDirectory(GCSA_ACCOUNT+ "/"  + accountId + "/analysis");
			} catch (IOException e1) {
				FileUtils.deleteDirectory(new File(GCSA_ACCOUNT+ "/"  + accountId + "/analysis"));
				FileUtils.deleteDirectory(new File(GCSA_ACCOUNT+ "/" + accountId));
				FileUtils.deleteDirectory(new File(GCSA_ACCOUNT));
				throw new UserManagementException("IOException" + e1.toString());
			}
			
			try {
				FileUtils.createDirectory(GCSA_ACCOUNT+ "/"  + accountId + "/projects");
			} catch (IOException e1) {
				FileUtils.deleteDirectory(new File(GCSA_ACCOUNT+ "/"  + accountId + "/projects"));
				FileUtils.deleteDirectory(new File(GCSA_ACCOUNT+ "/"  + accountId + "/analysis"));
				FileUtils.deleteDirectory(new File(GCSA_ACCOUNT+ "/" + accountId));
				FileUtils.deleteDirectory(new File(GCSA_ACCOUNT));
				throw new UserManagementException("IOException" + e1.toString());
			}
			
			try {
				FileUtils.createDirectory(GCSA_ACCOUNT+ "/"  + accountId + "/projects/default");
			} catch (IOException e1) {
				throw new UserManagementException("IOException" + e1.toString());
			}
			
			try {
				FileUtils.createDirectory(GCSA_ACCOUNT+ "/"  + accountId + "/projects/default/jobs");
			} catch (IOException e1) {
				FileUtils.deleteDirectory(new File(GCSA_ACCOUNT+ "/"  + accountId + "/projects/default/jobs"));
				FileUtils.deleteDirectory(new File(GCSA_ACCOUNT+ "/" + accountId));
				FileUtils.deleteDirectory(new File(GCSA_ACCOUNT));
				throw new UserManagementException("IOException" + e1.toString());
			}

		} else {
			throw new UserManagementException(
					"ERROR: The project has not been created ");
		}

	}
	
	public void createJobFolder(String accountId, String jobId)throws UserManagementException {
		String path = GCSA_ACCOUNT+ "/"  + accountId + "/jobs";
		if(new File(path).exists() && new File(path).canWrite()) {
			try {
				FileUtils.createDirectory(path + "/" + jobId);
			} catch (IOException e) {
				throw new UserManagementException("IOException" + e.toString());
			}
		}
	}
	
	public void removeJobFolder(String accountId, String jobId)throws UserManagementException {
		File path = new File(GCSA_ACCOUNT+ "/"  + accountId + "/jobs/"+ jobId);
		FileUtils.deleteDirectory(path);
	}

}
