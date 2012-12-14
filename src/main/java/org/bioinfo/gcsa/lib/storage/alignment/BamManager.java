package org.bioinfo.gcsa.lib.storage.alignment;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecord.SAMTagAndValue;
import net.sf.samtools.SAMRecordIterator;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.commons.log.Logger;

import com.google.gson.Gson;


public class BamManager {

	protected Gson gson = new Gson();
	protected Logger logger;

	public BamManager() throws IOException{
		//		ResourceBundle properties = ResourceBundle.getBundle("application");
		gson = new Gson();
		logger = new Logger();
		logger.setLevel(Logger.INFO_LEVEL);
	}

	//	public String getByRegion(List<Region> regions)
	//	open_file
	//		for(region) {
	//			SAMRecordIterator recordsFound = inputSam.query(region);
	//		}
	//	}

	public String getByRegion(final String filePath, final String chr, final int start, final int end, Boolean viewAsPairs, Boolean showSoftclipping) throws IOException{
		long totalTime = System.currentTimeMillis();
		
		System.out.println("chr: "+chr+" start: "+start+" end: "+end);

		File inputSamFile = new File(filePath);
		File indexFile = null;
		if(!new File(filePath+".bai").exists()) {
			// crearlo!
		}
		indexFile = new File(filePath+".bai");


		long t = System.currentTimeMillis();
		final SAMFileReader inputSam = new SAMFileReader(inputSamFile, indexFile);
		System.out.println("new SamFileReader in "+(System.currentTimeMillis()-t)+"ms");
		System.out.println("hasIndex " + inputSam.hasIndex());

		t = System.currentTimeMillis();
		SAMRecordIterator recordsFound = inputSam.query(chr, start, end, false);
		System.out.println("query SamFileReader in "+(System.currentTimeMillis()-t)+"ms");

		/**
		 * GET GENOME SEQUENCE
		 */
		t = System.currentTimeMillis();
		String forwardSequence = getSequence(chr, start, end);
		String reverseSequence = revcomp(forwardSequence); 
//		System.out.println(forwardSequence);
//		System.out.println(reverseSequence);
		System.out.println("Get genome sequence in "+(System.currentTimeMillis()-t)+"ms");
		/**
		 * COVERAGE
		 */
		short[] coverageArray = new short [end-start+1];
		short[] aBaseArray = new short [end-start+1];
		short[] cBaseArray = new short [end-start+1];
		short[] gBaseArray = new short [end-start+1];
		short[] tBaseArray = new short [end-start+1];

		//*ARRAY LIST*//
		ArrayList<SAMRecord> records = new ArrayList<SAMRecord>();
		t = System.currentTimeMillis();
		while(recordsFound.hasNext()){
			SAMRecord record = recordsFound.next();
			records.add(record);
		}
		System.out.println(records.size()+" elements added in: "+(System.currentTimeMillis()-t)+"ms");
		
		if(viewAsPairs){
			t = System.currentTimeMillis();
			Collections.sort(records, new Comparator<SAMRecord>() {
				@Override
				public int compare(SAMRecord o1, SAMRecord o2) {
					if(o1 != null && o1.getReadName() != null && o2 != null) {
						return o1.getReadName().compareTo(o2.getReadName());					
					}
					return -1;
				}
			});
			System.out.println(records.size()+" elements sorted in: "+(System.currentTimeMillis()-t)+"ms");
		}
		
		t = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"reads\":[");
		StringBuilder attrString;
		String readStr;
		int readPos;
		for (SAMRecord record : records) {
			Boolean condition = (!record.getReadUnmappedFlag());
			if(condition){
				attrString = new StringBuilder();
				attrString.append("{");
				for (SAMTagAndValue attr : record.getAttributes()) {
					attrString.append("\""+attr.tag+"\":\""+    attr.value.toString().replace("\\", "\\\\").replace("\"", "\\\"")    +"\",");
				}
				//Remove last comma
				if(attrString.length()>1){
					attrString.replace(attrString.length()-1, attrString.length(), "");
				}
				attrString.append("}");

				readStr = record.getReadString();

				/***************************************************************************/
				if(true){//TEST
					//				if(record.getReadNegativeStrandFlag()
					//				){
					//				if(record.getReadName().equals("SRR081241.8998181") || 
					//				   record.getReadName().equals("SRR081241.645807")	
					//				){

//					System.out.println("#############################################################################################################################################");
//					System.out.println("#############################################################################################################################################");
//					System.out.println("Unclipped Start:"+(record.getUnclippedStart()-start));
//					System.out.println("Unclipped End:"+(record.getUnclippedEnd()-start+1));
//					System.out.println(record.getCigarString()+"   Alig Length:"+(record.getAlignmentEnd()-record.getAlignmentStart()+1)+"   Unclipped length:"+(record.getUnclippedEnd()-record.getUnclippedStart()+1));
					
					String refStr = forwardSequence.substring((500+record.getUnclippedStart()-start), (500+record.getUnclippedEnd()-start+1));
					
//					System.out.println("refe:"+refStr+"  refe.length:"+refStr.length());
//					System.out.println("read:"+readStr+"  readStr.length:"+readStr.length()+"   getReadLength:"+record.getReadLength());
					StringBuilder diffStr = new StringBuilder();

					int index = 0;
					int indexRef = 0;
					//				System.out.println(gson.toJson(record.getCigar().getCigarElements()));
					for(int i=0; i < record.getCigar().getCigarElements().size(); i++) {
						CigarElement cigarEl = record.getCigar().getCigarElement(i);
						CigarOperator cigarOp = cigarEl.getOperator();
						int cigarLen = cigarEl.getLength();
//						System.out.println(cigarOp+" found"+" index:"+index+" indexRef:"+indexRef+" cigarLen:"+cigarLen);

						if(cigarOp == CigarOperator.M || cigarOp == CigarOperator.EQ || cigarOp == CigarOperator.X) {
							String subref = refStr.substring(indexRef, indexRef+cigarLen);
							String subread = readStr.substring(index, index+cigarLen);
							diffStr.append(getDiff(subref,subread));
							index = index+cigarLen;
							indexRef = indexRef+cigarLen;
						}
						if(cigarOp == CigarOperator.I) {
							diffStr.append(readStr.substring(index, index+cigarLen).toLowerCase());
							index = index+cigarLen;
							//TODO save insertions
						}
						if(cigarOp == CigarOperator.D) {
							for(int bi = 0; bi<cigarLen; bi++){
								diffStr.append("d");
							}
							indexRef = indexRef+cigarLen;
						}
						if(cigarOp == CigarOperator.N) {
							for(int bi = 0; bi<cigarLen; bi++){
								diffStr.append("n");
							}
							indexRef = indexRef+cigarLen;
						}
						if(cigarOp == CigarOperator.S) {
							if(showSoftclipping){
								String subread = readStr.substring(index, index+cigarLen);
								diffStr.append(subread);
								index = index+cigarLen;
								indexRef = indexRef+cigarLen;
							}else{
								for(int bi = 0; bi<cigarLen; bi++){
									diffStr.append(" ");
								}
								index = index+cigarLen;
								indexRef = indexRef+cigarLen;
							}
						}
						if(cigarOp == CigarOperator.H) {
							for(int bi = 0; bi<cigarLen; bi++){
								diffStr.append("h");
							}
							indexRef = indexRef+cigarLen;
						}
						if(cigarOp == CigarOperator.P) {
							for(int bi = 0; bi<cigarLen; bi++){
								diffStr.append("p");
							}
							indexRef = indexRef+cigarLen;
						}
						//					if(cigarOp == CigarOperator.EQ) {
						//						
						//					}
						//					if(cigarOp == CigarOperator.X) {
						//						
						//					}
					}

//					System.out.println("diff:"+diffStr);
					String empty = diffStr.toString().replace(" ", "");
//					System.out.println("diff:"+diffStr);
					/*************************************************************************/



					sb.append("{");
					sb.append("\"start\":"+record.getAlignmentStart()+",");
					sb.append("\"end\":"+record.getAlignmentEnd()+",");
					sb.append("\"unclippedStart\":"+record.getUnclippedStart()+",");
					sb.append("\"unclippedEnd\":"+record.getUnclippedEnd()+",");
					sb.append("\"chromosome\":\""+chr+"\",");
					sb.append("\"flags\":\""+record.getFlags()+"\",");//with flags the strand will be calculated
					sb.append("\"cigar\":\""+record.getCigarString()+"\",");
					sb.append("\"name\":\""+record.getReadName()+"\",");
					sb.append("\"blocks\":\""+record.getAlignmentBlocks().get(0).getLength()+"\",");

					sb.append("\"attributes\":"+attrString.toString()+",");

					//				sb.append("\"readGroupId\":\""+record.getReadGroup().getId()+"\",");
					//				sb.append("\"readGroupPlatform\":\""+record.getReadGroup().getPlatform()+"\",");
					//				sb.append("\"readGroupLibrary\":\""+record.getReadGroup().getLibrary()+"\",");
					sb.append("\"referenceName\":\""+record.getReferenceName()+"\",");
					sb.append("\"baseQualityString\":\""+record.getBaseQualityString().replace("\\", "\\\\").replace("\"", "\\\"")+"\",");// the " char unables parse from javascript
					//			sb.append("\"baseQualityString\":\""+gson.toJson(baseQualityArray)+"\",");// the " char unables parse from javascript
					sb.append("\"header\":\""+record.getHeader().toString()+"\",");
					sb.append("\"readLength\":"+record.getReadLength()+",");
					sb.append("\"mappingQuality\":"+record.getMappingQuality()+",");

					sb.append("\"mateReferenceName\":\""+record.getMateReferenceName()+"\",");
					sb.append("\"mateAlignmentStart\":"+record.getMateAlignmentStart()+",");
					sb.append("\"inferredInsertSize\":"+record.getInferredInsertSize()+",");

					if(!empty.isEmpty()){
						sb.append("\"diff\":\""+diffStr+"\",");
					}

					sb.append("\"read\":\""+readStr+"\"");
					sb.append("},");

				}//IF TEST BY READ NAME



				//TODO cigar check for correct coverage calculation and 
				int refgenomeOffset = 0;
				int readOffset = 0;
				int offset = record.getAlignmentStart()-start;
				for(int i=0; i < record.getCigar().getCigarElements().size(); i++) {
					if(record.getCigar().getCigarElement(i).getOperator() == CigarOperator.M) {
						for(int j=record.getAlignmentStart()-start+refgenomeOffset, cont=0; cont<record.getCigar().getCigarElement(i).getLength(); j++,cont++) {
							if(j>=0 && j<coverageArray.length) {
								coverageArray[j]++;
								readPos = j-offset;
								//							if(record.getAlignmentStart() == 32877696){
								//								System.out.println(i-(record.getAlignmentStart()-start));
								//								System.out.println(record.getAlignmentStart()-start);
								//							}
								//							System.out.print(" - "+(cont+readOffset));
								//							System.out.print("|"+readStr.length());
								int total = cont+readOffset;
								//							if(total < readStr.length()){
								switch(readStr.charAt(total)){
								case  'A': aBaseArray[j]++; break;
								case  'C': cBaseArray[j]++; break;
								case  'G': gBaseArray[j]++; break;
								case  'T': tBaseArray[j]++; break;
								}
								//							}
							}
						}
					}
					if(record.getCigar().getCigarElement(i).getOperator() != CigarOperator.I) {
						refgenomeOffset += record.getCigar().getCigarElement(i).getLength()-1;
						readOffset += record.getCigar().getCigarElement(i).getLength()-1;
					}else { 
						refgenomeOffset++;
						readOffset += record.getCigar().getCigarElement(i).getLength()-1;
					}
				}
			}
		}
		
		//Remove last comma
		if(sb.length()>1 && sb.charAt(sb.length()-1) == ','){
			sb.replace(sb.length()-1, sb.length(), "");
		}

		//		//FIXME
		//		sb.append("]");
		//		sb.append(",\"coverage\":"+gson.toJson(coverageArray));
		//		sb.append("}");


		//FIXME
		sb.append("]");
		sb.append(",\"coverage\":{\"all\":"+gson.toJson(coverageArray));
		sb.append(",\"a\":"+gson.toJson(aBaseArray));
		sb.append(",\"c\":"+gson.toJson(cBaseArray));
		sb.append(",\"g\":"+gson.toJson(gBaseArray));
		sb.append(",\"t\":"+gson.toJson(tBaseArray));
		sb.append("}");
		sb.append("}");


		String json = sb.toString();
		System.out.println("Result String created in "+(System.currentTimeMillis()-t)+"ms");

		//		IOUtils.write("/tmp/dqslastgetByRegionCall", json);

		inputSam.close();

		System.out.println("TOTAL "+(System.currentTimeMillis()-totalTime)+"ms");
		return json;
	}

	private String getDiff(String refStr, String readStr) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i<refStr.length();i++){
			if(refStr.charAt(i)!=readStr.charAt(i)){
				sb.append(readStr.charAt(i));
			}else{
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	private String getSequence(final String chr, final int start, final int end) {
		String urlString = "http://ws-beta.bioinfo.cipf.es/cellbase/rest/latest/hsa/genomic/region/"+chr+":"+(start-500)+"-"+(end+500)+"/sequence";
		System.out.println(urlString);
		StringBuilder sb = new StringBuilder();
		try {
			URL url = new URL(urlString);
			InputStream is = url.openConnection().getInputStream();
			BufferedReader reader = new BufferedReader( new InputStreamReader( is )  );
			String line = reader.readLine(); //remove first line
			while( ( line = reader.readLine() ) != null )  {
				sb.append(line.trim());
			}
			reader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sb.toString();
	}

	private String revcomp(String seq) {
		StringBuilder sb = new StringBuilder(seq.length());
		char c;
		for(int i=seq.length()-1; i>0; i--) {
			c = seq.charAt(i);
			switch(c) {
			case 'A': c = 'T';
			break;
			case 'T': c = 'A';
			break;
			case 'G': c = 'C';
			break;
			case 'C': c = 'G';
			break;
			}
			sb.append(c);
		}
		return sb.toString();
	}

	public String getFileList(final String filePath){
		File bamDir = new File(filePath+"/bam");
		try {
			FileUtils.checkDirectory(bamDir);

			File[] files = FileUtils.listFiles(bamDir, ".+.bam");
			StringBuilder sb = new StringBuilder();

			sb.append("[");
			for (int i = 0; i < files.length; i++) {
				if(!files[i].isDirectory()){
					File bai = new File(files[i].getAbsolutePath()+".bai");	
					try {
						FileUtils.checkFile(bai);
						sb.append("\""+files[i].getName()+"\",");
					} catch (IOException e) {
						logger.info(files[i].getName()+" was not added because "+files[i].getName()+".bai was not found.");
					}
				}
			}
			//Remove last comma
			if(sb.length()>1){
				sb.replace(sb.length()-1, sb.length(), "");
			}
			sb.append("]");

			logger.info(sb.toString());
			return sb.toString();

		} catch (IOException e1) {
			return bamDir.getAbsolutePath() +"not exists.";
		}


	}

}
