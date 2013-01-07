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
		logger.setLevel(Logger.INFO_LEVEL);
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
		return Files.exists(Paths.get(accountHomePath, accountId, FileIOManager.BUCKETS_FOLDER, bucketId));
	}

	/********************
	 * 
	 * JOBS METHODS
	 * 
	 ********************/
	public URI createJob(String accountId, String bucketId, String jobId) throws IOManagementException {
		// String path = getAccountPath(accountId) + "/jobs";
		Path jobFolder = getJobPath(accountId, bucketId, jobId);

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

	public void removeJob(String accountId, String bucketId, String jobId) throws IOManagementException {
		Path jobFolder = getJobPath(accountId, bucketId, jobId);

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
			throw new IOManagementException("removeJob(): could not delete the job folder: " + e.toString());
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
	 * OTHER METHODS
	 * 
	 ********************/
	public Path createFolder(String accountId, String bucketId, String objectId, boolean parents)
			throws IOManagementException {
		// String idStr = objectId.replace(":", "/");
		// String fileName = getObjectName(idStr);

		// String userFileStr = getBucketPath(accountId, bucketId) + "/" +
		// idStr;
		Path folder = Paths.get(accountHomePath, accountId, FileIOManager.BUCKETS_FOLDER, bucketId, objectId);
		// File userFile = new File(userFileStr);

		// logger.debug("IOManager: " + userFile.getAbsolutePath());
		// if (!parents && !userFile.getParentFile().exists()) {
		// throw new IOManagementException("no such folder");
		// }
		//
		// if (userFile.exists()) {
		// throw new IOManagementException("folder already exists");
		// }

		try {
			if (existBucket(accountId, bucketId)) {
				if (Files.exists(folder.getParent()) && Files.isDirectory(folder.getParent())
						&& Files.isWritable(folder.getParent())) {
					Files.createDirectory(folder);
				} else {
					if (parents) {
						Files.createDirectories(folder);
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
		// object.setId(idStr);
		// object.setFileName(fileName);

		return folder;
	}

	public String createObject(String bucket, String accountId, ObjectItem object, InputStream fileData,
			String objectname, boolean parents) throws IOManagementException, IOException {
		String idStr = objectname.replace(":", "/");
		String fileName = getObjectName(idStr);
		
		
		// CREATING A RANDOM TEMP FOLDER
		String rndStr = StringUtils.randomString(20);
		Path randomFolder = Paths.get(tmp, rndStr);
		Path tmpFile = Paths.get(tmp, rndStr, fileName);

		String userFileStr = getBucketPath(accountId, bucket) + "/" + idStr;
		Path userFile = Paths.get(userFileStr);

		// if parents is
		// true, folders
		// will be
		// autocreated
		logger.info("IOManager: " + tmpFile.toAbsolutePath());
		logger.info("IOManager: " + userFile.toAbsolutePath());
		if (!parents && !Files.exists(userFile.getParent())) {
			throw new IOManagementException("no such folder");
		}

		if (Files.exists(userFile)) {
			userFileStr = renameExistingFile(userFileStr);
			userFile = Paths.get(userFileStr);
			idStr = userFileStr.replace(getBucketPath(accountId, bucket) + "/", "");
			fileName = getObjectName(idStr);
		}

		object.setId(idStr);
		object.setFileName(fileName);

		logger.info(tmpFile.toAbsolutePath().toString());

		try {
			Files.createDirectory(randomFolder);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOManagementException("Could not create the upload temp directory");
		}
		// COPYING TO DISK

		try {
			Files.copy(fileData, tmpFile);
			// Java 7 method
			// Files.copy(fileData, Paths.get(tmpFile.getAbsolutePath()));
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOManagementException("Could not write the file on disk");
		}
		// COPYING FROM TEMP TO ACCOUNT DIR
		try {
			logger.info(userFile.toAbsolutePath().toString());
			Files.copy(tmpFile, userFile);
			object.setDiskUsage(Files.size(userFile));
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOManagementException("Copying from tmp folder to account folder");
		}
		IOManagerUtils.deleteDirectory(randomFolder);
		return idStr;
	}

	public String deleteObject(String bucket, String accountId, String objectname) throws IOManagementException {
		String idStr = objectname.replace(":", "/");
		String userFileStr = getBucketPath(accountId, bucket) + "/" + idStr;
		logger.info("IOManager: " + userFileStr);
		logger.info(userFileStr);
		File dir = new File(userFileStr);
		if (dir.delete()) {
			return idStr;
		} else {
			throw new IOManagementException("could not delete the data");
		}
		// FileUtils.deleteDirectory(new File(userFileStr));
	}

	public String getJobResultFromBucket(String bucket, String accountId, String sessionId, String jobId)
			throws IOException, DocumentException {
		String resultFileStr = getJobPath(accountId, bucket, jobId).toString();
		File resultFile = new File(resultFileStr + "/result.xml");
		logger.debug("checking file: " + resultFile);
		FileUtils.checkFile(resultFile);
		logger.debug("file " + resultFile + " exists");

		Result resultXml = new Result();
		resultXml.loadXmlFile(resultFile.getAbsolutePath());
		Gson g = new Gson();
		String resultJson = g.toJson(resultXml);
		return resultJson;
	}

	public String getFileTableFromJob(String bucket, String accountId, String sessionId, String jobId, String filename,
			int first, int end, String[] colnamesArray, String[] colvisibilityArray, String callback, String sort)
			throws IOManagementException, IOException {

		String jobFileStr = getJobPath(accountId, bucket, jobId).toString();
		File jobFile = new File(jobFileStr + "/" + filename);
		try {
			FileUtils.checkFile(jobFile);
		} catch (IOException e) {
			throw new IOManagementException("File not found: " + jobFile.getAbsolutePath());
		}
		String name = filename.replace("..", "").replace("/", "");

		List<String> avoidingFiles = getAvoidingFiles();
		if (avoidingFiles.contains(name)) {
			throw new IOManagementException("No permission to use that file: " + jobFile.getAbsolutePath());
		}

		StringBuilder stringBuilder = new StringBuilder();

		int totalCount = -1;
		List<String> headLines;
		try {
			headLines = IOUtils.head(jobFile, 30);
		} catch (IOException e) {
			throw new IOManagementException("could not head file: " + jobFile.getAbsolutePath());
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

			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(jobFile)));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (!line.startsWith("#")) {
					numFeatures++;
				}
			}
			br.close();
			totalCount = numFeatures;

			String text = "#NUMBER_FEATURES	" + numFeatures;
			IOUtils.prepend(jobFile, text);
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

			List<String> dataFile = IOUtils.grep(jobFile, "^[^#].*");
			double[] numbers = ListUtils.toDoubleArray(IOUtils.column(jobFile, numColumn, "\t", "^[^#].*"));
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
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(jobFile)));
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
			br.close();
		}
		return stringBuilder.toString();
	}

	public DataInputStream getFileFromJob(String bucket, String accountId, String sessionId, String jobId,
			String filename, String zip) throws IOManagementException, FileNotFoundException {

		String fileStr = getJobPath(accountId, bucket, jobId).toString();
		File file = new File(fileStr + "/" + filename);
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

	private Path getAccountPath(String accountId) {
		return Paths.get(accountHomePath, accountId);
	}

	private Path getBucketPath(String accountId, String bucketId) {
		if (bucketId != null) {
			return getAccountPath(accountId).resolve(Paths.get("buckets", bucketId.toLowerCase()));
		}
		return getAccountPath(accountId).resolve("buckets");
	}
	public Path getObjectPath(String accountId, String bucketId, String dataId) {
		dataId = dataId.replaceAll(":", "/");
		return getBucketPath(accountId, bucketId).resolve(dataId);
	}

	// public String getDataPath(String wsDataId){
	// wsDataId.replaceAll(":", "/")
	// }

	// public String getJobPath(String accountId, String bucketId, String jobId)
	// {
	// return getAccountPath(accountId) + "/jobs/" + jobId;
	// }

	private String renameExistingFile(String name) {
		File file = new File(name);
		if (file.exists()) {
			String fileName = FileUtils.removeExtension(name);
			String fileExt = FileUtils.getExtension(name);
			String newname = null;
			if (fileName != null && fileExt != null) {
				newname = fileName + "-copy" + fileExt;
			} else {
				newname = name + "-copy";
			}
			return renameExistingFile(newname);
		} else {
			return name;
		}
	}

	private String getObjectName(String objectname) {
		String[] tokens = objectname.split("/");
		return tokens[(tokens.length - 1)];
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
