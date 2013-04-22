package org.bioinfo.opencga.lib.storage.datamanagers;

import org.bioinfo.cellbase.lib.common.GenericFeature;
import org.bioinfo.cellbase.lib.common.GenericFeatureChunk;
import org.bioinfo.opencga.lib.storage.XObject;
import org.bioinfo.opencga.lib.storage.indices.DefaultParser;
import org.bioinfo.opencga.lib.storage.indices.SqliteManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class GffManager {

    private int CHUNKSIZE = 10000;

    public void createIndex(Path filePath) throws SQLException, IOException, ClassNotFoundException {

        SqliteManager sqliteManager = new SqliteManager();
        sqliteManager.connect(filePath);

        //offset
        String offsetTableName = "offset_region";
        LinkedHashMap<String,String> offsetColumns = new LinkedHashMap<String, String>();
        offsetColumns.put("chr", "TEXT");
        offsetColumns.put("start", "INT");
        offsetColumns.put("end", "INT");
        offsetColumns.put("pos", "BIGINT");
        sqliteManager.createTable(offsetTableName, offsetColumns);

        String offsetIndexName = "chr_start_end";
        LinkedHashMap<String,Integer> offsetIndices = new LinkedHashMap<String, Integer>();
        offsetIndices.put("chr", 0);
        offsetIndices.put("start", 3);
        offsetIndices.put("end", 4);
        DefaultParser offsetDefaultParser = new DefaultParser(offsetIndices);


        //chunk
        String chunkTableName = "chunk_region";
        LinkedHashMap<String,String> chunkColumns = new LinkedHashMap<String, String>();
        chunkColumns.put("chunk", "TEXT");
        chunkColumns.put("chr", "TEXT");
        chunkColumns.put("start", "INT");
        chunkColumns.put("end", "INT");
        chunkColumns.put("count", "BIGINT");
        sqliteManager.createTable(chunkTableName, chunkColumns);

        String chunkIndexName = "chr_start_end";
        LinkedHashMap<String,Integer> chunkIndices = new LinkedHashMap<String, Integer>();
        chunkIndices.put("chr", 0);
        chunkIndices.put("start", 3);
        chunkIndices.put("end", 4);
        DefaultParser chunkDefaultParser = new DefaultParser(chunkIndices);


        //chunk visited hash
        Set<Integer> visitedChunks = new HashSet<>();

        //Read file
        BufferedReader br;
        Boolean gzip = false;
        if (gzip) {
            br = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(filePath))));
        } else {
            br = Files.newBufferedReader(filePath, Charset.defaultCharset());
        }


        //Read file
        String line = null;
        long offsetPos = 0;
        while ((line = br.readLine()) != null) {
            XObject xObject = offsetDefaultParser.parse(line);
            xObject.put("pos",offsetPos);

            //table 1
            sqliteManager.insert(xObject, offsetTableName);
            offsetPos += line.length()+1;

            //table2
            int firstChunkId =  getChunkId(xObject.getInt("start"));
            int lastChunkId  = getChunkId(xObject.getInt("end"));

            for(int i=firstChunkId; i<=lastChunkId; i++){
                if(!visitedChunks.contains(i)){
                    int chunkStart = getChunkStart(i);
                    int chunkEnd = getChunkEnd(i);
                    visitedChunks.add(i);
                    XObject xoChunk = new XObject();
                    xoChunk.put("chunk", i);
                    xoChunk.put("chr", xObject.getString("chr"));
                    xoChunk.put("start", xObject.getInt("start"));
                    xoChunk.put("end", xObject.getInt("end"));
                    xoChunk.put("count", 0);
                    sqliteManager.insert(xoChunk, chunkTableName);
                }
                XObject xoUpdate = new XObject();
                xoUpdate.put("count", "count + 1");
                sqliteManager.update(xObject, xoUpdate, chunkTableName);

//                genericFeatureChunks.get(i).getFeatures().add(genericFeature);
            }

            //...

        }
        br.close();
        sqliteManager.commit(offsetTableName);

        sqliteManager.createIndex(offsetTableName, offsetIndexName, offsetIndices);
        sqliteManager.createIndex(chunkTableName, chunkIndexName, chunkIndices);

        sqliteManager.disconnect(true);
    }

    public void queryIndex(Path file) throws SQLException, IOException, ClassNotFoundException {
        //TODO ...

    }


    private int getChunkId(int position){
        return position/CHUNKSIZE;
    }
    private int getChunkStart(int id){
        if(id==0){
            return 1;
        }else{
            return id*CHUNKSIZE;
        }
    }
    private int getChunkEnd(int id){
        return (id*CHUNKSIZE)+CHUNKSIZE-1;
    }
}
