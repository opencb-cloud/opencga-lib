package org.bioinfo.opencga.lib.storage.indices;

import java.util.*;

import org.bioinfo.opencga.lib.storage.XObject;

public class DefaultParser {

	private String fieldSeparator;
    XObject columns;


    public DefaultParser(XObject indices) {
        this(indices, "\t");
    }

    public DefaultParser(XObject columns, String fieldSeparator) {
        this.columns = columns;
        this.fieldSeparator = fieldSeparator;
    }

	public XObject parse(String record) {
        XObject obj = new XObject();
		if(record != null) {
			String[] fields = record.split(fieldSeparator, -1);
            Set<String> names = columns.keySet();
            for (String colName : names) {
                int colIndex = columns.getInt(colName);
                if(colIndex>=0){
                    obj.put(colName, fields[columns.getInt(colName)]);
                }
            }
		}
		return obj;
	}
}
