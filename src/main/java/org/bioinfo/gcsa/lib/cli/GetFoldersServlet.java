package org.bioinfo.gcsa.lib.cli;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
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
			Path path = Paths.get(folder);
			respStr.append(listRecursiveJson(path).toString()).append(',');
		}
		respStr.deleteCharAt(respStr.length() - 1);
		respStr.append("]");

		pw.write(respStr.toString());
		pw.close();
	}

	private StringBuilder listRecursiveJson(Path path) throws IOException {
		return listRecursiveJson(path, false);
	}

	private StringBuilder listRecursiveJson(Path filePath, boolean coma) throws IOException {
		String c = "\"";
		StringBuilder sb = new StringBuilder();
		if (coma) {
			sb.append(",");
		}
		sb.append("{");
		sb.append(c + "text" + c + ":" + c + filePath.toString() + c);
		if (Files.isDirectory(filePath)) {
			sb.append(",");
			sb.append(c + "children" + c + ":[");
			DirectoryStream<Path> folderStream = Files.newDirectoryStream(filePath);
			int i = 0;
			for (Path p : folderStream) {
				System.out.println(p.toAbsolutePath());
				if (i == 0) {
					sb.append(listRecursiveJson(p, false));
				} else {
					sb.append(listRecursiveJson(p, true));
				}
				i++;
			}
			return sb.append("]}");
		}
		return sb.append("}");
	}
}
