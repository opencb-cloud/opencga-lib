package org.bioinfo.gcs.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.commons.io.utils.IOUtils;
import org.bioinfo.gcsa.lib.account.beans.ObjectItem;

import org.bioinfo.gcsa.lib.account.db.AccountManagementException;
import org.bioinfo.gcsa.lib.account.db.AccountManager;
import org.bioinfo.gcsa.lib.account.io.FileIOManager;
import org.junit.Test;

public class UserMongoDBManagerTest {

	private AccountManager userManager;
	private FileIOManager ioManager;
	private Properties properties;
	
	 @Test
	 public void test2() throws FileNotFoundException, IOException {
		 File propertiesFile = new File(System.getenv("GCSA_HOME") + "/conf/account.properties");
		 properties.load(new FileInputStream(propertiesFile));
		 ioManager = new FileIOManager(properties);
		 System.out.println(ioManager.getObjectPath("pako","default","aaaaa").toString());
	 }
	
	// @Test
	// public void createProject() {
	// CloudSessionManager cloudSessionManager;
	// try {
	// System.out.println(System.getenv("GCSA_HOME"));
	//
	// cloudSessionManager = new CloudSessionManager(
	// System.getenv("GCSA_HOME"));
	//
	// userManager = cloudSessionManager.getUserManager();
	// String sessionId = "uJIsS1JNzBdeUEcE0mjb";
	// String accountId = "echirivella";
	// for (int i = 0; i < 5; i++)
	// userManager.createProject(new Project("test" + i), accountId,
	// sessionId);
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (UserManagementException e) {
	// e.printStackTrace();
	// }
	//
	// }

	@Test
	public void createData() {
		// String project = "default";
		// CloudSessionManager cloudSessionManager;
		// try {
		// System.out.println(System.getenv("GCSA_HOME"));
		//
		// cloudSessionManager = new CloudSessionManager(
		// System.getenv("GCSA_HOME"));
		//
		// userManager = cloudSessionManager.getUserManager();
		// String sessionId = "p9adms8g7tiAO65OSQPt";
		// for (int i = 0; i < 5; i++)
		// userManager.createData("test" + i, project, sessionId);
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// } catch (UserManagementException e) {
		// e.printStackTrace();
		// }
	}

	// @Test
	// public void getUserBySessionIdTest() {
	// try {
	// CloudSessionManager cloudSessionManager = new CloudSessionManager(
	// System.getenv("GCSA_HOME"));
	// userManager = cloudSessionManager.getUserManager();
	// String sessionId = "OPq8ebQ3FQAPsVZ8cFAc";
	// // System.out.println(userManager.getAccountBySessionId(sessionId));
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	// public void createFileToProjectTest() {
	// if(new
	// File("/home/examples/bam/HG00096.chrom20.ILLUMINA.bwa.GBR.exome.20111114.bam").exists()){
	// try {
	// CloudSessionManager cloudSessionManager = new
	// CloudSessionManager(System.getenv("GCSA_HOME"));
	// userManager = cloudSessionManager.getUserManager();
	//
	// String sessionId = "puflZ9VhY7WW1xU6D469";
	// File f = new
	// File("/home/examples/bam/HG00096.chrom20.ILLUMINA.bwa.GBR.exome.20111114.bam");
	// String data = "sampletext";
	// InputStream fileData = new FileInputStream(f);
	// userManager.createFileToProject("Default", f.getName(), fileData,
	// sessionId);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	//
	// }

	// @Test
	// public void mailTest() throws UserManagementException,
	// FileNotFoundException, IOException {
	// CloudSessionManager cloudSessionManager = new CloudSessionManager();
	// String email = "pakosala@gmail.com";
	// String accountId = "fsalavert";
	// cloudSessionManager.resetPassword(accountId, email);
	// }

	// @Test
	// public void createProject() {
	// String sessionId = "49vtbIxHqHGUA7KHM9Vz";
	// String accountId = "imedina";
	// for (int i = 0; i < 5; i++)
	// userManager.createProject(new Project("test" + i), accountId,
	// sessionId);
	// }

	@Test
	public void test() throws AccountManagementException, FileNotFoundException, IOException {
		// String accountId = "pako";
		// String accountName = "paco";
		// String password = "pepe";
		// String email = "pakosala@gmail.com";
		// String sessionIp ="0.0.0.0";
		// cloudSessionManager.createAccount(accountId, password, accountName,
		// email, sessionIp);
		// List<Session> sList = new ArrayList<Session>();
		// sList.add(new Session());
		// sList.add(new Session());
		// sList.add(new Session());
		// System.out.println(new Gson().toJson(sList));
	}

	// @Test
	// public void getUserBySessionIdTest() {
	// try {
	// System.getenv("GCSA_HOME"));
	// userManager = cloudSessionManager.getUserManager();
	// String sessionId = "OPq8ebQ3FQAPsVZ8cFAc";
	// // System.out.println(userManager.getAccountBySessionId(sessionId));
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	@Test
	public void changePasswordTest() throws FileNotFoundException, IOException, AccountManagementException,
			NoSuchAlgorithmException {
		// String accountId = "fsalavert";
		// String sessionId = "zACSnUM25lVxdgbcUTTb";
		// String password = StringUtils.sha1("pepe");
		// String nPassword1 = StringUtils.sha1("pepe");
		// String nPassword2 = StringUtils.sha1("pepe");
		// cloudSessionManager.changePassword(accountId, sessionId, password,
		// nPassword1, nPassword2);
	}

	@Test
	public void changeEmailTest() throws FileNotFoundException, IOException, AccountManagementException,
			NoSuchAlgorithmException {
		// String accountId = "fsalavert";
		// String sessionId = "zACSnUM25lVxdgbcUTTb";
		// String email = "pakosala@gmail.com";
		// cloudSessionManager.changeEmail(accountId, sessionId, email);
	}

	// @Test
	public void createFileToProjectTest() {
		if (new File("/home/examples/bam/HG00096.chrom20.ILLUMINA.bwa.GBR.exome.20111114.bam").exists()) {
			try {
				String sessionId = "qXJfMkhflb4GUhEV1GbJ";
				String accountId = "fsalavert";

				System.out.println(sessionId);
				File f = new File("/home/examples/bam/HG00096.chrom20.ILLUMINA.bwa.GBR.exome.20111114.bam");
				InputStream fileData = new FileInputStream(f);
				// String projectName = null;
				String projectName = "Default";
//				ObjectItem data = new ObjectItem();
//				// "id" : "",
//				// "type" : "",
//				String type = "bam";
//				// "fileName" : "",
//				String fileName = f.getName();
//				// "multiple" : "",
//				// "diskUsage" : "",
//				String diskUsage = "1234321";
//				// "creationTime" : "20121128153118",
//				// "responsible" : "",
//				String responsible = "Paco";
//				// "organization" : "",
//				String organization = "CIPF";
//				// "date" : "",
//				// "description" : "",
//				String description = "ILLUMINA CHR 20 BAM";
//				// "status" : "",
//				// "statusMessage" : "",
//				// "members" : [ ]
//
//				data.setFileName(type);
//				data.setFileName(fileName);
//				data.setDiskUsage(diskUsage);
				// cloudSessionManager.createDataToProject(projectName,
				// accountId, sessionId, data, fileData, "");

				fileData.close();
			} catch (Exception e) {
				System.out.println(e);
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

	// @Test
	// public void getUserBySessionIdTest() {
	// try {
	// CloudSessionManager cloudSessionManager = new
	// CloudSessionManager(System.getenv("GCSA_HOME"));
	// userManager = cloudSessionManager.userManager;
	//
	// String sessionId = "vXdrMs4CrKQcq2GueyMA";
	// System.out.println(userManager.getAccountBySessionId(sessionId));
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

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



//	@Test
//	public void test1() throws IOException {
//		String filename = "/tmp/hola.txt";
////		filename = renameExistingFile(filename);
//		System.out.println(filename);
//		FileUtils.touch(new File(filename));
//	}

}
