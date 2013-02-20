package org.bioinfo.gcsa.lib.cli;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetFoldersServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter pw = resp.getWriter();

		StringBuilder respStr = new StringBuilder("[");
		String[] folders = GcsaMain.properties.getProperty("LOCAL.ALLOWED.FOLDERS").split(";");
		for (String folder : folders) {
			Path path = Paths.get(folder.toString());
			respStr.append(listRecursiveJson(path.toFile()).toString()).append(',');
		}
		respStr.deleteCharAt(respStr.length() - 1);
		respStr.append("]");
		
		pw.write(respStr.toString());
		pw.close();
	}

	private StringBuilder listRecursiveJson(File file) {
		return listRecursiveJson(file, false);
	}

	private StringBuilder listRecursiveJson(File file, boolean coma) {
		String c = "\"";
		StringBuilder sb = new StringBuilder();
		if (coma) {
			sb.append(",");
		}
		sb.append("{");
		sb.append(c + "text" + c + ":" + c + file.getName() + c);
		File[] files;
		if (file.isDirectory() && (files = file.listFiles()) !=null ) {
			sb.append(",");
			sb.append(c + "children" + c + ":[");
			for (int i = 0; i < files.length; i++) {
				if (i == 0) {
					sb.append(listRecursiveJson(files[i], false));
				} else {
					sb.append(listRecursiveJson(files[i], true));
				}
			}
			return sb.append("]}");
		}
		return sb.append("}");
	}
}
