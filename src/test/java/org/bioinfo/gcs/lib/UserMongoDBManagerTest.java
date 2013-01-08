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
	
//	 @Test
//	 public void test2() throws FileNotFoundException, IOException {
//		 File propertiesFile = new File(System.getenv("GCSA_HOME") + "/conf/account.properties");
//		 properties.load(new FileInputStream(propertiesFile));
//		 ioManager = new FileIOManager(properties);
//		 System.out.println(ioManager.getObjectPath("pako","default","aaaaa").toString());
//	 }

}
