package org.bioinfo.opencga.lib.storage.datamanagers.bam;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.google.gson.*;
import net.sf.samtools.BAMIndexer;
import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecord.SAMTagAndValue;
import net.sf.samtools.SAMRecordIterator;

import org.apache.log4j.Logger;
import org.bioinfo.cellbase.lib.common.Region;
import org.bioinfo.commons.io.utils.FileUtils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.opencga.lib.analysis.AnalysisExecutionException;
import org.bioinfo.opencga.lib.analysis.SgeManager;
import org.bioinfo.opencga.lib.utils.Config;
import org.bioinfo.opencga.lib.utils.IOUtils;

public class BamManager {

    private String species = "hsa";
    private Gson gson;
    private static Logger logger = Logger.getLogger(BamManager.class);
    private static Path indexerManagerScript = Paths.get(Config.getGcsaHome(),
            Config.getAnalysisProperties().getProperty("OPENCGA.ANALYSIS.BINARIES.PATH"), "indexer", "indexerManager.py");

    private Properties analysisProperties = Config.getAnalysisProperties();

    public BamManager() throws IOException {
        gson = new Gson();
    }

    public static String createBamIndex(Path inputBamPath) throws IOException, InterruptedException,
            AnalysisExecutionException {
        String jobId = StringUtils.randomString(8);
        String commandLine = indexerManagerScript + " -t bam " + inputBamPath;
        try {
            SgeManager.queueJob("indexer", jobId, 0, inputBamPath.getParent().toString(), commandLine);
        } catch (Exception e) {
            logger.error(e.toString());
            throw new AnalysisExecutionException("ERROR: sge execution failed.");
        }
        return "indexer_" + jobId;
    }

    public File checkBamIndex(Path inputBamPath) {
        Path inputBamIndexFile = Paths.get(inputBamPath + ".bai");
        logger.info(inputBamIndexFile.toString());
        if (Files.exists(inputBamIndexFile)) {
            return inputBamIndexFile.toFile();
        }
        String fileName = IOUtils.removeExtension(inputBamPath.toString());
        inputBamIndexFile = Paths.get(fileName + ".bai");
        logger.info(inputBamIndexFile.toString());
        if (Files.exists(inputBamIndexFile)) {
            return inputBamIndexFile.toFile();
        }
        return null;
    }

    public String getByRegion(Path fullFilePath, String regionStr, Map<String, List<String>> params) throws IOException {
        long totalTime = System.currentTimeMillis();

        Region region = Region.parseRegion(regionStr);
        String chr = region.getChromosome();
        int start = region.getStart();
        int end = region.getEnd();

        logger.info("chr: " + chr + " start: " + start + " end: " + end);

        if (params.get("species") != null) {
            species = params.get("species").get(0);
        }
        Boolean viewAsPairs = false;
        if (params.get("view_as_pairs") != null) {
            viewAsPairs = Boolean.parseBoolean(params.get("view_as_pairs").get(0));
        }
        Boolean showSoftclipping = false;
        if (params.get("show_softclipping") != null) {
            showSoftclipping = Boolean.parseBoolean(params.get("show_softclipping").get(0));
        }
        Boolean histogram = false;
        if (params.get("histogram") != null) {
            histogram = Boolean.parseBoolean(params.get("histogram").get(0));
        }
        int interval = 200000;
        if (params.get("interval") != null) {
            interval = Integer.parseInt(params.get("interval").get(0));
        }

        File inputBamFile = new File(fullFilePath.toString());
        File inputBamIndexFile = checkBamIndex(fullFilePath);

        if (inputBamIndexFile == null) {
            logger.info("BamManager: " + "creating bam index for: " + fullFilePath);
            // createBamIndex(inputBamFile, inputBamIndexFile);
            return "{error:'no index found'}";
        }

        long t = System.currentTimeMillis();
        SAMFileReader inputSam = new SAMFileReader(inputBamFile, inputBamIndexFile);
        System.out.println("new SamFileReader in " + (System.currentTimeMillis() - t) + "ms");
        System.out.println("hasIndex " + inputSam.hasIndex());

        t = System.currentTimeMillis();
        SAMRecordIterator recordsFound = inputSam.query(chr, start, end, false);
        System.out.println("query SamFileReader in " + (System.currentTimeMillis() - t) + "ms");

        /**
         * ARRAY LIST
         */
        ArrayList<SAMRecord> records = new ArrayList<SAMRecord>();
        t = System.currentTimeMillis();
        while (recordsFound.hasNext()) {
            SAMRecord record = recordsFound.next();
            records.add(record);
        }
        System.out.println(records.size() + " elements added in: " + (System.currentTimeMillis() - t) + "ms");

        /**
         * Check histogram
         */
        if (histogram) {
            int numIntervals = (region.getEnd() - region.getStart()) / interval + 1;
            System.out.println("numIntervals :" + numIntervals);
            int[] intervalCount = new int[numIntervals];

            System.out.println(region.getChromosome());
            System.out.println(region.getStart());
            System.out.println(region.getEnd());
            for (SAMRecord record : records) {
//				System.out.println("---*-*-*-*-" + numIntervals);
//				System.out.println("---*-*-*-*-" + record.getAlignmentStart());
//				System.out.println("---*-*-*-*-" + interval);
                if (record.getAlignmentStart() >= region.getStart() && record.getAlignmentStart() <= region.getEnd()) {
                    int intervalIndex = (record.getAlignmentStart() - region.getStart()) / interval; // truncate
//					System.out.print(intervalIndex + " ");
                    intervalCount[intervalIndex]++;
                }
            }

            int intervalStart = region.getStart();
            int intervalEnd = intervalStart + interval - 1;
            BasicDBList intervalList = new BasicDBList();
            for (int i = 0; i < numIntervals; i++) {
                BasicDBObject intervalObj = new BasicDBObject();
                intervalObj.put("start", intervalStart);
                intervalObj.put("end", intervalEnd);
                intervalObj.put("interval", i);
                intervalObj.put("value", intervalCount[i]);
                intervalList.add(intervalObj);
                intervalStart = intervalEnd + 1;
                intervalEnd = intervalStart + interval - 1;
            }

            System.out.println(region.getChromosome());
            System.out.println(region.getStart());
            System.out.println(region.getEnd());
            return intervalList.toString();
        }

        /**
         * GET GENOME SEQUENCE
         */
        t = System.currentTimeMillis();
        String forwardSequence = getSequence(chr, start, end);
        String reverseSequence = revcomp(forwardSequence);
        // System.out.println(forwardSequence);
        // System.out.println(reverseSequence);
        System.out.println("Get genome sequence in " + (System.currentTimeMillis() - t) + "ms");
        /**
         * COVERAGE
         */
        short[] coverageArray = new short[end - start + 1];
        short[] aBaseArray = new short[end - start + 1];
        short[] cBaseArray = new short[end - start + 1];
        short[] gBaseArray = new short[end - start + 1];
        short[] tBaseArray = new short[end - start + 1];

        if (viewAsPairs) {
            t = System.currentTimeMillis();
            Collections.sort(records, new Comparator<SAMRecord>() {
                @Override
                public int compare(SAMRecord o1, SAMRecord o2) {
                    if (o1 != null && o1.getReadName() != null && o2 != null) {
                        return o1.getReadName().compareTo(o2.getReadName());
                    }
                    return -1;
                }
            });
            System.out.println(records.size() + " elements sorted in: " + (System.currentTimeMillis() - t) + "ms");
        }

        t = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"reads\":[");
        StringBuilder attrString;
        String readStr;
        int readPos;
//        logger.info("Processing SAM records");
        for (SAMRecord record : records) {
//            logger.info(record.getReadName());

            Boolean condition = (!record.getReadUnmappedFlag());
            if (condition) {
                attrString = new StringBuilder();
                attrString.append("{");
                for (SAMTagAndValue attr : record.getAttributes()) {
                    attrString.append("\"" + attr.tag + "\":\""
                            + attr.value.toString().replace("\\", "\\\\").replace("\"", "\\\"") + "\",");
                }
                // Remove last comma
                if (attrString.length() > 1) {
                    attrString.replace(attrString.length() - 1, attrString.length(), "");
                }
                attrString.append("}");

                readStr = record.getReadString();

                /***************************************************************************/
                if (true) {// TEST
                    // if(record.getReadNegativeStrandFlag()
                    // ){
                    // if(record.getReadName().equals("SRR081241.8998181") ||
                    // record.getReadName().equals("SRR081241.645807")
                    // ){

                    // System.out.println("#############################################################################################################################################");
                    // System.out.println("#############################################################################################################################################");
                    // System.out.println("Unclipped Start:"+(record.getUnclippedStart()-start));
                    // System.out.println("Unclipped End:"+(record.getUnclippedEnd()-start+1));
                    // System.out.println(record.getCigarString()+"   Alig Length:"+(record.getAlignmentEnd()-record.getAlignmentStart()+1)+"   Unclipped length:"+(record.getUnclippedEnd()-record.getUnclippedStart()+1));

                    String refStr = forwardSequence.substring((500 + record.getUnclippedStart() - start),
                            (500 + record.getUnclippedEnd() - start + 1));

                    // System.out.println("refe:"+refStr+"  refe.length:"+refStr.length());
                    // System.out.println("read:"+readStr+"  readStr.length:"+readStr.length()+"   getReadLength:"+record.getReadLength());
                    StringBuilder diffStr = new StringBuilder();

                    int index = 0;
                    int indexRef = 0;
                    // System.out.println(gson.toJson(record.getCigar().getCigarElements()));

//                    logger.info("checking cigar: " + record.getCigar().toString());
                    for (int i = 0; i < record.getCigar().getCigarElements().size(); i++) {
                        CigarElement cigarEl = record.getCigar().getCigarElement(i);
                        CigarOperator cigarOp = cigarEl.getOperator();
                        int cigarLen = cigarEl.getLength();
//                        logger.info(cigarOp + " found" + " index:" + index + " indexRef:" + indexRef + " cigarLen:" + cigarLen);

                        if (cigarOp == CigarOperator.M || cigarOp == CigarOperator.EQ || cigarOp == CigarOperator.X) {
                            String subref = refStr.substring(indexRef, indexRef + cigarLen);
                            String subread = readStr.substring(index, index + cigarLen);
                            diffStr.append(getDiff(subref, subread));
                            index = index + cigarLen;
                            indexRef = indexRef + cigarLen;
                        }
                        if (cigarOp == CigarOperator.I) {
                            diffStr.append(readStr.substring(index, index + cigarLen).toLowerCase());
                            index = index + cigarLen;
                            // TODO save insertions
                        }
                        if (cigarOp == CigarOperator.D) {
                            for (int bi = 0; bi < cigarLen; bi++) {
                                diffStr.append("d");
                            }
                            indexRef = indexRef + cigarLen;
                        }
                        if (cigarOp == CigarOperator.N) {
                            for (int bi = 0; bi < cigarLen; bi++) {
                                diffStr.append("n");
                            }
                            indexRef = indexRef + cigarLen;
                        }
                        if (cigarOp == CigarOperator.S) {
                            if (showSoftclipping) {
                                String subread = readStr.substring(index, index + cigarLen);
                                diffStr.append(subread);
                                index = index + cigarLen;
                                indexRef = indexRef + cigarLen;
                            } else {
                                for (int bi = 0; bi < cigarLen; bi++) {
                                    diffStr.append(" ");
                                }
                                index = index + cigarLen;
                                indexRef = indexRef + cigarLen;
                            }
                        }
                        if (cigarOp == CigarOperator.H) {
                            for (int bi = 0; bi < cigarLen; bi++) {
                                diffStr.append("h");
                            }
                            indexRef = indexRef + cigarLen;
                        }
                        if (cigarOp == CigarOperator.P) {
                            for (int bi = 0; bi < cigarLen; bi++) {
                                diffStr.append("p");
                            }
                            indexRef = indexRef + cigarLen;
                        }
                        // if(cigarOp == CigarOperator.EQ) {
                        //
                        // }
                        // if(cigarOp == CigarOperator.X) {
                        //
                        // }
                    }
                    // System.out.println("diff:"+diffStr);
                    String empty = diffStr.toString().replace(" ", "");
                    // System.out.println("diff:"+diffStr);
                    /*************************************************************************/

                    sb.append("{");
                    sb.append("\"start\":" + record.getAlignmentStart() + ",");
                    sb.append("\"end\":" + record.getAlignmentEnd() + ",");
                    sb.append("\"unclippedStart\":" + record.getUnclippedStart() + ",");
                    sb.append("\"unclippedEnd\":" + record.getUnclippedEnd() + ",");
                    sb.append("\"chromosome\":\"" + chr + "\",");
                    sb.append("\"flags\":\"" + record.getFlags() + "\",");// with
                    // flags
                    // the
                    // strand
                    // will
                    // be
                    // calculated
                    sb.append("\"cigar\":\"" + record.getCigarString() + "\",");
                    sb.append("\"name\":\"" + record.getReadName() + "\",");
                    sb.append("\"blocks\":\"" + record.getAlignmentBlocks().get(0).getLength() + "\",");

                    sb.append("\"attributes\":" + attrString.toString() + ",");

                    // sb.append("\"readGroupId\":\""+record.getReadGroup().getId()+"\",");
                    // sb.append("\"readGroupPlatform\":\""+record.getReadGroup().getPlatform()+"\",");
                    // sb.append("\"readGroupLibrary\":\""+record.getReadGroup().getLibrary()+"\",");
                    sb.append("\"referenceName\":\"" + record.getReferenceName() + "\",");
                    sb.append("\"baseQualityString\":\""
                            + record.getBaseQualityString().replace("\\", "\\\\").replace("\"", "\\\"") + "\",");// the
                    // "
                    // char
                    // unables
                    // parse
                    // from
                    // javascript
                    // sb.append("\"baseQualityString\":\""+gson.toJson(baseQualityArray)+"\",");//
                    // the " char unables parse from javascript
                    sb.append("\"header\":\"" + record.getHeader().toString() + "\",");
                    sb.append("\"readLength\":" + record.getReadLength() + ",");
                    sb.append("\"mappingQuality\":" + record.getMappingQuality() + ",");

                    sb.append("\"mateReferenceName\":\"" + record.getMateReferenceName() + "\",");
                    sb.append("\"mateAlignmentStart\":" + record.getMateAlignmentStart() + ",");
                    sb.append("\"inferredInsertSize\":" + record.getInferredInsertSize() + ",");

                    if (!empty.isEmpty()) {
                        sb.append("\"diff\":\"" + diffStr + "\",");
                    }

                    sb.append("\"read\":\"" + readStr + "\"");
                    sb.append("},");

                }// IF TEST BY READ NAME


//                logger.info("Creating coverage array");
                // TODO cigar check for correct coverage calculation and
                int refgenomeOffset = 0;
                int readOffset = 0;
                int offset = record.getAlignmentStart() - start;
                for (int i = 0; i < record.getCigar().getCigarElements().size(); i++) {
                    if (record.getCigar().getCigarElement(i).getOperator() == CigarOperator.M) {
//                        logger.info("start: "+start);
//                        logger.info("r a start: "+record.getAlignmentStart());
//                        logger.info("refgenomeOffset: "+refgenomeOffset);
//                        logger.info("r c lenght: "+record.getCigar().getCigarElement(i).getLength());
//                        logger.info(record.getAlignmentStart() - start + refgenomeOffset);
//                        logger.info("readStr: "+readStr.length());
//                        logger.info("readStr: "+readStr.length());

                        for (int j = record.getAlignmentStart() - start + refgenomeOffset, cont = 0; cont < record.getCigar().getCigarElement(i).getLength(); j++, cont++) {
                            if (j >= 0 && j < coverageArray.length) {
                                coverageArray[j]++;
                                readPos = j - offset;
                                // if(record.getAlignmentStart() == 32877696){
                                // System.out.println(i-(record.getAlignmentStart()-start));
                                // System.out.println(record.getAlignmentStart()-start);
                                // }
                                // System.out.print(" - "+(cont+readOffset));
                                // System.out.print("|"+readStr.length());
                                int total = cont + readOffset;
                                // if(total < readStr.length()){
//                                logger.info(readStr.length());
                                switch (readStr.charAt(total)) {
                                    case 'A':
                                        aBaseArray[j]++;
                                        break;
                                    case 'C':
                                        cBaseArray[j]++;
                                        break;
                                    case 'G':
                                        gBaseArray[j]++;
                                        break;
                                    case 'T':
                                        tBaseArray[j]++;
                                        break;
                                }
                                // }
                            }
                        }
                    }
                    if (record.getCigar().getCigarElement(i).getOperator() == CigarOperator.I) {
                        refgenomeOffset++;
                        readOffset += record.getCigar().getCigarElement(i).getLength() - 1;
                    } else if (record.getCigar().getCigarElement(i).getOperator() == CigarOperator.D) {
                        refgenomeOffset += record.getCigar().getCigarElement(i).getLength() - 1;
                        readOffset++;
                    } else if (record.getCigar().getCigarElement(i).getOperator() == CigarOperator.H) {
                        //Ignored Hardclipping and do not update offset pointers
                    } else {
                        refgenomeOffset += record.getCigar().getCigarElement(i).getLength() - 1;
                        readOffset += record.getCigar().getCigarElement(i).getLength() - 1;
                    }
//                    if (record.getCigar().getCigarElement(i).getOperator() != CigarOperator.I) {
//                    } else if(){
//                    }
                }
//                logger.info("coverage array created");
            }
//            logger.info(" ");
        }

        // Remove last comma
        int sbLength = sb.length();
        int sbLastPos = sbLength - 1;
        if (sbLength > 1 && sb.charAt(sbLastPos) == ',') {
            sb.replace(sbLastPos, sbLength, "");
        }

        // //FIXME
        // sb.append("]");
        // sb.append(",\"coverage\":"+gson.toJson(coverageArray));
        // sb.append("}");

        // FIXME
        sb.append("]");
        sb.append(",\"coverage\":{\"all\":" + gson.toJson(coverageArray));
        sb.append(",\"a\":" + gson.toJson(aBaseArray));
        sb.append(",\"c\":" + gson.toJson(cBaseArray));
        sb.append(",\"g\":" + gson.toJson(gBaseArray));
        sb.append(",\"t\":" + gson.toJson(tBaseArray));
        sb.append("}");
        sb.append("}");

        String json = sb.toString();
        System.out.println("Result String created in " + (System.currentTimeMillis() - t) + "ms");

        // IOUtils.write("/tmp/dqslastgetByRegionCall", json);

        inputSam.close();

        System.out.println("TOTAL " + (System.currentTimeMillis() - totalTime) + "ms");
        return json;
    }

    private String getDiff(String refStr, String readStr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < refStr.length(); i++) {
            if (refStr.charAt(i) != readStr.charAt(i)) {
                sb.append(readStr.charAt(i));
            } else {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String getSequence(final String chr, final int start, final int end) throws IOException {
        String version = "latest";
        String host = analysisProperties.getProperty("CELLBASE.HOST","ws.bioinfo.cipf.es");
        if (species.equals("ccl")) {//TESTING
            version = "v3";
        }
        String urlString = "http://" + host + "/cellbase/rest/" + version + "/" + species + "/genomic/region/" + chr + ":"
                + (start - 500) + "-" + (end + 500) + "/sequence?of=json";
        System.out.println(urlString);

        URL url = new URL(urlString);
        InputStream is = url.openConnection().getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line = null;
        StringBuilder sb = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            sb.append(line.trim());
        }
        reader.close();

        JsonElement json = new JsonParser().parse(sb.toString());
        JsonArray array = json.getAsJsonArray();
        JsonObject obj = array.get(0).getAsJsonObject();

        return obj.get("sequence").getAsString();
    }

    private String revcomp(String seq) {
        StringBuilder sb = new StringBuilder(seq.length());
        char c;
        for (int i = seq.length() - 1; i > 0; i--) {
            c = seq.charAt(i);
            switch (c) {
                case 'A':
                    c = 'T';
                    break;
                case 'T':
                    c = 'A';
                    break;
                case 'G':
                    c = 'C';
                    break;
                case 'C':
                    c = 'G';
                    break;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public String getFileList(final String filePath) {
        File bamDir = new File(filePath + "/bam");
        try {
            FileUtils.checkDirectory(bamDir);

            File[] files = FileUtils.listFiles(bamDir, ".+.bam");
            StringBuilder sb = new StringBuilder();

            sb.append("[");
            for (int i = 0; i < files.length; i++) {
                if (!files[i].isDirectory()) {
                    File bai = new File(files[i].getAbsolutePath() + ".bai");
                    try {
                        FileUtils.checkFile(bai);
                        sb.append("\"" + files[i].getName() + "\",");
                    } catch (IOException e) {
                        logger.info(files[i].getName() + " was not added because " + files[i].getName()
                                + ".bai was not found.");
                    }
                }
            }
            // Remove last comma
            if (sb.length() > 1) {
                sb.replace(sb.length() - 1, sb.length(), "");
            }
            sb.append("]");

            logger.info(sb.toString());
            return sb.toString();

        } catch (IOException e1) {
            return bamDir.getAbsolutePath() + "not exists.";
        }

    }
}
