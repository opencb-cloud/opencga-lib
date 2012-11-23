package org.bioinfo.gcs.lib;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.bioinfo.gcsa.lib.users.CloudSessionManager;
import org.bioinfo.gcsa.lib.users.persistence.UserManager;
import org.junit.Test;

public class UserMongoDBManaTest {
	private UserManager userManager;
	@Test
	public void createFileToProjectTest() {
		try {
			CloudSessionManager cloudSessionManager = new CloudSessionManager(System.getenv("GCSA_HOME"));
			userManager = cloudSessionManager.userManager;
			
			String sessionId = "oRO0Z0N1EWhRUH2gJxrL";
			String fileName = "datos_experimento.txt";
			String data = "sampletext";
			InputStream fileData = new ByteArrayInputStream(data.getBytes("UTF-8"));  
			
			userManager.createFileToProject("Default", fileName, fileData, sessionId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void createJobTest() {
		try {
			CloudSessionManager cloudSessionManager = new CloudSessionManager(System.getenv("GCSA_HOME"));
			userManager = cloudSessionManager.userManager;
			
			String sessionId = "z5ZhOVqKkyypCMrYIgXS";
			
			userManager.createJob("", "", new ArrayList<String>(), sessionId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
