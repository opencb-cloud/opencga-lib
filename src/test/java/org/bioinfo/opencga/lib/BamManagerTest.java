package org.bioinfo.opencga.lib;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.gson.*;
import org.bioinfo.opencga.lib.account.CloudSessionManager;
import org.bioinfo.opencga.lib.account.db.AccountManagementException;
import org.bioinfo.opencga.lib.account.io.IOManagementException;
import org.bioinfo.opencga.lib.storage.XObject;
import org.bioinfo.opencga.lib.storage.datamanagers.GffManager;
import org.bioinfo.opencga.lib.storage.datamanagers.bam.BamManager;
import org.bioinfo.opencga.lib.storage.indices.SqliteManager;
import org.bioinfo.opencga.lib.utils.Config;
import org.junit.Test;

public class BamManagerTest {
    static CloudSessionManager cloudSessionManager;
    static {
        try {
            cloudSessionManager = new CloudSessionManager();
        } catch (IOException | IOManagementException e) {
            e.printStackTrace();
        }
    }

    String USER_HOME = System.getProperty("user.home");
//	@Test
	public void test() throws IOManagementException {
		if(new File("/home/examples").exists()){
			try {
				BamManager bu = new BamManager();
				Path file = Paths.get("home","examples","bam","HG00096.chrom20.ILLUMINA.bwa.GBR.exome.20111114.bam");
				Map<String, List<String>> params = new HashMap<String, List<String>> ();
//				view_as_pairs
//				show_softclipping
				params.put("show_softclipping", Arrays.asList("true"));
				String result = bu.getByRegion(file,"20:32875000-32879999", params );
//			String result = bu.getByRegion("/home/examples","out_sorted", "1", 90604245, 93604245);
//			System.out.println(result);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			System.out.println("/home/examples not found, skip.");
		}
	}
    @Test
    public void testQueryDBIndex() throws IOException, SQLException, ClassNotFoundException {

        Path indexerPath = Paths.get(USER_HOME, "appl", "opencga-lib", "opencga-cloud","analysis","indexer");
        Path filePath = indexerPath.resolve("HG00096.chrom20.ILLUMINA.bwa.GBR.exome.20111114.bam");

        Path dbPath = Paths.get(filePath.toString() + ".db");


        if(Files.exists(dbPath)){
            BamManager bamManager = new BamManager();
            List<XObject> a = bamManager.queryRegion(filePath, "20", 32870000, 32870001);
            for(XObject r :a){
                System.out.println(r.getString("id"));
            }

        }else{
            System.out.println("no db file found");
        }

    }

    @Test
    public void test2() throws IOException, SQLException, ClassNotFoundException {
        SqliteManager sqliteManager = new SqliteManager();
        Path indexerPath = Paths.get(USER_HOME, "appl", "opencga-lib", "opencga-cloud","analysis","indexer");
        Path filePath = indexerPath.resolve("aaaaaa");
        Files.delete(Paths.get(filePath.toString()+".db"));
        sqliteManager.connect(filePath);
        sqliteManager.createTable("a", new XObject("a", "TEXT"));
        sqliteManager.insert(new XObject("a", "1"), "a");
        sqliteManager.insert(new XObject("a", "1"), "a");
        sqliteManager.disconnect(true);
    }
}
