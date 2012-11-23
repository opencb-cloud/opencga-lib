package org.bioinfo.gcsa.lib.users.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.commons.io.utils.IOUtils;
import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.gcsa.lib.GcsaUtils;
import org.bioinfo.gcsa.lib.users.CloudSessionManager;
import org.bioinfo.gcsa.lib.users.IOManager;
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
			
			if (userLoad == null) {
				userLoad = new User(accountId, accountName, password, email,
						session);
			}

			userCollection.insert((DBObject) JSON.parse(new Gson()
					.toJson(userLoad)));

		} else {// SI EXISTE USUARIO
			throw new UserManagementException(
					"ERROR: The account already exists");
		}

	}

	public void createAnonymousUser(String accountId, String password,
			String email) {

	}

	public String login(String accountId, String password, Session session) {
		BasicDBObject query = new BasicDBObject();
		String id = "";
		query.put("accountId", accountId);
		query.put("password", password);

		System.out.println(query.toString());

		DBCursor iterator = userCollection.find(query);

		if (iterator.count() == 1) {
			User user = new Gson().fromJson(iterator.next().toString(),
					User.class);
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
	
	@Override
	public String logout(String accountId, String sessionId) {
		
		
		
		
		
		return null;
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
		return null;
	}

	public String getUserByEmail(String email, String sessionId) {
		return null;
	}

	public void checkSessionId(String accountId, String sessionId) {

	}

	public String getAllProjectsBySessionId(String accountId, String sessionId) {
		return null;
	}

	public void createProject(String accountId, String sessionId)
			throws UserManagementException {
		// try {
		//
		// }catch(IOException e) {
		// threo new UserMangeentException("": e.toString);
		// }
	}

	public List<Project> jsonToProjectList(String json) {
		return null;
	}

	public void createProject(Project project, String accountId,
			String sessionId) throws UserManagementException {

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

	private boolean userExist(String accountID) throws UserManagementException {
		boolean userExist = true;

		BasicDBObject query = new BasicDBObject();
		query.put("accountId", accountID);
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

//			// TODO ESTO HAY QUE ARREGLARLO
//			mongo = new Mongo("127.0.0.1", 27017);
		} catch (UnknownHostException e) {
			throw new UserManagementException(
					"ERROR: Not connected to mongoDB " + e.toString());
		} catch (MongoException e) {
			throw new UserManagementException(
					"ERROR: Not connected to mongoDB " + e.toString());
		}
	}

	private void updateMongo(DBObject filter, String field, Object value) {
		BasicDBObject set = new BasicDBObject("$set",
				new BasicDBObject().append(field,
						(DBObject) JSON.parse(new Gson().toJson(value))));
		userCollection.update(filter, set);
	}

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
	
	public String getAccountIdBySessionId(String sessionId){
		BasicDBObject query = new BasicDBObject();
		BasicDBObject fields = new BasicDBObject();
		query.put("sessions.id", sessionId);
		fields.put("_id", 0);
		fields.put("accountId", 1);
		DBObject item = userCollection.findOne(query,fields);
		return (String) item.get("accountId");
	}
	
	@Override
	public String createFileToProject(String project, String fileName, InputStream fileData, String sessionId) {
		
		System.out.println(getAccountIdBySessionId(sessionId));
		
		System.out.println(project);
		System.out.println(fileName);
		
		System.out.println(sessionId);
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
			return "Writing the file on disk";	
		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createJob(String jobName, String toolName, List<String> dataList, String sessionId) {
		String jobId = StringUtils.randomString(8);
		System.out.println(jobId);
		String accountId = getAccountIdBySessionId(sessionId);
		System.out.println(accountId);
		
//		ioManager.createJobFolder(accountId);
		return jobId;
	}

}
