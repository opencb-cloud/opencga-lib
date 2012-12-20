package org.bioinfo.gcsa.lib.account.io;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

public class IOManager {

	private Logger logger;
	private Properties properties;

	private String home;
	private String accounts;
	private String tmp;

	public IOManager(Properties properties) {
		logger = new Logger();
		logger.setLevel(Logger.INFO_LEVEL);
		this.properties = properties;
		home = System.getenv(properties.getProperty("GCSA.ENV.HOME"));
		accounts = home + properties.getProperty("GCSA.ACCOUNT.PATH");
		tmp = properties.getProperty("TMP.PATH");
	}

	public void createScaffoldAccountId(String accountId) throws IOManagementException {

		if (!new File(accounts).exists()) {
			try {
				FileUtils.createDirectory(accounts);
			} catch (IOException e1) {
				FileUtils.deleteDirectory(new File(accounts));
				throw new IOManagementException("IOException" + e1.toString());
			}
		}

		System.out.println("---------------->>>>> PATHGCSA: " + accounts);
		if (new File(accounts).exists() && new File(accounts).canWrite() && new File(accounts).canRead()
				&& new File(accounts).canExecute()) {
			try {
				FileUtils.createDirectory(accounts + "/" + accountId);
				logger.info("account scaffold created");
			} catch (IOException e1) {
				FileUtils.deleteDirectory(new File(accounts + "/" + accountId));
				FileUtils.deleteDirectory(new File(accounts));
				throw new IOManagementException("IOException" + e1.toString());
			}

			try {
				FileUtils.createDirectory(accounts + "/" + accountId + "/analysis");
			} catch (IOException e1) {
				FileUtils.deleteDirectory(new File(accounts + "/" + accountId + "/analysis"));
				FileUtils.deleteDirectory(new File(accounts + "/" + accountId));
				FileUtils.deleteDirectory(new File(accounts));
				throw new IOManagementException("IOException" + e1.toString());
			}

			try {
				FileUtils.createDirectory(accounts + "/" + accountId + "/buckets");
			} catch (IOException e1) {
				FileUtils.deleteDirectory(new File(accounts + "/" + accountId + "/buckets"));
				FileUtils.deleteDirectory(new File(accounts + "/" + accountId + "/analysis"));
				FileUtils.deleteDirectory(new File(accounts + "/" + accountId));
				FileUtils.deleteDirectory(new File(accounts));
				throw new IOManagementException("IOException" + e1.toString());
			}

			try {
				FileUtils.createDirectory(accounts + "/" + accountId + "/buckets/default");
			} catch (IOException e1) {
				throw new IOManagementException("IOException" + e1.toString());
			}

			try {
				FileUtils.createDirectory(getAccountPath(accountId) + "/jobs");
			} catch (IOException e) {
				throw new IOManagementException("IOException" + e.toString());
			}

		} else {
			throw new IOManagementException("ERROR: The bucket has not been created ");
		}

	}

	public void createBucketFolder(String accountId, String bucket) throws IOManagementException {
		String path = getBucketPath(accountId, bucket);
		try {
			Boolean created = FileUtils.createDirectory(path);
			if (!created) {
				throw new IOManagementException("could not create the directory, already exists");
			}
		} catch (IOException e1) {
			FileUtils.deleteDirectory(new File(path));
			throw new IOManagementException("could not create the directory: " + e1.toString());
		}
	}

	public void deleteBucketFolder(String accountId, String bucket) throws IOManagementException {
		String path = getBucketPath(accountId, bucket);
		Boolean deleted = FileUtils.deleteDirectory(new File(path));
		if (!deleted) {
			throw new IOManagementException("could not delete the bucket directory.");
		}
	}

	public void createJobFolder(String accountId, String bucket, String jobId) throws IOManagementException {
		String path = getAccountPath(accountId) + "/jobs";

		if (new File(path).exists() && new File(path).canWrite()) {
			try {
				FileUtils.createDirectory(path + "/" + jobId);
			} catch (IOException e) {
				throw new IOManagementException("IOException" + e.toString());
			}
		}
	}

	public void removeJobFolder(String accountId, String bucket, String jobId) throws IOManagementException {
		File path = new File(accounts + "/" + accountId + "/buckets/" + bucket + "/jobs/" + jobId);
		FileUtils.deleteDirectory(path);
	}

	public String createFolder(String bucket, String accountId, ObjectItem object, String objectname, boolean parents)
			throws IOManagementException {
		String idStr = objectname.replace(":", "/");
		String fileName = getObjectName(idStr);

		String userFileStr = getBucketPath(accountId, bucket) + "/" + idStr;
		File userFile = new File(userFileStr);

		logger.info("IOManager: " + userFile.getAbsolutePath());
		if (!parents && !userFile.getParentFile().exists()) {
			throw new IOManagementException("no such folder");
		}

		if (userFile.exists()) {
			throw new IOManagementException("folder already exists");
		}

		try {
			FileUtils.createDirectory(userFileStr);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOManagementException("could not create the directory "+e.toString());
		}
		object.setId(idStr);
		object.setFileName(fileName);
		
		return idStr;
	}

	public String createData(String bucket, String accountId, ObjectItem object, InputStream fileData,
			String objectname, boolean parents) throws IOManagementException {
		String idStr = objectname.replace(":", "/");
		String fileName = getObjectName(idStr);

		// CREATING A RANDOM TEMP FOLDER
		File rndFolder = null;
		String randomFolder = tmp + "/" + StringUtils.randomString(20);
		File tmpFile = new File(randomFolder + "/" + fileName);

		String userFileStr = getBucketPath(accountId, bucket) + "/" + idStr;
		File userFile = new File(userFileStr);

		// if parents is
		// true, folders
		// will be
		// autocreated
		logger.info("IOManager: " + tmpFile.getAbsolutePath());
		logger.info("IOManager: " + userFile.getAbsolutePath());
		if (!parents && !userFile.getParentFile().exists()) {
			throw new IOManagementException("no such folder");
		}

		if (userFile.exists()) {
			userFileStr = renameExistingFile(userFileStr);
			userFile = new File(userFileStr);
			idStr = userFileStr.replace(getBucketPath(accountId, bucket) + "/", "");
			fileName = getObjectName(idStr);
		}

		object.setId(idStr);
		object.setFileName(fileName);

		logger.info(tmpFile.getAbsolutePath());

		try {
			FileUtils.createDirectory(randomFolder);
			rndFolder = new File(randomFolder);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOManagementException("Could not create the upload temp directory");
		}
		// COPYING TO DISK

		try {
			IOUtils.write(tmpFile, fileData);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOManagementException("Could not write the file on disk");
		}
		// COPYING FROM TEMP TO ACCOUNT DIR
		try {
			logger.info(userFile.getAbsolutePath());
			FileUtils.touch(userFile);
			FileUtils.copy(tmpFile, userFile);
			object.setDiskUsage(Long.toString(userFile.length()));

		} catch (IOException e) {
			e.printStackTrace();
			throw new IOManagementException("Copying from tmp folder to account folder");
		}
		FileUtils.deleteDirectory(rndFolder);
		return idStr;
	}

	public String deleteData(String bucket, String accountId, String objectname) {
		String idStr = objectname.replace(":", "/");
		String userFileStr = getBucketPath(accountId, bucket) + "/" + idStr;
		logger.info("IOManager: " + userFileStr);
		logger.info(userFileStr);
		FileUtils.deleteDirectory(new File(userFileStr));
		return idStr;
	}

	public String getJobResultFromBucket(String bucket, String accountId, String sessionId, String jobId)
			throws IOException, DocumentException {
		String resultFileStr = getJobPath(accountId, bucket, jobId);
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

		String jobFileStr = getJobPath(accountId, bucket, jobId);
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

		String fileStr = getJobPath(accountId, bucket, jobId);
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

	private String getAccountPath(String accountId) {
		return accounts + "/" + accountId;
	}

	private String getBucketPath(String accountId, String bucketId) {
		return getAccountPath(accountId) + "/buckets/" + bucketId.toLowerCase();
	}

	// public String getDataPath(String wsDataId){
	// wsDataId.replaceAll(":", "/")
	// }
	public String getDataPath(String accountId, String bucketId, String dataId) {
		dataId = dataId.replaceAll(":", "/");
		return getBucketPath(accountId, bucketId) + "/" + dataId;
	}

	public String getJobPath(String accountId, String bucketId, String jobId) {
		return getAccountPath(accountId) + "/jobs/" + jobId;
	}

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
}
