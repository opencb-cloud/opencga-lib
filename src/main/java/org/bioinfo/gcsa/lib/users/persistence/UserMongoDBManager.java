package org.bioinfo.gcsa.lib.users.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import org.bioinfo.commons.io.utils.FileUtils;
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
	private String GCSA_MONGO_DB = CloudSessionManager.properties
			.getProperty("GCSA.MONGO.DB");
	private String GCSA_MONGO_COLLECTION = CloudSessionManager.properties
			.getProperty("GCSA.MONGO.COLLECTION");
	private String GCSA_ENV = System.getenv(CloudSessionManager.properties
			.getProperty("GCSA.ENV.PATH"));

	public UserMongoDBManager() throws UserManagementException {
		connectToMongo();
		getDataBase(GCSA_MONGO_DB);
		getCollection(GCSA_MONGO_COLLECTION);
	}

	public void createUser(String accountId, String password,
			String accountName, String email, Session session)
			throws UserManagementException {
		User userLoad = null;

		if (!userExist(accountId)) {// SI NO EXISTE USUARIO
			System.out.println("NO EXISTE USUARIO");
			
			if (new File(GCSA_ENV + "/"+accountId).exists()
					&& new File(GCSA_ENV +"/"+ accountId +"/"+ "user.conf").exists()) {
				// EL USUARIO NO EXISTE PERO TIENE CARPETA Y FICHERO DE
				// CONFIGURACION
				System.out.println("EL USUARIO NO EXISTE PERO TIENE CARPETA Y FICHERO DE CONFIGURACION");
				try {
					BufferedReader br = new BufferedReader(new FileReader(
							GCSA_ENV +"/"+ accountId +"/"+ "user.conf"));
					System.out.println("Estamos cargando el fichero");
					userLoad = new Gson().fromJson(br, User.class);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

			}

			ioManager.createScaffoldAccountId(accountId);
			if (userLoad == null) {
				userLoad = new User(accountId, accountName, password, email,session);
			}

			userCollection.insert((DBObject) JSON.parse(new Gson().toJson(userLoad)));

		} else {// SI EXISTE USUARIO

		}

	}

	public void createAnonymousUser(String accountId, String password,
			String email) {

	}

	public String login(String accountId, String password) {
		boolean correctLogin = false;
		String randomString = "";
		BasicDBObject query = new BasicDBObject();
		query.put("accountId", accountId);
		query.put("password", password);

		System.out.println(query.toString());

		DBCursor iterator = userCollection.find(query);

		// Comentar
		while (iterator.hasNext()) {
			System.out.println(iterator.next().toString());
		}
		System.out.println(iterator.count());
		// FinComentar

		if (iterator.count() == 1) {
			correctLogin = true;
			// Tienes que añadir para que modifique el valor sobre el ID del
			// usuario.
		}

		return randomString;
	}

	public String testPipe(String accountId, String password) {
		StringBuilder strB = new StringBuilder();
		BasicDBObject query = new BasicDBObject();
		query.put("accountId", accountId);
		query.put("password", password);

		DBCursor iterator = userCollection.find(query);

		User user;
		DBObject dbo;
		while (iterator.hasNext()) {
			dbo = iterator.next();
			strB = strB.append(dbo.toString());
			System.out.println("dbo.toString(): " + dbo.toString());
			// new Gson().fromJson(dbo.toString(), User.class);
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
			// mongo = new Mongo(
			// CloudSessionManager.properties.getProperty("GCSA.MONGO.IP"),
			// Integer.parseInt(CloudSessionManager.properties
			// .getProperty("GCSA.MONGO.PORT")));
			// TODO ESTO HAY QUE ARREGLARLO
			mongo = new Mongo("127.0.0.1", 27017);
		} catch (UnknownHostException e) {
			throw new UserManagementException(
					"ERROR: Not connected to mongoDB " + e.toString());
		} catch (MongoException e) {
			throw new UserManagementException(
					"ERROR: Not connected to mongoDB " + e.toString());
		}
	}

}
