package org.bioinfo.opencga.lib;

import org.bioinfo.cellbase.lib.common.GenericFeature;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class SeekTest {
    String USER_HOME = System.getProperty("user.home");
    @Test
    public void testQueryByOffset() throws IOException {
        Path regulatoryRegionPath = Paths.get(USER_HOME, "cellbase_v3", "hsapiens", "genomic_regulatory_region");
        Path filePath = regulatoryRegionPath.resolve("AnnotatedFeatures.gff");
        RandomAccessFile raf = new RandomAccessFile(filePath.toString(), "r");
        Path dbPath = regulatoryRegionPath.resolve("AnnotatedFeatures.gff.sqlite3");
//        chr10   ccat_histone    AnnotatedFeature        5400800 5411600
//        chrY    SWEmbl_R015_D150        AnnotatedFeature        59028904        59029348
        List<Long> a = queryRegulatoryDB(dbPath, "annotated_features2","chr10",5400800,5411600);
        List<Long> b = queryRegulatoryDB(dbPath, "annotated_features2","chrY",59028904,59029348);

        for(long item : a){
            System.out.println(item);
            raf.seek(item);
            System.out.println(raf.readLine());
        }
        for(long item : b){
            System.out.println(item);
            raf.seek(item);
            System.out.println(raf.readLine());
        }
    }

    @Test
    public void testGetOffset() throws IOException, SQLException, ClassNotFoundException {
        Path regulatoryRegionPath = Paths.get(USER_HOME, "cellbase_v3", "hsapiens", "genomic_regulatory_region");
        Path filePath = regulatoryRegionPath.resolve("AnnotatedFeatures.gff");

        List<String> GFFColumnNames = Arrays.asList("pos", "chr", "start", "end");
        List<String> GFFColumnTypes = Arrays.asList("BIGINT", "TEXT", "INT", "INT");
        List<String> indexColumns = Arrays.asList("chr", "start", "end");

        createSQLiteFiles(filePath, "annotated_features2", GFFColumnNames, GFFColumnTypes, indexColumns, false);



//        RandomAccessFile raf = new RandomAccessFile(filePath.toString(), "r");
//        List<Long> lineOffsets = new ArrayList<Long>();



//        String line;
//        long pos = 0;
//        while ((line = raf.readLine()) != null) {
//            System.out.print((pos)+" - ");
//            pos += line.length()+1;
//            System.out.println(line);
//        }
//        System.out.println(raf.length());

//        long t = System.currentTimeMillis();
//        raf.seek(90878045);
////        System.out.println(raf.readLine());
//        System.out.println((System.currentTimeMillis() - t) + " ms");
//
//
//        t = System.currentTimeMillis();
//        raf.seek(19883261);
////        System.out.println(raf.readLine());
//        System.out.println((System.currentTimeMillis() - t) + " ms");
//
//        t = System.currentTimeMillis();
//        raf.seek(1400883261);
////        System.out.println(raf.readLine());
//        System.out.println((System.currentTimeMillis() - t) + " ms");
////        raf.seek(0);
////        System.out.println(raf.readLine().length());
    }

    public static List<Long> queryRegulatoryDB(Path dbPath, String tableName, String chrFile, int start, int end) {
        Connection conn = null;
        List<Long> offsetPositions = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath.toString());

            Statement query = conn.createStatement();
            ResultSet rs = query.executeQuery("select * from " + tableName + " where chr='"+chrFile+"' AND start=" + start + " AND end=" + end);

            while (rs.next()) {
                offsetPositions.add(rs.getLong(1));
            }
            conn.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return offsetPositions;
    }


    public static void createSQLiteFiles(Path filePath, String tableName, List<String> columnNames, List<String> columnTypes, List<String> indexColumns, boolean gzip) throws ClassNotFoundException, IOException, SQLException {
        int LIMITROWS = 100000;
        int BatchCount = 0;

        Path dbPath = Paths.get(filePath.toString() + ".sqlite3");
        BufferedReader br;
        if (gzip) {
            br = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(filePath))));
        } else {
            br = Files.newBufferedReader(filePath, Charset.defaultCharset());
        }

        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath.toString());
        conn.setAutoCommit(false);//Set false to perform commits manually and increase performance on insertion

        //Create table query
        Statement createTables = conn.createStatement();

        StringBuilder sbQuery = new StringBuilder();
        sbQuery.append("CREATE TABLE if not exists " + tableName + "(");
        for (int i = 0; i < columnNames.size(); i++) {//columnNames and columnTypes must have same size
            sbQuery.append("'" + columnNames.get(i) + "' " + columnTypes.get(i) + ",");
        }
        sbQuery.deleteCharAt(sbQuery.length() - 1);
        sbQuery.append(")");

        System.out.println(sbQuery.toString());
        createTables.executeUpdate(sbQuery.toString());

        //Prepare insert query
        sbQuery = new StringBuilder();
        sbQuery.append("INSERT INTO " + tableName + "(");
        for (int i = 0; i < columnNames.size(); i++) {
            sbQuery.append("'" + columnNames.get(i) + "',");
        }
        sbQuery.deleteCharAt(sbQuery.length() - 1);
        sbQuery.append(")values (");
        sbQuery.append(repeat("?,", columnNames.size()));
        sbQuery.deleteCharAt(sbQuery.length() - 1);
        sbQuery.append(")");
        System.out.println(sbQuery.toString());

        PreparedStatement ps = conn.prepareStatement(sbQuery.toString());

        //Read file
        String line = null;
        long offsetPos = 0;
        while ((line = br.readLine()) != null) {

            insertByType(ps, getFields(line, offsetPos, tableName), columnTypes);
            offsetPos += line.length()+1;

            //commit batch
            if (BatchCount % LIMITROWS == 0 && BatchCount != 0) {
                ps.executeBatch();
                conn.commit();
            }
            ps.addBatch();
            BatchCount++;
        }
        br.close();

        //Execute last Batch
        ps.executeBatch();
        conn.commit();

        //Create index
        System.out.println("TODO create indices");
        System.out.println("creating indices...");

        sbQuery = new StringBuilder();
        for (int i = 0; i < indexColumns.size(); i++) {
            sbQuery.append(indexColumns.get(i) + ",");
        }
        sbQuery.deleteCharAt(sbQuery.length() - 1);
        String sQuery = "CREATE INDEX "+tableName+"_idx on "+tableName+"("+sbQuery.toString()+")";
        System.out.println(sQuery);
        createTables.executeUpdate(sQuery);
        System.out.println("indices created.");

        conn.commit();
        conn.close();
    }

    public static List<String> getFields(String line, long offsetPos, String tableName) {
        List<String> fields = new ArrayList<>();
        switch (tableName.toLowerCase()) {
            case "annotated_features2":
                fields.add(String.valueOf(offsetPos));
                String[] f = line.split("\t");
                fields.add(f[0]);
                fields.add(f[3]);
                fields.add(f[4]);
                break;
        }
        return fields;
    }

    public static void insertByType(PreparedStatement ps, List<String> fields, List<String> types) throws SQLException {
        //Datatypes In SQLite Version 3 -> http://www.sqlite.org/datatype3.html
        String raw;
        String type;
        if (types.size() == fields.size()) {
            for (int i = 0; i < fields.size(); i++) {//columnNames and columnTypes must have same size
                int sqliteIndex = i+1;
                raw = fields.get(i);
                type = types.get(i);

                switch (type) {
                    case "INTEGER":
                    case "INT":
                        ps.setInt(sqliteIndex, Integer.parseInt(raw));
                        break;
                    case "BIGINT":
                        ps.setLong(sqliteIndex, Long.parseLong(raw));
                        break;
                    case "REAL":
                        ps.setFloat(sqliteIndex, Float.parseFloat(raw));
                        break;
                    case "TEXT":
                        ps.setString(sqliteIndex, raw);
                        break;
                    default:
                        ps.setString(sqliteIndex, raw);
                        break;
                }
            }
        }

    }

    public static String repeat(String s, int n) {
        if (s == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

}
