package org.bioinfo.gcs.lib;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.bioinfo.gcsa.lib.users.CloudSessionManager;
import org.bioinfo.gcsa.lib.users.beans.Session;
import org.bioinfo.gcsa.lib.users.beans.Account;
import org.bioinfo.gcsa.lib.users.persistence.AccountManagementException;
import org.junit.Test;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

public class UserMongoDBManagerTest {
	
//	@Test
//	public void mailTest() throws UserManagementException, FileNotFoundException, IOException {
//		CloudSessionManager cloudSessionManager = new CloudSessionManager();
//		String email = "pakosala@gmail.com";
//		String accountId = "fsalavert";
//		cloudSessionManager.resetPassword(accountId, email);
//	}
	
//	@Test
//	public void createProject() {
//		String sessionId = "49vtbIxHqHGUA7KHM9Vz";
//		String accountId = "imedina";
//		for (int i = 0; i < 5; i++)
//			userManager.createProject(new Project("test" + i), accountId,
//					sessionId);
//	}

	@Test
	public void test() throws AccountManagementException, FileNotFoundException, IOException {
//		CloudSessionManager cloudSessionManager = new CloudSessionManager();
//		String accountId = "pako";
//		String accountName = "paco";
//		String password = "pepe";
//		String email = "pakosala@gmail.com";
//		String sessionIp ="0.0.0.0";
//		cloudSessionManager.createAccount(accountId, password, accountName, email, sessionIp);
		List<Session> sList = new ArrayList<Session>();
		sList.add(new Session());
		sList.add(new Session());
		sList.add(new Session());
		System.out.println(new Gson().toJson(sList));
	}
	
//	@Test
//	public void getUserBySessionIdTest() {
//		try {
//			CloudSessionManager cloudSessionManager = new CloudSessionManager(
//					System.getenv("GCSA_HOME"));
//			userManager = cloudSessionManager.getUserManager();
//			String sessionId = "OPq8ebQ3FQAPsVZ8cFAc";
////			 System.out.println(userManager.getAccountBySessionId(sessionId));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	@Test
	public void changePasswordTest() throws FileNotFoundException, IOException, AccountManagementException, NoSuchAlgorithmException {
//		CloudSessionManager cloudSessionManager = new CloudSessionManager();
//		String accountId = "fsalavert";
//		String sessionId = "zACSnUM25lVxdgbcUTTb";
//		String password = StringUtils.sha1("pepe");
//		String nPassword1 = StringUtils.sha1("pepe"); 
//		String nPassword2 = StringUtils.sha1("pepe");
//		cloudSessionManager.changePassword(accountId, sessionId, password, nPassword1, nPassword2);
	}
	@Test
	public void changeEmailTest() throws FileNotFoundException, IOException, AccountManagementException, NoSuchAlgorithmException {
//		CloudSessionManager cloudSessionManager = new CloudSessionManager();
//		String accountId = "fsalavert";
//		String sessionId = "zACSnUM25lVxdgbcUTTb";
//		String email = "pakosala@gmail.com";
//		cloudSessionManager.changeEmail(accountId, sessionId, email);
	}
//	@Test
//	public void createFileToProjectTest() {
//		if(new File("/home/examples/bam/HG00096.chrom20.ILLUMINA.bwa.GBR.exome.20111114.bam").exists()){
//			try {
////				CloudSessionManager cloudSessionManager = CloudSessionManager.getInstance();
//				CloudSessionManager cloudSessionManager = new CloudSessionManager();
//				userManager = cloudSessionManager.getUserManager();
//				
//				String sessionId = "qXJfMkhflb4GUhEV1GbJ";
//				String accountId = "fsalavert";
//				
//				System.out.println(sessionId);
//				File f = new File("/home/examples/bam/HG00096.chrom20.ILLUMINA.bwa.GBR.exome.20111114.bam");
//				InputStream fileData = new FileInputStream(f);
////				String projectName = null;
//				String projectName = "Default";
//				Data data = new Data();
////				"id" : "",
////				"type" : "",
//				String type = "bam";
////				"fileName" : "",
//				String fileName = f.getName();
////				"multiple" : "",
////				"diskUsage" : "",
//				String diskUsage = "1234321";
////				"creationTime" : "20121128153118",
////				"responsible" : "",
//				String responsible = "Paco";
////				"organization" : "",
//				String organization = "CIPF";
////				"date" : "",
////				"description" : "",
//				String description = "ILLUMINA CHR 20 BAM";
////				"status" : "",
////				"statusMessage" : "",
////				"members" : [ ]
//
//				data.setFileName(type);
//				data.setFileName(fileName);
//				data.setDiskUsage(diskUsage);
//				userManager.createDataToProject(projectName, accountId, sessionId, data, fileData);
//				
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}
	
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
