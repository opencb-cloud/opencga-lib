package org.bioinfo.gcs.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.bioinfo.gcsa.lib.users.CloudSessionManager;
import org.bioinfo.gcsa.lib.users.beans.Project;
import org.bioinfo.gcsa.lib.users.persistence.UserManager;
import org.junit.Test;

public class UserMongoDBManagerTest {
	private UserManager userManager;
	
	@Test
	public void createProject() {
		String sessionId = "49vtbIxHqHGUA7KHM9Vz";
		String accountId = "imedina";
		for (int i = 0; i < 5; i++)
			userManager.createProject(new Project("test" + i), accountId,
					sessionId);
	}

	@Test
	public void getUserBySessionIdTest() {
		try {
			CloudSessionManager cloudSessionManager = new CloudSessionManager(
					System.getenv("GCSA_HOME"));
			userManager = cloudSessionManager.getUserManager();
			String sessionId = "OPq8ebQ3FQAPsVZ8cFAc";
//			 System.out.println(userManager.getAccountBySessionId(sessionId));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createFileToProjectTest() {
		if(new File("/home/examples/bam/HG00096.chrom20.ILLUMINA.bwa.GBR.exome.20111114.bam").exists()){
			try {
				CloudSessionManager cloudSessionManager = new CloudSessionManager(System.getenv("GCSA_HOME"));
				userManager = cloudSessionManager.getUserManager();
				
				String sessionId = "puflZ9VhY7WW1xU6D469";
				File f = new File("/home/examples/bam/HG00096.chrom20.ILLUMINA.bwa.GBR.exome.20111114.bam");
				String data = "sampletext";
				InputStream fileData = new FileInputStream(f);
				userManager.createFileToProject("Default", f.getName(), fileData, sessionId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	// @Test
	// public void loginTest() {
	// }

	// @Test
	// public void createTest() {
	// try {
	// CloudSessionManager cloudSessionManager = new
	// CloudSessionManager(System.getenv("GCSA_HOME"));
	// userManager = cloudSessionManager.userManager;
	//
	// String accountId = "fsalavert";
	// String password = "hola";
	// Session session = new Session();
	//
	// System.out.println(userManager.login(accountId, password, session));
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	// @Test
	// public void createJobTest() {
	// try {
	// CloudSessionManager cloudSessionManager = new
	// CloudSessionManager(System.getenv("GCSA_HOME"));
	// userManager = cloudSessionManager.getUserManager();
	//
	// String sessionId = "JLymlrv3eWm5jIjAVwty";
	//
	// userManager.createJob("", null, "", new ArrayList<String>(), "",
	// sessionId);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

//	@Test
//	public void getUserBySessionIdTest() {
//		try {
//			CloudSessionManager cloudSessionManager = new CloudSessionManager(System.getenv("GCSA_HOME"));
//			userManager = cloudSessionManager.userManager;
//			
//			String sessionId = "vXdrMs4CrKQcq2GueyMA";
//			System.out.println(userManager.getAccountBySessionId(sessionId));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

//	@Test
//	public void loginTest() {
//	}
	
//	@Test
//	public void createTest() {
//			try {
//				CloudSessionManager cloudSessionManager = new CloudSessionManager(System.getenv("GCSA_HOME"));
//				userManager = cloudSessionManager.userManager;
//				
//				String accountId = "fsalavert";
//				String password = "hola";
//				Session session = new Session();
//				
//				System.out.println(userManager.login(accountId, password, session));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//	}

}
