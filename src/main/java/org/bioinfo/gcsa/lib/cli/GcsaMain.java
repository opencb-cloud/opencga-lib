package org.bioinfo.gcsa.lib.cli;

import java.io.File;
import java.util.Properties;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.bioinfo.gcsa.Config;

public class GcsaMain {

	public static Tomcat tomcat;

	public static void main(String[] args) {

		String home = args[0];
		if (home == null || home.trim().equals("")) {
			home = ".";
		}

		Properties properties = Config.getLocalServerProperties(home);
		int port = Integer.parseInt(properties.getProperty("LOCAL.PORT", "61976"));

		tomcat = new Tomcat();
		tomcat.setPort(port);

		Context ctx = tomcat.addContext("/gcsa/rest", new File(".").getAbsolutePath());

		Tomcat.addServlet(ctx, "fetch", new FetchServlet());
		ctx.addServletMapping("/storage/fetch", "fetch");

		Tomcat.addServlet(ctx, "admin", new AdminServlet());
		ctx.addServletMapping("/admin", "admin");
		
		try {
			tomcat.start();
			tomcat.getServer().await();
		} catch (LifecycleException e) {
			e.printStackTrace();
		}
	}

	public static void stop() throws LifecycleException {
		tomcat.stop();
	}
}
