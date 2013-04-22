package org.bioinfo.opencga.lib.storage.indices;

import org.bioinfo.opencga.lib.storage.XObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class SqliteManager {

    private int LIMITROWS = 100000;
    private Connection connection;

    private Map<String, Integer> tableInsertCounters;
    private Map<String, PreparedStatement> tableInsertPreparedStatement;
    private Map<String, PreparedStatement> tableUpdatePreparedStatement;

    private Map<String, LinkedHashMap<String, String>> tableColumns;

    public SqliteManager() {

        tableInsertCounters = new HashMap<>();
        tableInsertPreparedStatement = new HashMap<>();
        tableColumns = new HashMap<>();
    }

    public void connect(Path filePath) throws ClassNotFoundException, SQLException {
        Path dbPath = Paths.get(filePath.toString() + ".sqlite3");

        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath.toString());
        connection.setAutoCommit(false);//Set false to perform commits manually and increase performance on insertion
    }

    public void disconnect(boolean commit) throws SQLException {
        if (connection != null) {
            if (commit) {
                connection.commit();
            }
            connection.close();
        }
    }

    public void createTable(String tableName, LinkedHashMap<String, String> columns) throws SQLException {
        Statement createTables = connection.createStatement();

        StringBuilder sbQuery = new StringBuilder();
        sbQuery.append("CREATE TABLE if not exists " + tableName + "(");

        Set<String> names = columns.keySet();
        for (String colName : names) {
            sbQuery.append("'" + colName + "' " + columns.get(colName) + ",");
        }
        sbQuery.deleteCharAt(sbQuery.length() - 1);
        sbQuery.append(")");

        System.out.println(sbQuery.toString());
        createTables.executeUpdate(sbQuery.toString());

        tableColumns.put(tableName, columns);
    }

    public void createIndex(String tableName, String indexName, LinkedHashMap<String,Integer> indices) throws SQLException {
        Statement createIndex = connection.createStatement();
        StringBuilder sbQuery = new StringBuilder();
        Set<String> names = indices.keySet();
        for (String colName : names) {
            sbQuery.append(colName + ",");
        }
        sbQuery.deleteCharAt(sbQuery.length() - 1);
        String sQuery = "CREATE INDEX " + tableName + "_" + indexName + "_idx on " + tableName + "(" + sbQuery.toString() + ")";
        System.out.println(sQuery);
        createIndex.executeUpdate(sQuery);
        System.out.println("indices created.");

        connection.commit();
    }

    public void commit(String tableName) throws SQLException {
        tableInsertPreparedStatement.get(tableName).executeBatch();
        connection.commit();
    }

    public void insert(XObject xObject, String tableName) throws SQLException {
//        int BatchCount = 0;
        if (!tableInsertCounters.containsKey(tableName)) {
            tableInsertCounters.put(tableName, 0);
        }

        PreparedStatement ps;
        if (!tableInsertPreparedStatement.containsKey(tableName)) {
            StringBuilder sbQuery = new StringBuilder();
            sbQuery.append("INSERT INTO " + tableName + "(");

            Set<String> names = xObject.keySet();
            for (String colName : names) {
                sbQuery.append("'" + colName + "',");
            }

            sbQuery.deleteCharAt(sbQuery.length() - 1);
            sbQuery.append(")values (");
            sbQuery.append(repeat("?,", names.size()));
            sbQuery.deleteCharAt(sbQuery.length() - 1);
            sbQuery.append(")");
            System.out.println(sbQuery.toString());

            ps = connection.prepareStatement(sbQuery.toString());
            tableInsertPreparedStatement.put(tableName, ps);
        } else {
            ps = tableInsertPreparedStatement.get(tableName);
        }

        // everything is ready to insert
        insertByType(ps, xObject, tableColumns.get(tableName));

        //commit batch
        if (tableInsertCounters.get(tableName) % LIMITROWS == 0 && tableInsertCounters.get(tableName) != 0) {
            commit(tableName);
        }
        ps.addBatch();
        tableInsertCounters.put(tableName, tableInsertCounters.get(tableName) + 1);

    }

    public void update(XObject xObject, XObject newXObject, String tableName) throws SQLException {
        PreparedStatement ps;
        if (!tableUpdatePreparedStatement.containsKey(tableName)) {
            StringBuilder sbQuery = new StringBuilder();

            sbQuery.append("UPDATE " + tableName + " SET ");
            Set<String> updateColumns = newXObject.keySet();
            for (String colName : updateColumns) {
                sbQuery.append("'" + colName + "'=?, ");
            }
            sbQuery.delete(sbQuery.length() - 2, sbQuery.length());//", ".length()
            sbQuery.append(" WHERE ");
            Set<String> whereColumns = xObject.keySet();
            for (String colName : whereColumns) {
                sbQuery.append("'" + colName + "'=? AND ");
            }
            sbQuery.delete(sbQuery.length() - 5, sbQuery.length());//" AND ".length()

            System.out.println(sbQuery.toString());

            ps = connection.prepareStatement(sbQuery.toString());
            tableUpdatePreparedStatement.put(tableName, ps);
        } else {
            ps = tableUpdatePreparedStatement.get(tableName);
        }

        updateByType(ps, xObject, newXObject, tableColumns.get(tableName));
        //commit batch
        if (tableInsertCounters.get(tableName) % LIMITROWS == 0 && tableInsertCounters.get(tableName) != 0) {
            commit(tableName);
        }
        ps.addBatch();
        tableInsertCounters.put(tableName, tableInsertCounters.get(tableName) + 1);
    }

    private void insertByType(PreparedStatement ps, XObject xObject, LinkedHashMap<String, String> columns) throws SQLException {
        String type;
        int i=1;
        Set<String> names = xObject.keySet();
        for (String name : names) {
            type = columns.get(name);
            setByType(ps, i, type, xObject, name);
            i++;
        }
    }

    private void setByType(PreparedStatement ps, int i, String type, XObject xo, String name) throws SQLException {
        //Datatypes In SQLite Version 3 -> http://www.sqlite.org/datatype3.html
        switch (type.toUpperCase()) {
            case "INTEGER":
            case "INT":
                ps.setInt(i, xo.getInt(name));
                break;
            case "BIGINT":
                ps.setLong(i, xo.getLong(name));
                break;
            case "REAL":
                ps.setFloat(i, xo.getFloat(name));
                break;
            case "TEXT":
                ps.setString(i, xo.getString(name));
                break;
            default:
                ps.setString(i, xo.getString(name));
                break;
        }
    }

    private void updateByType(PreparedStatement ps, XObject xObject, XObject newXObject, LinkedHashMap<String, String> columns) throws SQLException {
        String type;
        int i=1;
        Set<String> names = newXObject.keySet();
        for (String name : names) {
            type = columns.get(name);
            setByType(ps, i, type, newXObject, name);
            i++;
        }
        names = xObject.keySet();
        for (String name : names) {
            type = columns.get(name);
            setByType(ps, i, type, xObject, name);
            i++;
        }
    }


    private String repeat(String s, int n) {
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
