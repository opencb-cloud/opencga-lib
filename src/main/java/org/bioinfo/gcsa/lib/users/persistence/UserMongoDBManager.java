package org.bioinfo.gcsa.lib.users.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.commons.io.utils.IOUtils;
import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.gcsa.lib.GcsaUtils;
import org.bioinfo.gcsa.lib.users.CloudSessionManager;
import org.bioinfo.gcsa.lib.users.IOManager;
import org.bioinfo.gcsa.lib.users.beans.Acl;
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
	private String GCSA_ACCOUNT = GCSA_ENV+CloudSessionManager.properties.getProperty("GCSA.ACCOUNT.PATH");
	private String TMP = CloudSessionManager.properties.getProperty("TMP.PATH");

	
	public UserMongoDBManager() throws UserManagementException {
		connectToMongo();
		getDataBase(GCSA_MONGO_DB);
		getCollection(GCSA_MONGO_COLLECTION);
	}

	/////////////////////////////////////
	/*
	 * User methods
	 */
	////////////////////////////////////
	
	public void createUser(String accountId, String password,
			String accountName, String email, Session session)
			throws UserManagementException {
		User userLoad = null;

		System.out.println( CloudSessionManager.properties.getProperty("GCSA.ACCOUNT.PATH"));
		
		if (!userExist(accountId)) {// SI NO EXISTE USUARIO
			System.out.println("NO EXISTE USUARIO");
			
			if (new File(GCSA_ACCOUNT + "/" + accountId).exists()
					&& new File(GCSA_ACCOUNT + "/" + accountId + "/" + "account.conf")
							.exists()) {
				// EL USUARIO NO EXISTE PERO TIENE CARPETA Y FICHERO DE
				// CONFIGURACION
				System.out
						.println("EL USUARIO NO EXISTE PERO TIENE CARPETA Y FICHERO DE CONFIGURACION");
				try {
					BufferedReader br = new BufferedReader(new FileReader(
							GCSA_ACCOUNT + "/" + accountId + "/" + "user.conf"));
					System.out.println("Estamos cargando el fichero");
					userLoad = new Gson().fromJson(br, User.class);
					userLoad.addSession(session);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

			}

			ioManager.createScaffoldAccountId(accountId);
			System.out.println("ha creado las carpetas: " + accountId);
			
			if (validate(email)){
			
				if (userLoad == null) {
					userLoad = new User(accountId, accountName, password, email,
							session);
				}
	
				userCollection.insert((DBObject) JSON.parse(new Gson()
						.toJson(userLoad)));
			}
			else{
				throw new UserManagementException(
						"ERROR: email not valid");
			}

		} else {// SI EXISTE USUARIO
			throw new UserManagementException(
					"ERROR: The account already exists");
		}

	}

	public void createAnonymousUser(String accountId, String password, String email) {

	}

	public String login(String accountId, String password, Session session) {
		BasicDBObject query = new BasicDBObject();
		String id = "";
		query.put("accountId", accountId);
		query.put("password", password);

		System.out.println(query.toString());

		DBCursor iterator = userCollection.find(query);

		if (iterator.count() == 1) {
			User user = new Gson().fromJson(iterator.next().toString(), User.class);
			user.addSession(session);
			id = session.getId();
			List<Session> sess = user.getSessions();

			BasicDBObject filter = new BasicDBObject("accountId", accountId);
			updateMongo(filter, "sessions", sess);

			// mover a oldSessions todas las sesiones con mas de 24 horas.
			Calendar cal;
			List<Session> s = user.getSessions();
			Date fechaActual = GcsaUtils.toDate(GcsaUtils.getTime());
			List<Session> oldSes = user.getOldSessions();
			boolean changed = false;
			
			for (int i = 0; i < s.size(); i++) {

				Date loginDate = GcsaUtils.toDate(s.get(i).getLogin());
				// String month = s.get(i).getLogin().substring(, endIndex);

				cal = new GregorianCalendar();
				cal.setTime(loginDate);

				cal.setTimeInMillis(loginDate.getTime());
				// sumamos 24h a la fecha del login
				cal.add(Calendar.DATE, 1);

				Date fechaCaducidad = new Date(cal.getTimeInMillis());

				if (fechaCaducidad.compareTo(fechaActual) < 0) {
					// caducada -> movemos a oldSessions
					System.out.println("FECHA CADUCADA : " + loginDate.toString());
					s.get(i).setLogout(GcsaUtils.getTime());
					oldSes.add(s.get(i));
					s.remove(i);
					changed = true;
				}
			}
			
			if (changed){
				updateMongo(new BasicDBObject("accountId",user.getAccountId()), "sessions", s);
				updateMongo(new BasicDBObject("accountId",user.getAccountId()), "oldSessions", oldSes);
			}
			
		}

		return id;
	}
	
	public String logout(String accountId, String sessionId) {
		String logoutStatus = "ERROR";
		if(checkSessionId(accountId, sessionId)){
			
			//INSERT DATA OBJECT IN MONGO
			Session session = getSessionId(accountId, sessionId);
			session.setLogout(GcsaUtils.getTime());
			BasicDBObject dataDBObject = (BasicDBObject) JSON.parse(new Gson().toJson(session));
			System.out.println("ses_id: " + dataDBObject.toString());
			BasicDBObject query = new BasicDBObject();
			BasicDBObject item = new BasicDBObject();
			BasicDBObject action = new BasicDBObject();
			query.put("accountId", accountId);
			query.put("sessions.id", sessionId);
			item.put("oldSessions", dataDBObject);
			action.put("$push", item);
			
			userCollection.update(query, action);
			logoutStatus = "SUCCESS";
		}
		
		return logoutStatus;
	}

	public String testPipe(String accountId, String password) {
		StringBuilder strB = new StringBuilder();

		BasicDBObject query = new BasicDBObject();
		query.put("accountId", accountId);
		query.put("password", password);

		DBCursor iterator = userCollection.find(query);

		DBObject dbo;
		while (iterator.hasNext()) {
			dbo = iterator.next();
			strB = strB.append(dbo.toString());
			System.out.println("dbo.toString(): " + dbo.toString());
		}

		return "";
	}

	public String getUserByAccountId(String accountId, String sessionId) {
		String userStr = "";
		
		BasicDBObject query = new BasicDBObject();
		
		query.put("accountId", accountId);
		query.put("sessions.id", sessionId);

		DBCursor iterator = userCollection.find(query);

		if (iterator.count() == 1) {
			userStr = iterator.next().toString();
		}
		
		return userStr;
	}

	public String getUserByEmail(String email, String sessionId) {
		String userStr = "";
		
		BasicDBObject query = new BasicDBObject();
		
		query.put("email", email);
		query.put("sessions.id", sessionId);

		DBCursor iterator = userCollection.find(query);

		if (iterator.count() == 1) {
			userStr = iterator.next().toString();
		}
		
		return userStr;
	}
	
	//////////////////////////////////////
	/*
	 * Project methods
	 */
	//////////////////////////////////////
	
	@Override
	public boolean checkSessionId(String accountId, String sessionId) {
		boolean isValidSession = false;

		BasicDBObject query = new BasicDBObject();
		query.put("accountId", accountId);
		query.put("sessions.id", sessionId);
		DBCursor iterator = userCollection.find(query);
		
		if (iterator.count() > 0)
			isValidSession = true;
		
		return isValidSession;
	}
	
	public String getAllProjectsBySessionId(String accountId, String sessionId) {
		String projectsStr = "";
		User user = null;
		
		BasicDBObject query = new BasicDBObject();
		
		query.put("accountId", accountId);
		query.put("sessions.id", sessionId);

		DBCursor iterator = userCollection.find(query);

		if (iterator.count() == 1) {
			user = new Gson().fromJson(iterator.next().toString(),User.class);
			projectsStr = JSON.parse(new Gson().toJson(user.getProjects())).toString();
		}
		
		return projectsStr;
	}

//	public void createProject(String accountId, String sessionId)
//			throws UserManagementException {
//		// try {
//		//
//		// }catch(IOException e) {
//		// throw new UserMangeentException("": e.toString);
//		// }
//	}

	public String createProject(Project project, String accountId, String sessionId){
		BasicDBObject filter = new BasicDBObject("accountId",accountId);
		filter.put("session.id", sessionId);
		List<Project> projects = new Gson().fromJson(getUserByAccountId(accountId, sessionId),User.class).getProjects();
		projects.add(project);
		updateMongo(filter, "projects", projects);
		return projects.toString();
	}
	
	public String getAccountIdBySessionId(String sessionId) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject fields = new BasicDBObject();
		query.put("sessions.id", sessionId);
		fields.put("_id", 0);
		fields.put("accountId", 1);
		DBObject item = userCollection.findOne(query,fields);

		if(item!=null){
			return (String) item.get("accountId");
		}else{
			return "ERROR: Invalid sessionId";
		}
	}
	
	@Override
	public Session getSessionId(String accountId, String sessionId) {

		//Set<String> sessionsId = new HashSet<String>();
		
		//db.users.find({"accountId":"imedina","sessions.id":"8l665MB3Q7MdKzfGJBJd"}, { "sessions.$":1 ,"_id":0})
		//ESTO DEVOLVERA SOLO UN OBJETO SESION, EL QUE CORRESPONDA CON LA ID DEL FIND
		
		BasicDBObject query = new BasicDBObject();
		BasicDBObject fields = new BasicDBObject();
		query.put("accountId", accountId);
		query.put("sessions.id", sessionId);
		fields.put("_id", 0);
		fields.put("sessions.$", 1);
		
		DBCursor iterator = userCollection.find(query,fields);
		DBObject dbo = iterator.next();
		System.out.println("dbo.get(): ----> " + dbo.get("sessions"));
		
		Session[] sessions = new Gson().fromJson(dbo.get("sessions").toString(), Session[].class);
		
		String next = dbo.toString();
		System.out.println("next ----> " + next);
		
		next = sessions[0].toString();
		System.out.println("next ----> " + next);

		System.out.println(sessions[0]);
		
		return sessions[0];
	}

//	public HashSet<String> getAllOldIdSessions(String accountId, String sessionId) {
//
//		
//		Set<String> oldSessions = new HashSet<String>();
		
//		//ArrayList<Session> oldSessions = new ArrayList<Session>();
//		
//		BasicDBObject query = new BasicDBObject();
//		BasicDBObject fields = new BasicDBObject();
//		query.put("accountId", accountId);
//		query.put("sessions.id", sessionId);
//		fields.put("_id", 0);
//		fields.put("oldSessions", 1);
//		
//		DBCursor iterator = userCollection.find(query,fields);
//
//		while (iterator.hasNext()) {
//			oldSessions.add(new Gson().fromJson(iterator.next().toString(), Session.class));
//		}
//		
//		return oldSessions;
//		
//	}
	
	@Override
	public String createFileToProject(String project, String fileName, InputStream fileData, String sessionId) {
		//CREATING A RANDOM TEMP FOLDER
		
		String randomFolder = TMP+"/"+StringUtils.randomString(20);
		try {
			FileUtils.createDirectory(randomFolder);
		}catch(Exception e){
			e.printStackTrace();
			return "Could not create the upload temp directory";
		}
		// COPYING TO DISK
		File tmpFile = new File(randomFolder+"/"+ fileName);
		try {
			IOUtils.write(tmpFile, fileData);
		} catch (IOException e) {
			e.printStackTrace();
			return "Could not write the file on disk";	
		}
		//COPYING FROM TEMP TO ACCOUNT DIR
		File userFile = new File(GCSA_ACCOUNT+"/"+getAccountIdBySessionId(sessionId)+"/"+project+"/"+fileName);
		try {
			FileUtils.touch(userFile);
			FileUtils.copy(tmpFile, userFile);
			
			//INSERT DATA OBJECT ON MONGO
			Data data = new Data();
			BasicDBObject dataDBObject = (BasicDBObject) JSON.parse(new Gson().toJson(data));
			BasicDBObject query = new BasicDBObject();
			BasicDBObject item = new BasicDBObject();
			BasicDBObject action = new BasicDBObject();
			query.put("sessions.id", sessionId);
			item.put("projects.$.data", dataDBObject);
			action.put("$push", item);
			WriteResult result = userCollection.update(query, action);
			
			if(result.getError()!=null){
				FileUtils.deleteDirectory(userFile);
				FileUtils.deleteDirectory(tmpFile);
				return "MongoDB error, "+result.getError()+" files will be deleted";
			}
			FileUtils.deleteDirectory(tmpFile);
			return null;
			
		} catch (IOException e) {
			e.printStackTrace();
			return "Copying from tmp folder to account folder";	
		}
		
	}
	
	/////////////////////////
	/*
	 * Job methods
	 */
	////////////////////////
	
	@Override
	public String createJob(String jobName, String toolName, List<String> dataList, String sessionId) {
		String jobId = StringUtils.randomString(8);
		String accountId = getAccountIdBySessionId(sessionId);
		
		try {
			// CREATE JOB FOLDER
			ioManager.createJobFolder(accountId, jobId);
			
			//INSERT DATA OBJECT ON MONGO
			Job job = new Job(jobId, "0", "", "", "", toolName, jobName, "0", "", "", "", "", dataList);
			Data data = new Data("", "dir", "", "", "", "", "", "", "", "", "", new ArrayList<Acl>(), job);
			BasicDBObject dataDBObject = (BasicDBObject) JSON.parse(new Gson().toJson(data));
			BasicDBObject query = new BasicDBObject();
			BasicDBObject item = new BasicDBObject();
			BasicDBObject action = new BasicDBObject();
			query.put("accountId", accountId);
			query.put("projects.status", "1");
			item.put("projects.$.data", dataDBObject);
			action.put("$push", item);
			WriteResult result = userCollection.update(query, action);
			
			if(result.getError()!=null) {
				ioManager.removeJobFolder(accountId, jobId);
				return "MongoDB error, "+result.getError()+" files will be deleted";
			}
			
			return jobId;
		} catch (UserManagementException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	////////////////////////
	/*
	 * Utils
	 */
	///////////////////////
	
	public List<Project> jsonToProjectList(String json) {
		
		Project[] projects = new Gson().fromJson(json, Project[].class);
		
		System.out.println("proyectos del json: " + projects);
		List<Project> p = Arrays.asList(projects);
	
		return p;
	}
	
	
	//////////////////////
	/*
	 * Private classes
	 */
	//////////////////////
	
	private boolean validate(String email) {
		String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"	+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		Pattern pattern = Pattern.compile(EMAIL_PATTERN);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
	
	private void getCollection(String nameCollection) {

		if (!mongoDB.collectionExists(nameCollection)) {
			userCollection = mongoDB.createCollection(nameCollection,
					new BasicDBObject());
		} else {
			userCollection = mongoDB.getCollection(nameCollection);
			System.out.println(userCollection.toString());
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
			 mongo = new Mongo(
			 CloudSessionManager.properties.getProperty("GCSA.MONGO.IP"),
			 Integer.parseInt(CloudSessionManager.properties
			 .getProperty("GCSA.MONGO.PORT")));

		} catch (UnknownHostException e) {
			throw new UserManagementException(
					"ERROR: Not connected to mongoDB " + e.toString());
		} catch (MongoException e) {
			throw new UserManagementException(
					"ERROR: Not connected to mongoDB " + e.toString());
		}
	}

	private void updateMongo(DBObject filter, String field, Object value) {
		BasicDBObject set = new BasicDBObject("$set",new BasicDBObject().append(field,	(DBObject) JSON.parse(new Gson().toJson(value))));
		userCollection.update(filter, set);
	}

	@SuppressWarnings("unused")
	private void updateMongo(BasicDBObject[] filter, String field, Object value) {

		BasicDBObject container = filter[0];
		for (int i = 1; i < filter.length; i++) {
			container.putAll(filter[i].toMap());
		}

		System.out.println("container ---> " + container.toString());

		System.out.println("value -----> "
				+ JSON.parse(new Gson().toJson(value)));

		BasicDBObject set = new BasicDBObject("$set",
				new BasicDBObject().append(field,
						JSON.parse(new Gson().toJson(value))));

		System.out.println("set -----> " + set.toString());

		userCollection.update(container, set);

	}

	@Override
	public String getJobFolder(String jobId, String sessionId) {
//		String accountId = getAccountIdBySessionId(sessionId);
//		BasicDBObject query = new BasicDBObject();
//		BasicDBObject fields = new BasicDBObject();
//		query.put("accountId", accountId);
//		query.put("projects.status", "1");
//		fields.put("_id", 0);
//		fields.put("projects.$", 1);
//		DBObject item = userCollection.findOne(query,fields);
//		Project[] p = new Gson().fromJson(item.get("projects").toString(), Project[].class);

		String jobFolder = GCSA_ACCOUNT+"/"+getAccountIdBySessionId(sessionId)+"/jobs/"+jobId+"/";
		if(new File(jobFolder).exists()) {
			return jobFolder;
		}
		else {
			return "ERROR: Invalid jobId";
		}
	}

	@Override
	public String getAccountBySessionId(String sessionId) {
		System.out.println(sessionId);
		BasicDBObject query = new BasicDBObject();
		BasicDBObject fields = new BasicDBObject();
		query.put("sessions.id", sessionId);
		fields.put("_id", 0);
		fields.put("password", 0);
		fields.put("sessions", 0);
		fields.put("oldSessions", 0);
		DBObject item = userCollection.findOne(query,fields);
		if(item!=null){
			return (String) item.toString();
		}else{
			return "ERROR: Invalid sessionId";
		}
	}

//	public HashSet<String> getAllOldIdSessions(String accountId, String sessionId) {
//
//		
//		Set<String> oldSessions = new HashSet<String>();
//		
//		//ArrayList<Session> oldSessions = new ArrayList<Session>();
//		
//		BasicDBObject query = new BasicDBObject();
//		BasicDBObject fields = new BasicDBObject();
//		query.put("accountId", accountId);
//		query.put("sessions.id", sessionId);
//		fields.put("_id", 0);
//		fields.put("oldSessions", 1);
//		
//		DBCursor iterator = userCollection.find(query,fields);
//
//		while (iterator.hasNext()) {
//			oldSessions.add(new Gson().fromJson(iterator.next().toString(), Session.class));
//		}
//		
//		return oldSessions;
//		
//	}

}
