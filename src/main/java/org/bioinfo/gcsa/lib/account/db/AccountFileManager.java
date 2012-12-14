package org.bioinfo.gcsa.lib.account.db;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.gcsa.lib.account.beans.Data;
import org.bioinfo.gcsa.lib.account.beans.Plugin;
import org.bioinfo.gcsa.lib.account.beans.Bucket;
import org.bioinfo.gcsa.lib.account.beans.Session;

public class AccountFileManager /*implements AccountManager*/ {

	private Logger logger;
	private Properties properties;

	private String home;
	private String accounts;
	private String tmp;

	public AccountFileManager(Properties properties) {
		logger = new Logger();
		logger.setLevel(Logger.INFO_LEVEL);
		this.properties = properties;
		home = System.getenv(properties.getProperty("GCSA.ENV.HOME"));
		accounts = home + properties.getProperty("GCSA.ACCOUNT.PATH");
		tmp = properties.getProperty("TMP.PATH");
	}
}
