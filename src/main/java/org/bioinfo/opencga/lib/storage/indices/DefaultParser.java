package org.bioinfo.opencga.lib.storage.indices;

import java.util.*;

import org.bioinfo.opencga.lib.storage.XObject;

public class DefaultParser {

	private String fieldSeparator;
    LinkedHashMap<String,Integer> indices;


    public DefaultParser(LinkedHashMap<String,Integer> indices) {
        this(indices, "\t");
    }

    public DefaultParser(LinkedHashMap<String,Integer> indices, String fieldSeparator) {
        this.indices = indices;
        this.fieldSeparator = fieldSeparator;
    }

	public XObject parse(String record) {
        XObject obj = new XObject();
		if(record != null) {
			String[] fields = record.split(fieldSeparator, -1);
            Set<String> names = indices.keySet();
            for (String colName : names) {
                obj.put(colName,fields[indices.get(colName)]);
            }
		}
		return obj;
	}
}
