package org.bioinfo.gcsa.lib.data.source;

import java.io.InputStream;

public interface Source {
	public InputStream getInputStream(String path);
}
