package org.bioinfo.opencga.lib;

import org.bioinfo.opencga.lib.storage.XObject;
import org.bioinfo.opencga.lib.storage.datamanagers.GffManager;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

public class GffManagerTest {
    String USER_HOME = System.getProperty("user.home");


    @Test
    public void testQueryByOffset() throws IOException, SQLException, ClassNotFoundException {
        Path regulatoryRegionPath = Paths.get(USER_HOME, "cellbase_v3", "hsapiens", "genomic_regulatory_region");
        Path filePath = regulatoryRegionPath.resolve("AnnotatedFeatures.gff");

        Path dbPath = Paths.get(filePath.toString() + ".sqlite3");
        if(Files.exists(dbPath)){
            Files.delete(dbPath);
        }

        GffManager gffManager = new GffManager();
        gffManager.createIndex(filePath);
    }

    @Test
    public void testIterateXobject() {
        StringBuilder sbQuery = new StringBuilder();

        XObject xo = new XObject();
        xo.put("a", "1");
        xo.put("f","23");
        xo.put("b", "2");

        Set<String> set = xo.keySet();
        for (String s : set) {
            System.out.println(s);
        }

        for (Map.Entry<String, Object> entry : xo.entrySet()) {
            String key = entry.getKey();
            int value  = xo.getInt(key);
            System.out.println(value);
        }


    }
    @Test
    public void testUpdate() {

        StringBuilder sbQuery = new StringBuilder();
        String tableName="asdf_hgfgfds";

        XObject xo = new XObject();
        xo.put("a", "1");
        xo.put("f","23");
        xo.put("b", "2");

        XObject xo2 = new XObject();
        xo2.put("a", "1");
        xo2.put("f","23");
        xo2.put("t", "2");
        xo2.put("s", "2");
        xo2.put("y", "2");

        sbQuery.append("UPDATE " + tableName + " SET ");
        Set<String> updateColumns = xo.keySet();
        for (String colName : updateColumns) {
            sbQuery.append("'" + colName + "'=?, ");
        }
        sbQuery.delete(sbQuery.length() - 2, sbQuery.length());//", ".length()
        sbQuery.append(" WHERE ");
        Set<String> whereColumns = xo2.keySet();
        for (String colName : whereColumns) {
            sbQuery.append("'" + colName + "'=? AND ");
        }
        sbQuery.delete(sbQuery.length() - 5, sbQuery.length());//" AND ".length()

        System.out.println(sbQuery.toString());

    }

}
