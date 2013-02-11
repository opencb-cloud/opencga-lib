package org.bioinfo.gcsa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Config {

	private static Logger logger = Logger.getLogger(Config.class);

	private static String gcsaHome = System.getenv("GCSA_HOME");
	private static boolean log4jReady = false;

	private static Properties accountProperties = null;
	private static Properties analysisProperties = null;

	public static String getGcsaHome() {
		return gcsaHome;
	}

	public static void setGcsaHome(String gcsaHome) {
		Config.gcsaHome = gcsaHome;

		accountProperties = null;
		analysisProperties = null;

		log4jReady = false;
		LogManager.resetConfiguration();
		configureLog4j();
	}

	public static void configureLog4j() {
		if (!log4jReady) {
			Path path = Paths.get(gcsaHome, "conf", "log4j.properties");
			try {
				PropertyConfigurator.configure(Files.newInputStream(path));
			} catch (IOException e) {
				BasicConfigurator.configure();
				logger.warn("failed to load log4j.properties, BasicConfigurator will be used.");
			}
			log4jReady = true;
		}
	}

	public static Properties getAccountProperties() {
		if (accountProperties == null) {
			Path path = Paths.get(gcsaHome, "conf", "account.properties");
			accountProperties = new Properties();
			try {
				accountProperties.load(Files.newInputStream(path));
			} catch (IOException e) {
				logger.fatal("failed to load account.properties.");
				return null;
			}
		}
		return accountProperties;
	}

	public static Properties getAnalysisProperties() {
		if (analysisProperties == null) {
			Path path = Paths.get(gcsaHome, "conf", "analysis.properties");
			analysisProperties = new Properties();
			try {
				analysisProperties.load(Files.newInputStream(path));
			} catch (IOException e) {
				logger.fatal("failed to load analysis.properties.");
				return null;
			}
		}
		return analysisProperties;
	}

}
