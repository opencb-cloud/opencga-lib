package org.bioinfo.gcsa.lib.account.io;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.commons.io.utils.IOUtils;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.commons.utils.ArrayUtils;
import org.bioinfo.commons.utils.ListUtils;
import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.gcsa.lib.account.beans.ObjectItem;
import org.bioinfo.tool.result.Result;
import org.dom4j.DocumentException;

import com.google.gson.Gson;

public class FileIOManager implements IOManager {

	private Logger logger;
	private Properties properties;

	private String appHomePath;
	private String accountHomePath;
	private String tmp;

	private static String BUCKETS_FOLDER = "buckets";
	private static String ANALYSIS_FOLDER = "analysis";
	private static String JOBS_FOLDER = "jobs";

	public FileIOManager(Properties properties) {
		logger = new Logger();
		logger.setLevel(Logger.DEBUG_LEVEL);
		this.properties = properties;
		appHomePath = System.getenv(properties.getProperty("GCSA.ENV.HOME"));
		accountHomePath = appHomePath + properties.getProperty("GCSA.ACCOUNT.PATH");
		tmp = properties.getProperty("TMP.PATH");
	}

	public void createAccount(String accountId) throws IOManagementException {

		// get Java 7 Path object, concatenate home and account
		Path accountPath = Paths.get(accountHomePath, accountId);

		// If account folder not exist is created
		if (!Files.exists(accountPath)) {
			try {
				Files.createDirectory(accountPath);
			} catch (IOException e) {
				throw new IOManagementException("IOException" + e.toString());
			}
		}

		logger.debug("createAccount(): Creating account folder: " + accountHomePath);
		if (Files.exists(accountPath) && Files.isDirectory(accountPath) && Files.isWritable(accountPath)) {

			try {
				Files.createDirectory(Paths.get(accountHomePath, accountId, FileIOManager.BUCKETS_FOLDER));
				createBucket(accountId, "default");
				Files.createDirectory(Paths.get(accountHomePath, accountId, FileIOManager.ANALYSIS_FOLDER));
				Files.createDirectory(Paths.get(accountHomePath, accountId, FileIOManager.JOBS_FOLDER));
			} catch (IOException e) {
				try {
					IOManagerUtils.deleteDirectory(accountPath);
				} catch (IOException e1) {
					throw new IOManagementException("IOException: " + e1.toString());
				}
				throw new IOManagementException("IOException: " + e.toString());
			}

			// try {
			// FileUtils.createDirectory(accountHomePath + "/" + accountId);
			// logger.info("account scaffold created");
			// } catch (IOException e1) {
			// FileUtils.deleteDirectory(new File(accountHomePath + "/" +
			// accountId));
			// FileUtils.deleteDirectory(new File(accountHomePath));
			// throw new IOManagementException("IOException" + e1.toString());
			// }
			//
			// try {
			// FileUtils.createDirectory(accountHomePath + "/" + accountId +
			// "/analysis");
			// } catch (IOException e1) {
			// FileUtils.deleteDirectory(new File(accountHomePath + "/" +
			// accountId + "/analysis"));
			// FileUtils.deleteDirectory(new File(accountHomePath + "/" +
			// accountId));
			// FileUtils.deleteDirectory(new File(accountHomePath));
			// throw new IOManagementException("IOException" + e1.toString());
			// }
			//
			// try {
			// FileUtils.createDirectory(accountHomePath + "/" + accountId +
			// "/buckets");
			// } catch (IOException e1) {
			// FileUtils.deleteDirectory(new File(accountHomePath + "/" +
			// accountId + "/buckets"));
			// FileUtils.deleteDirectory(new File(accountHomePath + "/" +
			// accountId + "/analysis"));
			// FileUtils.deleteDirectory(new File(accountHomePath + "/" +
			// accountId));
			// FileUtils.deleteDirectory(new File(accountHomePath));
			// throw new IOManagementException("IOException" + e1.toString());
			// }
			//
			// try {
			// FileUtils.createDirectory(accountHomePath + "/" + accountId +
			// "/buckets/default");
			// } catch (IOException e1) {
			// throw new IOManagementException("IOException" + e1.toString());
			// }
			//
			// try {
			// FileUtils.createDirectory(getAccountPath(accountId) + "/jobs");
			// } catch (IOException e) {
			// throw new IOManagementException("IOException" + e.toString());
			// }

		} else {
			throw new IOManagementException("ERROR: The account folder has not been created ");
		}

	}

	public void deleteAccount(String accountId) throws IOManagementException {
		Path accountPath = Paths.get(accountHomePath, accountId);
		try {
			IOManagerUtils.deleteDirectory(accountPath);
		} catch (IOException e1) {
			throw new IOManagementException("IOException: " + e1.toString());
		}
	}

	/********************
	 * 
	 * BUCKETS METHODS
	 * 
	 ********************/
	public URI createBucket(String accountId, String bucketId) throws IOManagementException {
		// String path = getBucketPath(accountId, bucketId);
		Path bucketFolder = Paths.get(accountHomePath, accountId, FileIOManager.BUCKETS_FOLDER, bucketId);
		if (Files.exists(bucketFolder.getParent()) && Files.isDirectory(bucketFolder.getParent())
				&& Files.isWritable(bucketFolder.getParent())) {
			try {
				bucketFolder = Files.createDirectory(bucketFolder);
			} catch (IOException e) {
				// FileUtils.deleteDirectory(new File(path));
				throw new IOManagementException("createBucket(): could not create the bucket folder: " + e.toString());
			}
		}
		return bucketFolder.toUri();
	}

	public void deleteBucket(String accountId, String bucketId) throws IOManagementException {
		// String path = getBucketPath(accountId, bucketId);
		Path bucketFolder = Paths.get(accountHomePath, accountId, FileIOManager.BUCKETS_FOLDER, bucketId);
		try {
			IOManagerUtils.deleteDirectory(bucketFolder);
		} catch (IOException e) {
			throw new IOManagementException("deleteBucket(): could not delete the bucket folder: " + e.toString());
		}
	}

	public void renameBucket(String accountId, String oldBucketId, String newBucketId) throws IOManagementException {
		Path oldBucketFolder = Paths.get(accountHomePath, accountId, FileIOManager.BUCKETS_FOLDER, oldBucketId);
		Path newBucketFolder = Paths.get(accountHomePath, accountId, FileIOManager.BUCKETS_FOLDER, newBucketId);
		try {
			Files.move(oldBucketFolder, newBucketFolder);
		} catch (IOException e) {
			throw new IOManagementException("deleteBucket(): could not rename the bucket folder: " + e.toString());
		}
	}

	public boolean existBucket(String accountId, String bucketId) throws IOManagementException {
		return Files.exists(getBucketPath(accountId, bucketId));
	}

	/********************
	 * 
	 * JOBS METHODS
	 * 
	 ********************/
	public URI createJob(String accountId, String jobId) throws IOManagementException {
		// String path = getAccountPath(accountId) + "/jobs";
		Path jobFolder = getJobPath(accountId, null, jobId);
		logger.debug("PAKO " + jobFolder);

		if (Files.exists(jobFolder.getParent()) && Files.isDirectory(jobFolder.getParent())
				&& Files.isWritable(jobFolder.getParent())) {
			try {
				Files.createDirectory(jobFolder);
			} catch (IOException e) {
				throw new IOManagementException("createJob(): could not create the job folder: " + e.toString());
			}
		} else {
			throw new IOManagementException("createJob(): 'jobs' folder not writable");
		}
		return jobFolder.toUri();
	}

	public void removeJob(String accountId, String jobId) throws IOManagementException {
		Path jobFolder = getJobPath(accountId, null, jobId);

		try {
			IOManagerUtils.deleteDirectory(jobFolder);
		} catch (IOException e) {
			throw new IOManagementException("removeJob(): could not delete the job folder: " + e.toString());
		}
	}

	public void removeJobObjects(String accountId, String bucketId, String jobId, List<String> objects)
			throws IOManagementException {
		Path jobFolder = getJobPath(accountId, bucketId, jobId);

		try {
			if (objects != null && objects.size() > 0) {
				for (String object : objects) {
					Files.delete(Paths.get(jobFolder.toString(), object));
				}
			}
		} catch (IOException e) {
			throw new IOManagementException("removeJobObjects(): could not delete the job folder: " + e.toString());
		}
	}

	public void moveJob(String accountId, String oldBucketId, String oldJobId, String newBucketId, String newJobId)
			throws IOManagementException {
		Path oldBucketFolder = getJobPath(accountId, oldBucketId, oldJobId);
		Path newBucketFolder = getJobPath(accountId, newBucketId, newJobId);

		try {
			Files.move(oldBucketFolder, newBucketFolder);
		} catch (IOException e) {
			throw new IOManagementException("deleteBucket(): could not rename the bucket folder: " + e.toString());
		}
	}

	// TODO tener en cuenta las demás implementaciones de la interfaz.
	public Path getJobPath(String accountId, String bucketId, String jobId) {
		Path jobFolder;
		// If a bucket is passed then outdir is set
		if (bucketId != null && !bucketId.equals("")) {
			jobFolder = Paths.get(accountHomePath, accountId, FileIOManager.BUCKETS_FOLDER, jobId);
		} else {
			jobFolder = Paths.get(accountHomePath, accountId, FileIOManager.JOBS_FOLDER, jobId);
		}
		return jobFolder;
	}

	/********************
	 * 
	 * OBJECT METHODS
	 * 
	 ********************/
	public Path createFolder(String accountId, String bucketId, Path objectId, boolean parents)
			throws IOManagementException {

		Path fullFolderPath = getObjectPath(accountId, bucketId, objectId);

		try {
			if (existBucket(accountId, bucketId)) {
				if (Files.exists(fullFolderPath.getParent()) && Files.isDirectory(fullFolderPath.getParent())
						&& Files.isWritable(fullFolderPath.getParent())) {
					Files.createDirectory(fullFolderPath);
				} else {
					if (parents) {
						Files.createDirectories(fullFolderPath);
					} else {
						throw new IOManagementException("createFolder(): path do no exist");
					}
				}
			} else {
				throw new IOManagementException("createFolder(): bucket '" + bucketId + "' do no exist");
			}
		} catch (IOException e) {
			throw new IOManagementException("createFolder(): could not create the directory " + e.toString());
		}

		return objectId;
	}

	public Path createObject(String accountId, String bucketId, Path objectId, ObjectItem objectItem,
			InputStream fileIs, boolean parents) throws IOManagementException, IOException {

		Path fullFilePath = getObjectPath(accountId, bucketId, objectId);
		
		// if parents is
		// true, folders
		// will be
		// autocreated
		if (!parents && !Files.exists(fullFilePath.getParent())) {
			throw new IOManagementException("createObject(): folder '" + fullFilePath.getParent().getFileName()
					+ "' not exists");
		}

		// check if file exists and update fullFilePath and objectId
		fullFilePath = renameExistingFileIfNeeded(fullFilePath);
		objectId = getBucketPath(accountId, bucketId).relativize(fullFilePath);

		// creating a random tmp folder
		String rndStr = StringUtils.randomString(20);
		Path randomFolder = Paths.get(tmp, rndStr);
		Path tmpFile = randomFolder.resolve(fullFilePath.getFileName());

		try {
			Files.createDirectory(randomFolder);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOManagementException("createObject(): Could not create the upload temp directory");
		}
		try {
			Files.copy(fileIs, tmpFile);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOManagementException("createObject(): Could not write the file on disk");
		}
		try {
			Files.copy(tmpFile, fullFilePath);
			objectItem.setDiskUsage(Files.size(fullFilePath));
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOManagementException("createObject(): Copying from tmp folder to bucket folder");
		}
		IOManagerUtils.deleteDirectory(randomFolder);
		return objectId;
	}

	public Path deleteObject(String accountId, String bucketId, Path objectId) throws IOManagementException {
		Path fullFilePath = getObjectPath(accountId, bucketId, objectId);
		try {
			if (Files.deleteIfExists(fullFilePath)) {
				return objectId;
			} else {
				throw new IOManagementException("could not delete the object");
			}
		} catch (IOException e) {
			throw new IOManagementException("deleteObject(): could not delete the object " + e.toString());
		}
	}

	public String getJobResult(Path jobPath) throws DocumentException, IOManagementException, IOException {
		Path resultFile = jobPath.resolve("result.xml");

		if (Files.exists(resultFile)) {
			Result resultXml = new Result();
			resultXml.loadXmlFile(resultFile.toAbsolutePath().toString());
			Gson g = new Gson();
			String resultJson = g.toJson(resultXml);
			return resultJson;
		} else {
			throw new IOManagementException("getJobResultFromBucket(): the file '" + resultFile + "' not exists");
		}
	}

	public String getFileTableFromJob(Path jobPath, String filename, String start, String limit, String colNames,
			String colVisibility, String callback, String sort) throws IOManagementException, IOException {

		int first = Integer.parseInt(start);
		int end = first + Integer.parseInt(limit);
		String[] colnamesArray = colNames.split(",");
		String[] colvisibilityArray = colVisibility.split(",");

		// Path jobPath = getJobPath(accountId, bucketId, jobId);
		Path jobFile = jobPath.resolve(filename);

		if (!Files.exists(jobFile)) {
			throw new IOManagementException("getFileTableFromJob(): the file '" + jobFile.toAbsolutePath()
					+ "' not exists");
		}

		String name = filename.replace("..", "").replace("/", "");
		List<String> avoidingFiles = getAvoidingFiles();
		if (avoidingFiles.contains(name)) {
			throw new IOManagementException("getFileTableFromJob(): No permission to use the file '"
					+ jobFile.toAbsolutePath() + "'");
		}

		StringBuilder stringBuilder = new StringBuilder();

		int totalCount = -1;
		List<String> headLines;
		try {
			headLines = IOUtils.head(jobFile.toFile(), 30);
		} catch (IOException e) {
			throw new IOManagementException("getFileTableFromJob(): could not head the file '"
					+ jobFile.toAbsolutePath() + "'");
		}
		Iterator<String> headIterator = headLines.iterator();
		while (headIterator.hasNext()) {
			String line = headIterator.next();
			if (line.startsWith("#NUMBER_FEATURES")) {
				totalCount = Integer.parseInt(line.split("\t")[1]);
				break;
			}
		}

		logger.debug("totalCount ---after read head lines ---------> " + totalCount);

		if (totalCount == -1) {
			logger.debug("totalCount ---need to count all lines and prepend it---------> " + totalCount);

			int numFeatures = 0;
			BufferedReader br = Files.newBufferedReader(jobFile, Charset.defaultCharset());
			// BufferedReader br = new BufferedReader(new InputStreamReader(new
			// FileInputStream(jobFile.toFile())));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (!line.startsWith("#")) {
					numFeatures++;
				}
			}
			totalCount = numFeatures;
			String text = "#NUMBER_FEATURES	" + numFeatures;
			IOUtils.prepend(jobFile.toFile(), text);
		}

		if (!sort.equals("false")) {
			// Para obtener el numero de la columna a partir del
			// nombre que viene del store de Sencha
			Map<String, Integer> indexColumn = new HashMap<String, Integer>();
			for (int i = 0; i < colnamesArray.length; i++) {
				indexColumn.put(colnamesArray[i], i);
			}

			// Parsear y obtener el nombre de la columna que envia
			// Sencha
			// logger.info("PAKO::SORT1: "+ sort);
			Gson gson = new Gson();
			TableSort[] datos = gson.fromJson(sort, TableSort[].class);
			logger.info("PAKO:SORT: " + Arrays.toString(datos));
			int numColumn = indexColumn.get(datos[0].getProperty());
			String direction = datos[0].getDirection();
			logger.info("PAKO:SORT:NUMCOLUMN " + numColumn);

			boolean decreasing = false;
			if (direction.equals("DESC")) {
				decreasing = true;
			}

			List<String> dataFile = IOUtils.grep(jobFile.toFile(), "^[^#].*");
			double[] numbers = ListUtils.toDoubleArray(IOUtils.column(jobFile.toFile(), numColumn, "\t", "^[^#].*"));
			int[] orderedRowIndices = ArrayUtils.order(numbers, decreasing);

			String[] fields;
			stringBuilder.append(callback + "({\"total\":\"" + totalCount + "\",\"items\":[");
			for (int j = 0; j < orderedRowIndices.length; j++) {
				if (j >= first && j < end) {
					fields = dataFile.get(orderedRowIndices[j]).split("\t");
					stringBuilder.append("{");
					for (int i = 0; i < fields.length; i++) {
						if (Integer.parseInt(colvisibilityArray[i].toString()) == 1) {
							stringBuilder.append("\"" + colnamesArray[i] + "\":\"" + fields[i] + "\",");
						}
					}
					stringBuilder.append("}");
					stringBuilder.append(",");
				} else {
					if (j >= end) {
						break;
					}
				}
			}
			stringBuilder.append("]});");

		} else {// END SORT

			int numLine = 0;
			String line = null;
			String[] fields;
			BufferedReader br = Files.newBufferedReader(jobFile, Charset.defaultCharset());
			stringBuilder.append(callback + "({\"total\":\"" + totalCount + "\",\"items\":[");
			while ((line = br.readLine()) != null) {
				if (!line.startsWith("#")) {
					if (numLine >= first && numLine < end) {
						fields = line.split("\t");
						stringBuilder.append("{");
						logger.info("PAKO::length: " + fields.length);
						for (int i = 0; i < fields.length; i++) {
							// logger.info("PAKO::colvisibilityArray[i]: "+
							// colvisibilityArray[i]);
							// logger.info("PAKO::fields[i]: "+
							// fields[i]);
							if (Integer.parseInt(colvisibilityArray[i].toString()) == 1) {
								stringBuilder.append("\"" + colnamesArray[i] + "\":\"" + fields[i] + "\",");
							}
						}
						stringBuilder.append("}");
						stringBuilder.append(",");
					} else {
						if (numLine >= end) {
							break;
						}
					}
					numLine++;
				}
			}
			stringBuilder.append("]});");
		}
		return stringBuilder.toString();
	}

	public DataInputStream getFileFromJob(Path jobPath, String filename, String zip) throws IOManagementException,
			FileNotFoundException {

		// String fileStr = getJobPath(accountId, bucketId, jobId).toString();
		Path filePath = jobPath.resolve(filename);
		File file = filePath.toFile();
		String name = filename.replace("..", "").replace("/", "");
		List<String> avoidingFiles = getAvoidingFiles();
		if (avoidingFiles.contains(name)) {
			throw new IOManagementException("No permission to use that file: " + file.getAbsolutePath());
		}
		try {
			FileUtils.checkFile(file);
		} catch (IOException e) {
			throw new IOManagementException("File not found: " + file.getAbsolutePath());
		}

		if (zip.compareTo("true") != 0) {// PAKO zip != true
			DataInputStream is = new DataInputStream(new FileInputStream(file));
			return is;
		} else {// PAKO zip=true, create the zip file
			String randomFolder = StringUtils.randomString(20);
			try {
				FileUtils.createDirectory(tmp + "/" + randomFolder);
			} catch (IOException e) {
				throw new IOManagementException("Could not create the random folder '" + randomFolder + "'");
			}
			File zipfile = new File(tmp + "/" + randomFolder + "/" + filename + ".zip");
			File arr[] = new File[] { file }; // PAKO creo un array con un único
			// file porq zipFiles recibe un
			// array de files
			try {
				FileUtils.zipFiles(arr, zipfile);
			} catch (IOException e) {
				throw new IOManagementException("Could not zip the file '" + file.getName() + "'");
			}// PAKO comprimir
			logger.debug("checking file: " + zipfile.getName());
			try {
				FileUtils.checkFile(zipfile);
			} catch (IOException e) {
				throw new IOManagementException("Could not find zipped file '" + zipfile.getName() + "'");
			}
			logger.debug("file " + zipfile.getName() + " exists");
			DataInputStream is = new DataInputStream(new FileInputStream(zipfile));
			return is;
		}
	}

	/**************/

	public Path getAccountPath(String accountId) {
		return Paths.get(accountHomePath, accountId);
	}

	public Path getBucketPath(String accountId, String bucketId) {
		if (bucketId != null) {
			return getAccountPath(accountId).resolve(Paths.get(FileIOManager.BUCKETS_FOLDER, bucketId.toLowerCase()));
		}
		return getAccountPath(accountId).resolve(FileIOManager.BUCKETS_FOLDER);
	}

	public Path getObjectPath(String accountId, String bucketId, Path objectId) {
		return getBucketPath(accountId, bucketId).resolve(objectId);
	}

	// public String getDataPath(String wsDataId){
	// wsDataId.replaceAll(":", "/")
	// }

	// public String getJobPath(String accountId, String bucketId, String jobId)
	// {
	// return getAccountPath(accountId) + "/jobs/" + jobId;
	// }

	private Path renameExistingFileIfNeeded(Path fullFilePath) {
		if (Files.exists(fullFilePath)) {
			String file = fullFilePath.getFileName().toString();
			Path parent = fullFilePath.getParent();
			String fileName = FileUtils.removeExtension(file);
			String fileExt = FileUtils.getExtension(file);
			String newname = null;
			if (fileName != null && fileExt != null) {
				newname = fileName + "-copy" + fileExt;
			} else {
				newname = file + "-copy";
			}
			return renameExistingFileIfNeeded(parent.resolve(newname));
		} else {
			return fullFilePath;
		}
	}

	/****/
	private List<String> getAvoidingFiles() {
		List<String> avoidingFiles = new ArrayList<String>();
		avoidingFiles.add("cli.txt");
		avoidingFiles.add("form.txt");
		avoidingFiles.add("input_params.txt");
		avoidingFiles.add("job.log");
		avoidingFiles.add("jobzip.zip");
		return avoidingFiles;
	}

	private class TableSort {
		private String property;
		private String direction;

		public TableSort() {
		}

		public TableSort(String prop, String direct) {
			this.setProperty(prop);
			this.setDirection(direct);
		}

		@Override
		public String toString() {
			return property + "::" + direction;
		}

		public String getProperty() {
			return property;
		}

		public void setProperty(String property) {
			this.property = property;
		}

		public String getDirection() {
			return direction;
		}

		public void setDirection(String direction) {
			this.direction = direction;
		}

	}

	/*******/

	public StringBuilder listRecursiveJson(File file) {
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
		if (file.isDirectory()) {
			sb.append(",");
			sb.append(c + "children" + c + ":[");
			File[] files = file.listFiles();
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
