package org.bioinfo.gcsa.lib.storage.datasource;

import java.io.InputStream;

public interface Source {
	public InputStream getInputStream(String path);
}
