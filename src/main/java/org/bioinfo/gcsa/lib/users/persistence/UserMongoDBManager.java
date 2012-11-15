package org.bioinfo.gcsa.lib.users.persistence;

import java.net.UnknownHostException;
import java.util.List;

import org.bioinfo.gcsa.lib.users.CloudSessionManager;
import org.bioinfo.gcsa.lib.users.IOManager;
import org.bioinfo.gcsa.lib.users.beans.Project;
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

	public UserMongoDBManager() throws UserManagementException {
		// TODO Hacer el pool de 10 conexiones a mongo
		connectToMongo();
	}

	public void createUser(String accountId, String password,
			String accountName, String email) throws UserManagementException {

		if (!userExist(accountId)) {

			// creacion de carpetas correspondientes a la creacion de un nuevo
			// usuario
			ioManager.createAccountId(accountId);

			User user = new User(accountId, accountName, password, email);

			getDataBase(CloudSessionManager.properties
					.getProperty("GCSA.MONGO.DB"));
			getCollection(CloudSessionManager.properties
					.getProperty("GCSA.MONGO.COLLECTION"));

			userCollection
					.insert((DBObject) JSON.parse(new Gson().toJson(user)));
		}
		else{
			throw new UserManagementException("ERROR: User has been created");
		}
	}

	public void createAnonymousUser(String accountId, String password,
			String email) {

	}

	public String login(String accountId, String password) {
		return null;
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
		}
	}

	private boolean userExist(String accountID) throws UserManagementException {
		boolean userExist = true;

		BasicDBObject query = new BasicDBObject();
		query.put("accountId", accountID);
		DBCursor iterator = userCollection.find(query);

		if (iterator.count() > 0)
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

}
