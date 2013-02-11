package org.bioinfo.gcsa.lib.cli;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.LifecycleException;
import org.bioinfo.gcsa.lib.storage.feature.BamManager;


public class FetchServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final String OUTPUT_DIRECTORY = "../output";

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		PrintWriter pw = resp.getWriter();
		System.err.println("en el servlet");
		//		pw.write("toma ya!!");

		
		if(true) {
			try {
				GcsaMain.stop();
			} catch (LifecycleException e) {
				e.printStackTrace();
			}
		}
		
		String filePath = req.getParameter("filepath").replace(":", "/");
		String region = req.getParameter("region");

		System.err.println("filepath: "+filePath+", exists: "+Files.exists(Paths.get(filePath)));
		System.err.println("region: "+region);

		req.getParameterMap();
		Map<String, List<String>> params = new HashMap<>();
		BamManager bamManager = new BamManager();
		pw.write(bamManager.getByRegion(Paths.get(filePath), region, params));


		pw.close();
	}


}
