package org.bioinfo.gcsa.lib.storage.feature;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bioinfo.formats.core.variant.Vcf4;
import org.bioinfo.gcsa.lib.storage.TabixReader;

import com.google.gson.Gson;

public class VcfManager {
	private Gson gson;
	private static Logger logger = Logger.getLogger(BamManager.class);

	public VcfManager() throws IOException {
		gson = new Gson();
	}

	public String getByRegion(Path fullFilePath, String regionStr, Map<String, List<String>> params) throws IOException {
		TabixReader tabixReader = new TabixReader(fullFilePath.toString());
		TabixReader.Iterator lines = tabixReader.query(regionStr);

		String line;
		while ((line = lines.next()) != null) {
			Vcf4 vcfBean = new Vcf4(line);
		}
		return regionStr;
	}
}
