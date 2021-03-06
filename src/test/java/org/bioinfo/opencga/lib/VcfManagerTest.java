package org.bioinfo.opencga.lib;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bioinfo.opencga.lib.account.io.IOManagementException;
import org.bioinfo.opencga.lib.storage.XObject;
import org.bioinfo.opencga.lib.storage.datamanagers.VcfManager;
import org.junit.Test;

public class VcfManagerTest {
    String USER_HOME = System.getProperty("user.home");

//	@Test
//	public void test() throws IOManagementException {
//		if(new File("/home/examples").exists()){
//			try {
//				VcfManager man = new VcfManager();
//				Path file = Paths.get("/home","examples","vcf","1000genomes_5000_variants.vcf.gz");
//				Map<String, List<String>> params = new HashMap<String, List<String>> ();
//				String result = man.getByRegion(file,"19:246522-246722", params );
//				System.out.println(result);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}else{
//			System.out.println("/home/examples not found, skip.");
//		}
//	}

    @Test
    public void testQueryDBIndex() throws IOException, SQLException, ClassNotFoundException {
        Path filePath = Paths.get(USER_HOME, "..", "examples", "vcf", "1000genomes_5000_variants.vcf");

        VcfManager vcfManager = new VcfManager();
        Map<String, List<String>> params = new HashMap<String, List<String>>();
        System.out.println(vcfManager.queryRegion(filePath, "19:80840-88840", params));
    }

}
