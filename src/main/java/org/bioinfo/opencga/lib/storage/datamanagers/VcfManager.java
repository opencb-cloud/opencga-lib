package org.bioinfo.opencga.lib.storage.datamanagers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bioinfo.formats.core.variant.Vcf4;
import org.bioinfo.formats.core.variant.vcf4.VcfRecord;
import org.bioinfo.opencga.lib.storage.TabixReader;
import org.bioinfo.opencga.lib.storage.datamanagers.bam.BamManager;

import com.google.gson.Gson;

public class VcfManager {
	private Gson gson;
	private static Logger logger = Logger.getLogger(BamManager.class);

	public VcfManager() throws IOException {
		gson = new Gson();
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
