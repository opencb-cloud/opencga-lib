package org.bioinfo.opencga.lib.storage.datamanagers;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.bioinfo.cellbase.lib.common.Region;
import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.formats.core.variant.vcf4.VcfRecord;
import org.bioinfo.opencga.lib.analysis.AnalysisExecutionException;
import org.bioinfo.opencga.lib.analysis.SgeManager;
import org.bioinfo.opencga.lib.storage.TabixReader;
import org.bioinfo.opencga.lib.storage.XObject;
import org.bioinfo.opencga.lib.storage.datamanagers.bam.BamManager;
import org.bioinfo.opencga.lib.storage.indices.SqliteManager;
import org.bioinfo.opencga.lib.utils.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VcfManager {
	private Gson gson;
	private static Logger logger = Logger.getLogger(BamManager.class);

    private static Path indexerManagerScript = Paths.get(Config.getGcsaHome(),
            Config.getAnalysisProperties().getProperty("OPENCGA.ANALYSIS.BINARIES.PATH"), "indexer", "indexerManager.py");

    XObject vcfColumns;

	public VcfManager() throws IOException {
		gson = new Gson();

        vcfColumns = new XObject();
        vcfColumns.put("chromosome", 0);
        vcfColumns.put("position", 1);
        vcfColumns.put("id", 2);
        vcfColumns.put("ref", 3);
        vcfColumns.put("alt", 4);
        vcfColumns.put("qual", 5);
        vcfColumns.put("filter", 6);
        vcfColumns.put("info", 7);
	}

    public static String createIndex(Path inputPath) throws IOException, InterruptedException,
            AnalysisExecutionException {

        if(Files.exists(Paths.get(inputPath + ".db"))){
            Files.delete(Paths.get(inputPath + ".db"));
        }

        String jobId = StringUtils.randomString(8);
        String commandLine = indexerManagerScript + " -t vcf -i " + inputPath;
        try {
            SgeManager.queueJob("indexer", jobId, 0, inputPath.getParent().toString(), commandLine);
        } catch (Exception e) {
            logger.error(e.toString());
            throw new AnalysisExecutionException("ERROR: sge execution failed.");
        }
        return "indexer_" + jobId;
    }

    private static File checkVcfIndex(Path inputPath) {
        //name.vcf.gz
        //name.vcf.tbi
        Path inputCompressedFile = Paths.get(inputPath + ".gz");
        Path inputIndexFile = Paths.get(inputPath + ".gz.tbi");
        if (Files.exists(inputIndexFile) && Files.exists(inputCompressedFile)) {
            return inputIndexFile.toFile();
        }
        return null;
    }

    public static boolean checkIndex(Path filePath){
        return Files.exists(Paths.get(filePath + ".db"));
    }


    public String queryRegion(Path filePath, String regionStr, Map<String, List<String>> params) throws SQLException, IOException, ClassNotFoundException {

        Path gzFilePath = Paths.get(filePath.toString()+".gz");

        Region region = Region.parseRegion(regionStr);
        String chromosome = region.getChromosome();
        int start = region.getStart();
        int end = region.getEnd();

        SqliteManager sqliteManager = new SqliteManager();
        sqliteManager.connect(filePath, true);

        Boolean histogram = false;
        if (params.get("histogram") != null) {
            histogram = Boolean.parseBoolean(params.get("histogram").get(0));
        }

        if(histogram){
            long tq = System.currentTimeMillis();
            String tableName = "chunk";
            String chrPrefix = "";
            String queryString = "SELECT * FROM " + "chunk" + " WHERE chromosome='"+chrPrefix+chromosome+"' AND start<=" + end + " AND end>=" + start;
            List<XObject> queryResults =  sqliteManager.query(queryString);

            sqliteManager.disconnect(true);
            System.out.println("Query time " + (System.currentTimeMillis() - tq) + "ms");
            return gson.toJson(queryResults);
        }

//        String tableName = "global_stats";
//        String queryString = "SELECT value FROM " + tableName + " WHERE name='CHR_"+chromosome+"_PREFIX'";
//        String chrPrefix = sqliteManager.query(queryString).get(0).getString("value");

        String chrPrefix = "";
        String tableName = "record_query_fields";
        String queryString = "SELECT position FROM " + tableName + " WHERE chromosome='"+chrPrefix+chromosome+"' AND position<=" + end + " AND position>=" + start;
        List<XObject> queryResults =  sqliteManager.query(queryString);
        int queryResultsLength = queryResults.size();
        //disconnect
        sqliteManager.disconnect(true);


        HashMap<String, XObject> queryResultsMap = new HashMap<>();
        for(XObject r : queryResults){
            queryResultsMap.put(r.getString("position"),r);
        }

        System.out.println("queryResultsLength " + queryResultsLength);

        //Query Tabbix
        File inputVcfIndexFile = checkVcfIndex(filePath);
        TabixReader tabixReader = new TabixReader(gzFilePath.toString());
        if (inputVcfIndexFile == null) {
            logger.info("VcfManager: " + "creating vcf index for: " + filePath);
            return null;
        }

        String line;
        logger.info("regionStr: "+regionStr);
        TabixReader.Iterator lines = null;
        try{
            lines = tabixReader.query(regionStr);
        }catch(Exception e){
            e.printStackTrace();
        }
        logger.info("lines != null: "+(lines == null));
        logger.info("lines: "+lines);
        List<org.bioinfo.formats.core.variant.vcf4.VcfRecord> records = new ArrayList<>();
        while (lines != null && ((line = lines.next()) != null)) {
            VcfRecord vcfRecord = new VcfRecord(line.split("\t"));
            if(queryResultsMap.get(String.valueOf(vcfRecord.getPosition()))!=null){
                records.add(vcfRecord);
                queryResultsLength--;
            }
            if(queryResultsLength<0){
                break;
            }
        }
        return gson.toJson(records);
    }


	public String getByRegion(Path fullFilePath, String regionStr, Map<String, List<String>> params) throws IOException {
		TabixReader tabixReader = new TabixReader(fullFilePath.toString());
		StringBuilder sb = new StringBuilder();
		try {
			TabixReader.Iterator lines = tabixReader.query(regionStr);

			String line;
			sb.append("[");
			while ((line = lines.next()) != null) {
				VcfRecord vcfRecord = new VcfRecord(line.split("\t"));
				sb.append(gson.toJson(vcfRecord) + ",");
			}
			// Remove last comma
			int sbLength = sb.length();
			int sbLastPos = sbLength - 1;
			if (sbLength > 1 && sb.charAt(sbLastPos) == ',') {
				sb.replace(sbLastPos, sbLength, "");
			}
			sb.append("]");

		} catch (Exception e) {
			logger.info(e);
			sb.append("[]");
		}

		return sb.toString();
	}
}
