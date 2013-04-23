package org.bioinfo.opencga.lib.storage.indices;

import java.util.*;

import org.bioinfo.opencga.lib.storage.XObject;

public class DefaultParser {

	private String fieldSeparator;
    XObject indices;


    public DefaultParser(XObject indices) {
        this(indices, "\t");
    }

    public DefaultParser(XObject indices, String fieldSeparator) {
        this.indices = indices;
        this.fieldSeparator = fieldSeparator;
    }

	public XObject parse(String record) {
        XObject obj = new XObject();
		if(record != null) {
			String[] fields = record.split(fieldSeparator, -1);
            Set<String> names = indices.keySet();
            for (String colName : names) {
                int colIndex = indices.getInt(colName);
                if(colIndex>=0){
                    obj.put(colName,fields[indices.getInt(colName)]);
                }
            }
		}
		return obj;
	}
}
