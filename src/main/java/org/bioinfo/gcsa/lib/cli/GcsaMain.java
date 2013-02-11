package org.bioinfo.gcsa.lib.cli;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.cli.Options;


public class GcsaMain {

	public static Tomcat tomcat; 
	protected Options options;

	public static void main(String[] args) {

		tomcat = new Tomcat();
		tomcat.setPort(33333);  

		Context ctx = tomcat.addContext("/gcsa/rest/storage", new File(".").getAbsolutePath());  

		Tomcat.addServlet(ctx, "fetch", new FetchServlet());  
		ctx.addServletMapping("/fetch", "fetch");  

		Tomcat.addServlet(ctx, "admin", new FetchServlet());  
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
