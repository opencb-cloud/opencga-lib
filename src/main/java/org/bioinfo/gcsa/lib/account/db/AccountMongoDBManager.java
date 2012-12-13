package org.bioinfo.gcsa.lib.account.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.commons.io.utils.IOUtils;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.gcsa.lib.GcsaUtils;
import org.bioinfo.gcsa.lib.account.beans.Account;
import org.bioinfo.gcsa.lib.account.beans.Acl;
import org.bioinfo.gcsa.lib.account.beans.Data;
import org.bioinfo.gcsa.lib.account.beans.Job;
import org.bioinfo.gcsa.lib.account.beans.Plugin;
import org.bioinfo.gcsa.lib.account.beans.Project;
import org.bioinfo.gcsa.lib.account.beans.Session;
import org.bioinfo.gcsa.lib.account.io.IOManagementException;
import org.bioinfo.gcsa.lib.account.io.IOManager;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

public class AccountMongoDBManager implements AccountManager {

	private MongoClient mongoClient;
	private DB mongoDB;
	private DBCollection userCollection;

	private Logger logger;
	private Properties properties;
	private IOManager ioManager;

	private String home;
	private String accounts;
	private String tmp;
	private Gson gson;

	public AccountMongoDBManager(Properties properties) throws NumberFormatException, UnknownHostException {
		gson = new Gson();
		logger = new Logger();
		logger.setLevel(Logger.INFO_LEVEL);
		this.properties = properties;

		ioManager = new IOManager(properties);
		home = System.getenv(properties.getProperty("GCSA.ENV.HOME"));
		accounts = home + properties.getProperty("GCSA.ACCOUNT.PATH");
		tmp = properties.getProperty("TMP.PATH");

		connect();
	}

	private void connect() throws NumberFormatException, UnknownHostException {
		logger.info("mongodb connect");
		String host = properties.getProperty("GCSA.MONGO.HOST", "localhost");
		int port = Integer.parseInt(properties.getProperty("GCSA.MONGO.PORT"));
		String db = properties.getProperty("GCSA.MONGO.DB");
		String collection = properties.getProperty("GCSA.MONGO.COLLECTION");

		mongoClient = new MongoClient(host, port);
		mongoDB = mongoClient.getDB(db);
		userCollection = mongoDB.getCollection(collection);
	}

	public void disconnect() {
		logger.info("mongodb disconnect");
		userCollection = null;
		if (mongoDB != null) {
			mongoDB.cleanCursors(true);
		}
		if (mongoClient != null) {
			mongoClient.close();
		}
	}

	// ///////////////////////////////////
	/*
	 * Account methods
	 */
	// //////////////////////////////////

	private void checkAccountExists(String accountId) throws AccountManagementException {
		BasicDBObject query = new BasicDBObject();
		query.put("accountId", accountId);
		DBObject obj = userCollection.findOne(query);
		if (obj != null) {
			throw new AccountManagementException("the account already exists");
		}
	}

	private String getAccountPath(String accountId) {
		return accounts + "/" + accountId;
	}

	private String getProjectPath(String accountId, String projectId) {
		return getAccountPath(accountId) + "/" + projectId;
	}

	@Override
	public String getDataPath(String projectId, String dataId, String sessionId) {
		String accountId = getAccountIdBySessionId(sessionId);
		dataId = dataId.replaceAll(":", "/");
		return getProjectPath(accountId, projectId) + "/" + dataId;

		// // projects:default:jobs:ae8Bhh8Y:test.txt
		// // projects:default:virtualdir:test.txt
		// String path = null;
		// if (dataId.contains(":jobs:")) {
		// path = "/" + dataId.replaceAll(":", "/");
		// } else {
		// String[] fields = dataId.split(":", 3);
		// if (fields.length > 3) {
		// return "ERROR: unexpected format on '" + dataId + "'";
		// } else {
		// path = "/" + fields[0] + "/" + fields[1] + "/" + fields[2];
		// }
		// }
		//
		// String dataPath = accounts + "/" + getAccountIdBySessionId(sessionId)
		// + path;
		//
		// if (new File(dataPath).exists()) {
		// return dataPath;
		// } else {
		// return "ERROR: data '" + dataId + "' not found";
		// }
	}

	private String accountConfPath(String accountId) {
		return accounts + "/" + accountId + "/" + "account.conf";
	}

	public void createAccount(String accountId, String password, String accountName, String email, Session session)
			throws AccountManagementException {

		checkAccountExists(accountId);

		Account account = null;

		File accountDir = new File(getAccountPath(accountId));
		File accountConf = new File(accountConfPath(accountId));
		if (accountDir.exists() && accountConf.exists()) {
			// covert user mode file to mode mongo
			// EL USUARIO NO EXISTE PERO TIENE CARPETA Y FICHERO DE
			// CONFIGURACION
			try {
				BufferedReader br = new BufferedReader(new FileReader(accountConf));
				account = gson.fromJson(br, Account.class);
				account.addSession(session);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}

		try {
			ioManager.createScaffoldAccountId(accountId);
		} catch (IOManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (account == null) {
			account = new Account(accountId, accountName, password, email);
			account.setLastActivity(GcsaUtils.getTime());
		}
		WriteResult wr = userCollection.insert((DBObject) JSON.parse(gson.toJson(account)));
		if (wr.getLastError().getErrorMessage() != null) {
			throw new AccountManagementException(wr.getLastError().getErrorMessage());
		}
	}

	public String login(String accountId, String password, Session session) throws AccountManagementException {
		BasicDBObject query = new BasicDBObject();
		query.put("accountId", accountId);
		query.put("password", password);

		DBObject obj = userCollection.findOne(query);

		if (obj != null) {
			Account account = gson.fromJson(obj.toString(), Account.class);
			account.addSession(session);
			List<Session> accountSessionList = account.getSessions();
			List<Session> accountOldSessionList = account.getOldSessions();
			List<Session> oldSessionsFound = new ArrayList<Session>();
			Date now = GcsaUtils.toDate(GcsaUtils.getTime());

			// get oldSessions
			for (Session s : accountSessionList) {
				Date loginDate = GcsaUtils.toDate(s.getLogin());
				Date fechaCaducidad = GcsaUtils.add24HtoDate(loginDate);
				if (fechaCaducidad.compareTo(now) < 0) {
					oldSessionsFound.add(s);
				}
			}
			// update arrays
			for (Session s : oldSessionsFound) {
				accountSessionList.remove(s);
				accountOldSessionList.add(s);
			}

			BasicDBObject fields = new BasicDBObject("sessions", JSON.parse(gson.toJson(accountSessionList)));
			fields.put("oldSessions", JSON.parse(gson.toJson(accountOldSessionList)));
			fields.put("lastActivity", GcsaUtils.getTime());
			BasicDBObject action = new BasicDBObject("$set", fields);
			WriteResult wr = userCollection.update(query, action);

			if (wr.getLastError().getErrorMessage() == null) {
				if (wr.getN() != 1) {
					throw new AccountManagementException("could not update sessions");
				}
			} else {
				throw new AccountManagementException(wr.getLastError().getErrorMessage());
			}

			return session.getId();

		} else {
			throw new AccountManagementException("account not found");
		}
	}

	@Override
	public Session getSession(String accountId, String sessionId) {
		// db.users.find({"accountId":"imedina","sessions.id":"8l665MB3Q7MdKzfGJBJd"},
		// { "sessions.$":1 ,"_id":0})
		// ESTO DEVOLVERA SOLO UN OBJETO SESION, EL QUE CORRESPONDA CON LA ID
		// DEL FIND

		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);
		BasicDBObject fields = new BasicDBObject();
		fields.put("_id", 0);
		fields.put("sessions.$", 1);

		DBObject obj = userCollection.findOne(query, fields);

		Session session = null;
		if (obj != null) {
			Session[] sessions = gson.fromJson(obj.get("sessions").toString(), Session[].class);
			session = sessions[0];
		}
		return session;
	}

	public void logout(String accountId, String sessionId) throws AccountManagementException {
		Session session = getSession(accountId, sessionId);
		if (session != null) {
			// INSERT DATA OBJECT IN MONGO
			session.setLogout(GcsaUtils.getTime());
			BasicDBObject dataDBObject = (BasicDBObject) JSON.parse(gson.toJson(session));
			BasicDBObject query = new BasicDBObject();
			query.put("accountId", accountId);
			query.put("sessions.id", sessionId);

			updateMongo("push", query, "oldSessions", dataDBObject);
			query.removeField("sessions.id");
			BasicDBObject value = new BasicDBObject("id", sessionId);
			updateMongo("pull", query, "sessions", value);
			updateMongo("set", new BasicDBObject("accountId", accountId), "lastActivity", GcsaUtils.getTime());
		} else {
			throw new AccountManagementException("logout");
		}
	}

	public void changePassword(String accountId, String sessionId, String password, String nPassword1, String nPassword2)
			throws AccountManagementException {

		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);
		query.put("password", password);
		BasicDBObject fields = new BasicDBObject("password", nPassword1);
		fields.put("lastActivity", GcsaUtils.getTime());
		BasicDBObject action = new BasicDBObject("$set", fields);
		WriteResult wr = userCollection.update(query, action);

		if (wr.getLastError().getErrorMessage() == null) {
			if (wr.getN() != 1) {
				throw new AccountManagementException("could not change password with this parameters");
			}
			logger.info("password changed");
		} else {
			throw new AccountManagementException("could not change password :" + wr.getError());
		}

	}

	public void changeEmail(String accountId, String sessionId, String nEmail) throws AccountManagementException {
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);
		BasicDBObject fields = new BasicDBObject("email", nEmail);
		fields.put("lastActivity", GcsaUtils.getTime());
		BasicDBObject action = new BasicDBObject("$set", fields);
		WriteResult wr = userCollection.update(query, action);

		if (wr.getLastError().getErrorMessage() == null) {
			if (wr.getN() != 1) {
				throw new AccountManagementException("could not change email with this parameters");
			}
			logger.info("email changed");
		} else {
			throw new AccountManagementException("could not change email :" + wr.getError());
		}
	}

	@Override
	public void resetPassword(String accountId, String email) throws AccountManagementException {
		String newPassword = StringUtils.randomString(6);
		String sha1Password = null;
		try {
			sha1Password = StringUtils.sha1(newPassword);
		} catch (NoSuchAlgorithmException e) {
			throw new AccountManagementException("could not encode password");
		}

		BasicDBObject query = new BasicDBObject();
		query.put("accountId", accountId);
		query.put("email", email);
		BasicDBObject item = new BasicDBObject("password", sha1Password);
		BasicDBObject action = new BasicDBObject("$set", item);
		WriteResult wr = userCollection.update(query, action);

		if (wr.getLastError().getErrorMessage() == null) {
			if (wr.getN() != 1) {
				throw new AccountManagementException("could not reset password with this parameters");
			}
			logger.info("password reset");
		} else {
			throw new AccountManagementException("could not reset the password");
		}

		StringBuilder message = new StringBuilder();
		message.append("Hello,").append("\n");
		message.append("You can now login using this new password:").append("\n\n");
		message.append(newPassword).append("\n\n\n");
		message.append("Please change it when you first login.").append("\n\n");
		message.append("Best regards,").append("\n\n");
		message.append("Computational Biology Unit at Computational Medicine Institute").append("\n");

		GcsaUtils.sendResetPasswordMail(email, message.toString());
	}

	@Override
	public String getAccountBySessionId(String accountId, String sessionId, String lastActivity)
			throws AccountManagementException {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject fields = new BasicDBObject();
		query.put("accountId", accountId);
		query.put("sessions.id", sessionId);
		fields.put("_id", 0);
		fields.put("password", 0);
		fields.put("sessions", 0);
		fields.put("oldSessions", 0);

		DBObject item = userCollection.findOne(query, fields);
		if (item != null) {
			// if has not been modified since last time was call
			if (lastActivity != null && item.get("lastActivity").toString().equals(lastActivity)) {
				return "{}";
			}
			return item.toString();
		} else {
			throw new AccountManagementException("could not get account info with this parameters");
		}
	}

	public String createProject(Project project, String accountId, String sessionId) throws AccountManagementException {
		BasicDBObject filter = new BasicDBObject("accountId", accountId);
		try {
			ioManager.createProjectFolder(accountId, project.getName());
		} catch (IOManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		updateMongo("push", filter, "projects", project);
		updateMongo("set", new BasicDBObject("accountId", accountId), "lastActivity", GcsaUtils.getTime());
		return "";
	}

	public String getAllProjectsBySessionId(String accountId, String sessionId) throws AccountManagementException {
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);

		DBObject item = userCollection.findOne(query);
		if (item != null) {
			// user = new Gson().fromJson(item.toString(), Account.class);
			// projectsStr = JSON.parse(new
			// Gson().toJson(user.getProjects())).toString();
			// account = gson.fromJson(item.toString(), Account.class);
			String projectsStr = item.get("projects").toString();
			// getJSON.parse(gson.toJson(account.getProjects())).toString();
			updateMongo("set", new BasicDBObject("accountId", accountId), "lastActivity", GcsaUtils.getTime());
			return projectsStr;
		} else {
			throw new AccountManagementException("invalid sessionId");
		}
	}

	public void createAnonymousUser(String accountId, String password, String email) {

	}

	// public void createData(String dataName, String project, String sessionId)
	// {
	//
	// String dataId = StringUtils.randomString(8);
	//
	// // /
	// dataName = StringUtils.randomString(10);
	// // /
	// String accountId = getAccountIdBySessionId(sessionId);
	//
	// Data data = new Data(dataId, "", dataName, "", "", "", "",
	// GcsaUtils.getTime(), "", "1", "",
	// new ArrayList<Acl>());
	// BasicDBObject dataDBObject = (BasicDBObject) JSON.parse(new
	// Gson().toJson(data));
	// BasicDBObject query = new BasicDBObject();
	// BasicDBObject item = new BasicDBObject();
	// BasicDBObject action = new BasicDBObject();
	// query.put("accountId", accountId);
	// query.put("projects.id", project);
	// item.put("projects.$.data", dataDBObject);
	// action.put("$push", item);
	// userCollection.update(query, action);
	//
	// // return dataId;
	// }

	// public String getUserByAccountId(String accountId, String sessionId) {
	// String userStr = "";
	//
	// BasicDBObject query = new BasicDBObject("accountId", accountId);
	// query.put("sessions.id", sessionId);
	//
	// DBCursor iterator = userCollection.find(query);
	//
	// if (iterator.count() == 1) {
	// userStr = iterator.next().toString();
	// updateMongo("set", new BasicDBObject("accountId", accountId),
	// "lastActivity", GcsaUtils.getTime());
	// }
	//
	// return userStr;
	// }

	public String getUserByEmail(String email, String sessionId) {
		String userStr = "";

		BasicDBObject query = new BasicDBObject("email", email);
		query.put("sessions.id", sessionId);

		DBCursor iterator = userCollection.find(query);

		if (iterator.count() == 1) {
			userStr = iterator.next().toString();
			updateMongo("set", query, "lastActivity", GcsaUtils.getTime());
		}

		return userStr;
	}

	// ////////////////////////////////////
	/*
	 * Project methods
	 */
	// ////////////////////////////////////

	@Override
	public boolean checkSessionId(String accountId, String sessionId) {
		boolean isValidSession = false;

		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);
		DBCursor iterator = userCollection.find(query);

		if (iterator.count() > 0) {
			isValidSession = true;
		}

		return isValidSession;
	}

	public String getAccountIdBySessionId(String sessionId) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject fields = new BasicDBObject();
		query.put("sessions.id", sessionId);
		fields.put("_id", 0);
		fields.put("accountId", 1);
		DBObject item = userCollection.findOne(query, fields);

		if (item != null) {
			return item.get("accountId").toString();
		} else {
			return "ERROR: Invalid sessionId";
		}
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

	private String getDataName(String objectname) {
		String[] tokens = objectname.split("/");
		return tokens[(tokens.length - 1)];
	}


	@Override
	public void createDataToProject(String project, String accountId, String sessionId, Data data)
			throws AccountManagementException {

		// INSERT DATA OBJECT ON MONGO
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);
		query.put("projects.id", project.toLowerCase());
		BasicDBObject dataDBObject = (BasicDBObject) JSON.parse(gson.toJson(data));
		BasicDBObject item = new BasicDBObject("projects.$.data", dataDBObject);
		BasicDBObject action = new BasicDBObject("$push", item);
		action.put("$set", new BasicDBObject("lastActivity", GcsaUtils.getTime()));
		WriteResult wr = userCollection.update(query, action);

		// db.users.update({"accountId":"fsalavert","projects.name":"Default"},{$push:{"projects.$.data":{"a":"a"}}})

		if (wr.getLastError().getErrorMessage() == null) {
			if (wr.getN() != 1) {
				throw new AccountManagementException("could not update database, with this parameters");
			}
			logger.info("data object created");
		} else {
			throw new AccountManagementException("could not update database, files will be deleted");
		}

		// BasicDBObject actitvityQuery = new BasicDBObject("accountId",
		// accountId);
		// query.put("sessions.id", sessionId);
		// updateMongo("set", actitvityQuery,"lastActivity",
		// GcsaUtils.getTime());

	}
	@Override
	public void deleteDataFromProject(String project, String accountId, String sessionId, String dataId)
			throws AccountManagementException {
//		db.users.update({"accountId":"pako","projects.id":"default"},{$pull:{"projects.$.data":{"id":"hola/como/estas/app.js"}}})
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);
		query.put("projects.id", project.toLowerCase());
		
		BasicDBObject projectData =  new BasicDBObject("projects.$.data", new BasicDBObject("id", dataId));
		BasicDBObject action =  new BasicDBObject("$pull", projectData);
		action.put("$set", new BasicDBObject("lastActivity", GcsaUtils.getTime()));
		
		WriteResult wr = userCollection.update(query, action);
		if (wr.getLastError().getErrorMessage() == null) {
			if (wr.getN() != 1) {
				throw new AccountManagementException("deleting data, with this parameters");
			}
			logger.info("data object deleted");
		} else {
			throw new AccountManagementException("could not delete data item from database");
		}
	}

	// ///////////////////////
	/*
	 * Job methods
	 */
	// //////////////////////

	@Override
	public String createJob(String jobName, String jobFolder, String project, String toolName, List<String> dataList,
			String commandLine, String sessionId) {
		String jobId = StringUtils.randomString(8);
		String accountId = getAccountIdBySessionId(sessionId);

		if (jobFolder == null) {
			// CREATE JOB FOLDER
			try {
				ioManager.createJobFolder(accountId, project, jobId);
			} catch (IOManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// INSERT JOB OBJECT ON MONGO
		Job job = new Job(jobId, "0", "", "", "", toolName, jobName, "0", commandLine, "", "", "", dataList);
		BasicDBObject jobDBObject = (BasicDBObject) JSON.parse(new Gson().toJson(job));
		BasicDBObject query = new BasicDBObject();
		BasicDBObject item = new BasicDBObject();
		BasicDBObject action = new BasicDBObject();
		query.put("accountId", accountId);
		query.put("projects.id", project);
		item.put("projects.$.jobs", jobDBObject);
		action.put("$push", item);
		WriteResult result = userCollection.update(query, action);

		if (result.getError() != null) {
			try {
				ioManager.removeJobFolder(accountId, project, jobId);
			} catch (IOManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "MongoDB error, " + result.getError() + " files will be deleted";
		}
		return jobId;
	}

	// //////////////////////
	/*
	 * Utils
	 */
	// /////////////////////

	public List<Project> jsonToProjectList(String json) {

		Project[] projects = new Gson().fromJson(json, Project[].class);
		return Arrays.asList(projects);
	}

	// ////////////////////
	/*
	 * Private classes
	 */
	// ////////////////////

	private void updateMongo(String operator, DBObject filter, String field, Object value) {
		BasicDBObject set = null;
		if (String.class.isInstance(value)) {
			set = new BasicDBObject("$" + operator, new BasicDBObject().append(field, value));
		} else {
			set = new BasicDBObject("$" + operator, new BasicDBObject().append(field,
					(DBObject) JSON.parse(new Gson().toJson(value))));
		}
		userCollection.update(filter, set);
	}

	@SuppressWarnings("unused")
	private void updateMongo(BasicDBObject[] filter, String field, Object value) {

		BasicDBObject container = filter[0];
		for (int i = 1; i < filter.length; i++) {
			container.putAll(filter[i].toMap());
		}

		BasicDBObject set = new BasicDBObject("$set", new BasicDBObject().append(field,
				JSON.parse(new Gson().toJson(value))));

		userCollection.update(container, set);

	}

	@Override
	public String getJobFolder(String project, String jobId, String sessionId) {
		// String accountId = getAccountIdBySessionId(sessionId);
		// BasicDBObject query = new BasicDBObject();
		// BasicDBObject fields = new BasicDBObject();
		// query.put("accountId", accountId);
		// query.put("projects.status", "1");
		// fields.put("_id", 0);
		// fields.put("projects.$", 1);
		// DBObject item = userCollection.findOne(query,fields);
		// Project[] p = new Gson().fromJson(item.get("projects").toString(),
		// Project[].class);

		String jobFolder = accounts + "/" + getAccountIdBySessionId(sessionId) + "/projects/" + project + "/jobs/"
				+ jobId + "/";
		if (new File(jobFolder).exists()) {
			return jobFolder;
		} else {
			return "ERROR: Invalid jobId";
		}
	}

	// private BasicDBObject createBasicDBQuery(String accountId, String
	// sessionId) {
	// BasicDBObject query = new BasicDBObject();
	// query.put("accountId", accountId);
	// query.put("sessions.id", sessionId);
	// return query;
	// }
	//
	// private BasicDBObject createBasicDBQuery(String ... params) { // String[]
	// params
	// BasicDBObject query = new BasicDBObject();
	// for(int i = 0; i<params.length; i += 2) {
	// query.put(params[i], params[i+1]);
	// }
	// return query;
	// }
	//
	// private BasicDBObject createBasicDBFieds() { // String[] params
	// BasicDBObject fields = new BasicDBObject();
	// fields.put("_id", 0);
	// fields.put("password", 0);
	// fields.put("sessions", 0);
	// fields.put("oldSessions", 0);
	// fields.put("data", 0);
	// return fields;
	// }

	@Override
	public List<Plugin> getUserAnalysis(String sessionId) throws AccountManagementException {
		BasicDBObject query = new BasicDBObject("sessions.id", sessionId);
		BasicDBObject fields = new BasicDBObject();
		fields.put("_id", 0);
		fields.put("plugins", 1);

		DBObject item = userCollection.findOne(query, fields);
		if (item != null) {
			Plugin[] userAnalysis = new Gson().fromJson(item.get("plugins").toString(), Plugin[].class);
			return Arrays.asList(userAnalysis);
		} else {
			throw new AccountManagementException("invalid session id");
		}
	}

}
