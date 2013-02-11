package org.bioinfo.gcs.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.commons.io.utils.IOUtils;
import org.bioinfo.gcsa.Config;
import org.bioinfo.gcsa.lib.account.beans.ObjectItem;

import org.bioinfo.gcsa.lib.account.db.AccountManagementException;
import org.bioinfo.gcsa.lib.account.db.AccountManager;
import org.bioinfo.gcsa.lib.account.io.FileIOManager;
import org.junit.Test;

public class CloudSessionTest {

	private AccountManager userManager;
	private FileIOManager ioManager;
	
	 @Test
	 public void test2() throws Exception {
		 ioManager = new FileIOManager();
		 System.out.println(ioManager.getObjectPath("pako","default",Paths.get("yeha.tar.gz")).toString());
		 
		 Path file = ioManager.getObjectPath("pako","default",Paths.get("yeha.tar.gz"));
//		 System.out.println();
		 
		Path relative = Paths.get("one","two").relativize(Paths.get("one","two","three","four.txt"));
		System.out.println(relative.toString());
	 }
}
