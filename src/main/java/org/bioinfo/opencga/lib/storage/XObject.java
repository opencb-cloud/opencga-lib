package org.bioinfo.opencga.lib.storage;

import java.util.LinkedHashMap;
import java.util.Map;

public class XObject extends LinkedHashMap<String, Object> {


	private static final long serialVersionUID = -242187651119508127L;

	
	public XObject() {
		
	}

	public XObject(int size) {
		super(size); 
	}
	
	public XObject(String key, Object value) {
		put(key, value); 
	}
	
	public XObject(Map<String, Object> map) {
		super(map);
	}


	public boolean containsField(String key) {
		return this.containsKey(key);
	}
	
	public Object removeField(String key) {
		return this.remove(key);
	}
	
	public Object get(String key ){
        return super.get(key);
    }
	
	
	public String getString(String field) {
		return getString(field, "");
	}

	public String getString(String field, String defaultValue) {
		if(field != null && this.containsKey(field)) {
			return String.valueOf(this.get(field));
		}
		return defaultValue;
	}


	public int getInt(String field) {
		return getInt(field, 0);
	}

	public int getInt(String field, int defaultValue) {
		if(field != null && this.containsKey(field)) {
			Object obj = this.get(field);
			if(obj instanceof Integer) {
				return (Integer)obj;
			}else{
				return Integer.parseInt(String.valueOf(obj));								
			}
		}
		return defaultValue;
	}


    public long getLong(String field) {
        return getLong(field, 0L);
    }

    public long getLong(String field, long defaultValue) {
        if(field != null && this.containsKey(field)) {
            Object obj = this.get(field);
            if(obj instanceof Long) {
                return (Long)obj;
            }else{
                return Long.parseLong(String.valueOf(obj));
            }
        }
        return defaultValue;
    }


	public float getFloat(String field) {
		return getFloat(field, 0.0f);
	}

	public float getFloat(String field, float defaultValue) {
		if(field != null && this.containsKey(field)) {
            Object obj = this.get(field);
            if(obj instanceof Float) {
                return (Float)obj;
            }else{
                return Float.parseFloat(String.valueOf(obj));
            }
		}
		return defaultValue;
	}


	public double getDouble(String field) {
		return getDouble(field, 0.0);
	}

	public double getDouble(String field, double defaultValue) {
		if(field != null && this.containsKey(field)) {
            Object obj = this.get(field);
            if(obj instanceof Double) {
                return (Double)obj;
            }else{
                return Double.parseDouble(String.valueOf(obj));
            }
		}
		return defaultValue;
	}

	
	public boolean getBoolean(String field) {
		return getBoolean(field, false);
	}

	public boolean getBoolean(String field, boolean defaultValue) {
		if(field != null && this.containsKey(field)) {
            Object obj = this.get(field);
            if(obj instanceof Boolean) {
                return (Boolean)obj;
            }else{
                return Boolean.parseBoolean(String.valueOf(obj));
            }
		}
		return defaultValue;
	}

}
