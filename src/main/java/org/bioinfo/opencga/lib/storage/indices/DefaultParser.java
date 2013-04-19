package org.bioinfo.opencga.lib.storage.indices;

import java.util.List;
import java.util.Map;

import org.bioinfo.opencga.lib.storage.XObject;

public class DefaultParser {

	private String fieldSeparator;
	private List<String> columnNames;
	private List<String> columnTypes;
	private List<Map<String, String>> columnIndices;	// {"chr_chunk_idx" => "chr,chunk"}
	
	public DefaultParser() {
		this.fieldSeparator = "\t";
	}
	
	public DefaultParser(String fieldSeparator) {
		this.fieldSeparator = fieldSeparator;
	}
	
	public XObject parse(String filename) {
		if(filename != null) {
			String[] fields = record.split(fieldSeparator, -1);
			
		}
		
		return null;
	}

}
