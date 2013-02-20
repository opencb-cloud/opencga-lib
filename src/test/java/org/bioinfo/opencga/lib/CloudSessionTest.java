package org.bioinfo.opencga.lib;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.bioinfo.opencga.lib.account.db.AccountManager;
import org.bioinfo.opencga.lib.account.io.FileIOManager;
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
