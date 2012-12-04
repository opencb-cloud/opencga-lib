package org.bioinfo.gcsa.lib.users.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Pattern;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.commons.io.utils.IOUtils;
import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.gcsa.lib.GcsaUtils;
import org.bioinfo.gcsa.lib.users.CloudSessionManager;
import org.bioinfo.gcsa.lib.users.IOManager;
import org.bioinfo.gcsa.lib.users.beans.Data;
import org.bioinfo.gcsa.lib.users.beans.Job;
import org.bioinfo.gcsa.lib.users.beans.Project;
import org.bioinfo.gcsa.lib.users.beans.Session;
import org.bioinfo.gcsa.lib.users.beans.User;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

public class UserMongoDBManager implements UserManager {
	private IOManager ioManager = new IOManager();
	private Mongo mongo;
	private DB mongoDB;
	private DBCollection userCollection;
	private String GCSA_MONGO_DB = CloudSessionManager.properties.getProperty("GCSA.MONGO.DB");
	private String GCSA_MONGO_COLLECTION = CloudSessionManager.properties.getProperty("GCSA.MONGO.COLLECTION");
	private String GCSA_ENV = System.getenv(CloudSessionManager.properties.getProperty("GCSA.ENV.PATH"));
	private String GCSA_ACCOUNT = GCSA_ENV + CloudSessionManager.properties.getProperty("GCSA.ACCOUNT.PATH");
	private String TMP = CloudSessionManager.properties.getProperty("TMP.PATH");

	public UserMongoDBManager() throws UserManagementException {
		connectToMongo();
		getDataBase(GCSA_MONGO_DB);
		getCollection(GCSA_MONGO_COLLECTION);
	}

	// ///////////////////////////////////
	/*
	 * User methods
	 */
	// //////////////////////////////////

	public void createUser(String accountId, String password, String accountName, String email, Session session)
			throws UserManagementException {
		User userLoad = null;

		if (!userExist(accountId)) {// SI NO EXISTE USUARIO

			if (new File(GCSA_ACCOUNT + "/" + accountId).exists()
					&& new File(GCSA_ACCOUNT + "/" + accountId + "/" + "account.conf").exists()) {
				// EL USUARIO NO EXISTE PERO TIENE CARPETA Y FICHERO DE
				// CONFIGURACION
				try {
					BufferedReader br = new BufferedReader(new FileReader(GCSA_ACCOUNT + "/" + accountId + "/"
							+ "user.conf"));
					userLoad = new Gson().fromJson(br, User.class);
					userLoad.addSession(session);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

			}

			ioManager.createScaffoldAccountId(accountId);

			if (validate(email)) {

				if (userLoad == null) {
					userLoad = new User(accountId, accountName, password, email);
				}

				userCollection.insert((DBObject) JSON.parse(new Gson().toJson(userLoad)));
				updateMongo("set", new BasicDBObject("accountId", accountId), "lastActivity", GcsaUtils.getTime());
			} else {
				throw new UserManagementException("ERROR: email not valid");
			}

		} else {// SI EXISTE USUARIO
			throw new UserManagementException("ERROR: The account already exists");
		}

	}

	public void createAnonymousUser(String accountId, String password, String email) {

	}

	public String login(String accountId, String password, Session session) {
		BasicDBObject query = new BasicDBObject();
		String id = "";
		query.put("accountId", accountId);
		query.put("password", password);

		DBCursor iterator = userCollection.find(query);

		if (iterator.count() == 1) {
			User user = new Gson().fromJson(iterator.next().toString(), User.class);
			user.addSession(session);
			id = session.getId();
			List<Session> sess = user.getSessions();

			BasicDBObject filter = new BasicDBObject("accountId", accountId);
			updateMongo("set", filter, "sessions", sess);
			updateMongo("set", new BasicDBObject("accountId", accountId), "lastActivity", GcsaUtils.getTime());
			// mover a oldSessions todas las sesiones con mas de 24 horas.
			Calendar cal;
			List<Session> s = user.getSessions();
			Date fechaActual = GcsaUtils.toDate(GcsaUtils.getTime());
			List<Session> oldSes = user.getOldSessions();
			boolean changed = false;

			for (int i = 0; i < s.size(); i++) {

				Date loginDate = GcsaUtils.toDate(s.get(i).getLogin());
				cal = new GregorianCalendar();
				cal.setTime(loginDate);
				cal.setTimeInMillis(loginDate.getTime());
				// sumamos 24h a la fecha del login
				cal.add(Calendar.DATE, 1);

				Date fechaCaducidad = new Date(cal.getTimeInMillis());

				if (fechaCaducidad.compareTo(fechaActual) < 0) {
					// caducada -> movemos a oldSessions
					s.get(i).setLogout(GcsaUtils.getTime());
					oldSes.add(s.get(i));
					s.remove(i);
					changed = true;
				}
			}

			if (changed) {
				updateMongo("set", new BasicDBObject("accountId", user.getAccountId()), "sessions", s);
				updateMongo("set", new BasicDBObject("accountId", user.getAccountId()), "oldSessions", oldSes);
				updateMongo("set", new BasicDBObject("accountId", accountId), "lastActivity", GcsaUtils.getTime());
			}

		}

		return id;
	}

	public String logout(String accountId, String sessionId) {
		String logoutStatus = "ERROR";
		if (checkSessionId(accountId, sessionId)) {

			// INSERT DATA OBJECT IN MONGO
			Session session = getSessionId(accountId, sessionId);
			session.setLogout(GcsaUtils.getTime());
			BasicDBObject dataDBObject = (BasicDBObject) JSON.parse(new Gson().toJson(session));
			BasicDBObject query = new BasicDBObject();
			query.put("accountId", accountId);
			query.put("sessions.id", sessionId);

			updateMongo("push", query, "oldSessions", dataDBObject);
			query.removeField("sessions.id");
			BasicDBObject value = new BasicDBObject("id", sessionId);
			updateMongo("pull", query, "sessions", value);
			updateMongo("set", new BasicDBObject("accountId", accountId), "lastActivity", GcsaUtils.getTime());

			logoutStatus = "SUCCESS";
		}

		return logoutStatus;
	}

	public String getUserByAccountId(String accountId, String sessionId) {
		String userStr = "";

		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);

		DBCursor iterator = userCollection.find(query);

		if (iterator.count() == 1) {
			userStr = iterator.next().toString();
			updateMongo("set", new BasicDBObject("accountId", accountId), "lastActivity", GcsaUtils.getTime());
		}

		return userStr;
	}

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

	public String changePassword(String accountId, String sessionId, String password, String nPassword1,
			String nPassword2) {
		String msg = "ERROR: Imposible to change";
		if (nPassword1.equals(nPassword2)) {
			BasicDBObject query = new BasicDBObject("accountId", accountId);
			query.put("sessions.id", sessionId);
			query.put("password", password);

			DBCursor iterator = userCollection.find(query);

			if (iterator.count() == 1) {
				updateMongo("set", query, "password", nPassword1);
				updateMongo("set", new BasicDBObject("accountId", accountId), "lastActivity", GcsaUtils.getTime());
				msg = "Password changed";
			}
		} else {
			msg = "ERROR: The new pass is not the same in both fields";
		}
		return msg;
	}

	public String changeEmail(String accountId, String sessionId, String nEmail) {
		String msg = "IMPOSIBLE TO CHANGE";
		if (validate(nEmail)) {
			BasicDBObject query = new BasicDBObject("accountId", accountId);
			query.put("sessions.id", sessionId);

			DBCursor iterator = userCollection.find(query);

			if (iterator.count() == 1) {
				updateMongo("set", query, "email", nEmail);
				updateMongo("set", new BasicDBObject("accountId", accountId), "lastActivity", GcsaUtils.getTime());
				msg = "EMAIL CHANGED";
			}
		} else {
			msg = "INVALID EMAIL";
		}

		return msg;
	}

	@Override
	public String resetPassword(String accountId, String email) {
		if (email != null && !email.trim().equals("") && accountId != null) {

			String newPassword = StringUtils.randomString(6);
			String sha1Password = null;
			try {
				sha1Password = StringUtils.sha1(newPassword);
			} catch (NoSuchAlgorithmException e) {
				return "ERROR generating new password";
			}

			BasicDBObject query = new BasicDBObject();
			query.put("accountId", accountId);
			query.put("email", email);
			BasicDBObject item = new BasicDBObject("password", sha1Password);
			BasicDBObject action = new BasicDBObject("$set", item);
			WriteResult result = userCollection.update(query, action);

			if (result.getN() != 1) {
				return "MongoDB ERROR, " + result.getError() + ", not found";
			}
			if (result.getError() != null) {
				return "MongoDB ERROR, " + result.getError() + ", error updating password";
			}

			StringBuilder message = new StringBuilder();
			message.append("Hello,").append("\n");
			message.append("You can now login using this new password:").append("\n\n");
			message.append(newPassword).append("\n\n\n");
			message.append("Please change it when you first login.").append("\n\n");
			message.append("Best regards,").append("\n\n");
			message.append("Bioinformatic and Genomics Group").append("\n");

			GcsaUtils.sendResetPasswordMail(email, message.toString());
			return "Password reset";
		} else {
			return "ERROR: The email is not valid";
		}
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

	public String getAllProjectsBySessionId(String accountId, String sessionId) {
		String projectsStr = "";
		User user = null;

		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);

		DBCursor iterator = userCollection.find(query);

		if (iterator.count() == 1) {
			user = new Gson().fromJson(iterator.next().toString(), User.class);
			projectsStr = JSON.parse(new Gson().toJson(user.getProjects())).toString();
			updateMongo("set", new BasicDBObject("accountId", accountId), "lastActivity", GcsaUtils.getTime());
		}

		return projectsStr;
	}

	public String createProject(Project project, String accountId, String sessionId) {
		BasicDBObject filter = new BasicDBObject("accountId", accountId);
		try {
			ioManager.createProjectFolder(accountId, project.getName());
		} catch (UserManagementException e) {
			e.printStackTrace();
		}
		updateMongo("push", filter, "projects", project);
		updateMongo("set", new BasicDBObject("accountId", accountId), "lastActivity", GcsaUtils.getTime());
		return "";
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

	@Override
	public Session getSessionId(String accountId, String sessionId) {
		// db.users.find({"accountId":"imedina","sessions.id":"8l665MB3Q7MdKzfGJBJd"},
		// { "sessions.$":1 ,"_id":0})
		// ESTO DEVOLVERA SOLO UN OBJETO SESION, EL QUE CORRESPONDA CON LA ID
		// DEL FIND

		BasicDBObject query = new BasicDBObject("accountId", accountId);
		BasicDBObject fields = new BasicDBObject();
		query.put("sessions.id", sessionId);
		fields.put("_id", 0);
		fields.put("sessions.$", 1);

		DBCursor iterator = userCollection.find(query, fields);
		DBObject dbo = iterator.next();

		Session[] sessions = new Gson().fromJson(dbo.get("sessions").toString(), Session[].class);

		return sessions[0];
	}

	@Override
	public String createDataToProject(String project, String accountId, String sessionId, Data data,
			InputStream fileData) {
		String fileName = data.getFileName();
		// CREATING A RANDOM TEMP FOLDER
		String randomFolder = TMP + "/" + StringUtils.randomString(20);
		try {
			FileUtils.createDirectory(randomFolder);
		} catch (Exception e) {
			e.printStackTrace();
			return "Could not create the upload temp directory";
		}
		// COPYING TO DISK
		File tmpFile = new File(randomFolder + "/" + fileName);
		try {
			IOUtils.write(tmpFile, fileData);
		} catch (IOException e) {
			e.printStackTrace();
			return "Could not write the file on disk";
		}
		// COPYING FROM TEMP TO ACCOUNT DIR
		File userFile = new File(GCSA_ACCOUNT + "/" + getAccountIdBySessionId(sessionId) + "/" + project + "/"
				+ fileName);
		try {
			FileUtils.touch(userFile);
			FileUtils.copy(tmpFile, userFile);

			// INSERT DATA OBJECT ON MONGO
			BasicDBObject dataDBObject = (BasicDBObject) JSON.parse(new Gson().toJson(data));
			BasicDBObject query = new BasicDBObject();
			BasicDBObject item = new BasicDBObject();
			BasicDBObject action = new BasicDBObject();
			query.put("sessions.id", sessionId);
			query.put("projects.id", project.toLowerCase());
			item.put("projects.$.data", dataDBObject);
			action.put("$push", item);
			System.out.println(action);
			WriteResult result = userCollection.update(query, action);

			// db.users.update({"accountId":"fsalavert","projects.name":"Default"},{$push:{"projects.$.data":{"a":"a"}}})

			if (result.getError() != null) {
				FileUtils.deleteDirectory(userFile);
				FileUtils.deleteDirectory(tmpFile);
				return "MongoDB error, " + result.getError() + " files will be deleted";
			}
			FileUtils.deleteDirectory(tmpFile);

			// BasicDBObject actitvityQuery = new BasicDBObject("accountId",
			// accountId);
			// query.put("sessions.id", sessionId);
			// updateMongo("set", actitvityQuery,"lastActivity",
			// GcsaUtils.getTime());
			return null;

		} catch (IOException e) {
			e.printStackTrace();
			return "Copying from tmp folder to account folder";
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

		try {
			if (jobFolder == null) {
				// CREATE JOB FOLDER
				ioManager.createJobFolder(accountId, project, jobId);

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
				ioManager.removeJobFolder(accountId, project, jobId);
				return "MongoDB error, " + result.getError() + " files will be deleted";
			}
			return jobId;
		} catch (UserManagementException e) {
			e.printStackTrace();
			return null;
		}
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

	private boolean validate(String email) {
		String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
				+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		Pattern pattern = Pattern.compile(EMAIL_PATTERN);
		return pattern.matcher(email).matches();
	}

	private void getCollection(String nameCollection) {

		if (!mongoDB.collectionExists(nameCollection)) {
			userCollection = mongoDB.createCollection(nameCollection, new BasicDBObject());
		} else {
			userCollection = mongoDB.getCollection(nameCollection);
		}
	}

	private boolean userExist(String accountId) throws UserManagementException {
		boolean userExist = true;

		BasicDBObject query = new BasicDBObject();
		query.put("accountId", accountId);
		DBCursor iterator = userCollection.find(query);

		if (iterator.count() < 1)
			userExist = false;

		return userExist;
	}

	private void getDataBase(String nameDataBase) {
		mongoDB = mongo.getDB(nameDataBase);
	}

	private void connectToMongo() throws UserManagementException {
		try {
			mongo = new Mongo(CloudSessionManager.properties.getProperty("GCSA.MONGO.IP"),
					Integer.parseInt(CloudSessionManager.properties.getProperty("GCSA.MONGO.PORT")));

		} catch (UnknownHostException e) {
			throw new UserManagementException("ERROR: Not connected to mongoDB " + e.toString());
		} catch (MongoException e) {
			throw new UserManagementException("ERROR: Not connected to mongoDB " + e.toString());
		}
	}

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

		String jobFolder = GCSA_ACCOUNT + "/" + getAccountIdBySessionId(sessionId) + "/projects/" + project + "/jobs/"
				+ jobId + "/";
		if (new File(jobFolder).exists()) {
			return jobFolder;
		} else {
			return "ERROR: Invalid jobId";
		}
	}

	@Override
	public String getDataPath(String dataId, String sessionId) {
		// projects:default:jobs:ae8Bhh8Y:test.txt
		// projects:default:virtualdir:test.txt
		String path = null;
		if (dataId.contains(":jobs:")) {
			path = "/" + dataId.replaceAll(":", "/");
		} else {
			String[] fields = dataId.split(":", 3);
			if (fields.length > 3) {
				return "ERROR: unexpected format on '" + dataId + "'";
			} else {
				path = "/" + fields[0] + "/" + fields[1] + "/" + fields[2];
			}
		}

		String dataPath = GCSA_ACCOUNT + "/" + getAccountIdBySessionId(sessionId) + path;

		if (new File(dataPath).exists()) {
			return dataPath;
		} else {
			return "ERROR: data '" + dataId + "' not found";
		}
		// return dataPath;
	}

	@Override
	public String getAccountBySessionId(String sessionId, String lastActivity) {
		System.out.println(sessionId);
		BasicDBObject query = new BasicDBObject();
		BasicDBObject fields = new BasicDBObject();
		query.put("sessions.id", sessionId);
		fields.put("_id", 0);
		fields.put("password", 0);
		fields.put("sessions", 0);
		fields.put("oldSessions", 0);
		fields.put("data", 0);

		DBObject item = userCollection.findOne(query, fields);
		if (item != null) {
			// if has not been modified since last time was call
			if (lastActivity != null && item.get("lastActivity").toString().equals(lastActivity)) {
				return "{}";
			}
			// item.put(arg0, arg1)
			return item.toString();
		} else {
			return "ERROR: Invalid sessionId";
		}
	}

}
