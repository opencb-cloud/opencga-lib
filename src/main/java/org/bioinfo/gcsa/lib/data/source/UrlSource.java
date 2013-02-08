package org.bioinfo.gcsa.lib.data.source;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.net.ssl.HttpsURLConnection;

public class UrlSource implements Source{
 
	public InputStream getInputStream(String path) {
		try {
			URL url = new URL(path);
			if("https".equals(url.getProtocol())){
				HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
				return con.getInputStream();
			}else{
				HttpURLConnection con = (HttpURLConnection)url.openConnection();
				return con.getInputStream();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
