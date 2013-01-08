package org.bioinfo.gcsa.lib.account.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class IOManagerUtils {

	public static void deleteDirectory(Path path) throws IOException {

		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				// try to delete the file anyway, even if its attributes
				// could not be read, since delete-only access is
				// theoretically possible
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				if (exc == null) {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}else {
					// directory iteration failed; propagate exception
					throw exc;
				}
			}
		});
	}
	
	public static String toString(File file) throws IOException {
		BufferedReader bufferedReader = new BufferedReader( new FileReader (file));
		StringBuilder result = new StringBuilder();
		String line = "";
		while ((line=bufferedReader.readLine()) != null) {
			result.append(line).append(System.getProperty("line.separator"));
		}
		bufferedReader.close();
		return result.toString().trim();
	}
}
