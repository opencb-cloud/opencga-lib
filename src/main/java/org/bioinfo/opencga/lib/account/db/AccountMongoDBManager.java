package org.bioinfo.opencga.lib.account.db;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.bioinfo.opencga.lib.account.beans.Account;
import org.bioinfo.opencga.lib.account.beans.Acl;
import org.bioinfo.opencga.lib.account.beans.AnalysisPlugin;
import org.bioinfo.opencga.lib.account.beans.Bucket;
import org.bioinfo.opencga.lib.account.beans.Job;
import org.bioinfo.opencga.lib.account.beans.ObjectItem;
import org.bioinfo.opencga.lib.account.beans.Project;
import org.bioinfo.opencga.lib.account.beans.Session;
import org.bioinfo.opencga.lib.utils.Config;
import org.bioinfo.opencga.lib.utils.MailUtils;
import org.bioinfo.opencga.lib.utils.StringUtils;
import org.bioinfo.opencga.lib.utils.TimeUtils;

import com.google.gson.Gson;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

public class AccountMongoDBManager implements AccountManager {

	private MongoClient mongoClient;
	private DB mongoDB;
	private DBCollection userCollection;

	private static Logger logger = Logger.getLogger(AccountMongoDBManager.class);
	private Properties accountProperties;

	private Gson gson;

	public AccountMongoDBManager() throws NumberFormatException, IOException {
		gson = new Gson();
		accountProperties = Config.getAccountProperties();
		connect();
	}

	private void connect() throws NumberFormatException, UnknownHostException {
		logger.info("mongodb connect");
		String db = accountProperties.getProperty("OPENCGA.MONGO.DB");
		String collection = accountProperties.getProperty("OPENCGA.MONGO.COLLECTION");
		String host = accountProperties.getProperty("OPENCGA.MONGO.HOST", "localhost");
		String user = accountProperties.getProperty("OPENCGA.MONGO.USER", "");
		String pass = accountProperties.getProperty("OPENCGA.MONGO.PASS", "");
		int port = Integer.parseInt(accountProperties.getProperty("OPENCGA.MONGO.PORT"));

		mongoClient = new MongoClient(host, port);
		mongoDB = mongoClient.getDB(db);
		boolean auth = mongoDB.authenticate(user, pass.toCharArray());
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

	/**
	 * Account methods ···
	 *****************************/
	private void checkAccountExists(String accountId) throws AccountManagementException {
		BasicDBObject query = new BasicDBObject();
		query.put("accountId", accountId);
		DBObject obj = userCollection.findOne(query);
		if (obj != null) {
			throw new AccountManagementException("the account already exists");
		}
	}

	@Override
	public void createAccount(String accountId, String password, String accountName, String role, String email,
			Session session) throws AccountManagementException {
		checkAccountExists(accountId);
		Account account = new Account(accountId, accountName, password, role, email);
		account.setLastActivity(TimeUtils.getTime());
		WriteResult wr = userCollection.insert((DBObject) JSON.parse(gson.toJson(account)));
		if (wr.getLastError().getErrorMessage() != null) {
			throw new AccountManagementException(wr.getLastError().getErrorMessage());
		}
	}

	@Override
	public String createAnonymousAccount(String accountId, String password, Session session)
			throws AccountManagementException {
		createAccount(accountId, password, "anonymous", "anonymous", "anonymous", session);
		// Everything is ok, so we login account
		// session = new Session();
		return login(accountId, password, session);
	}

	@Override
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
			Date now = TimeUtils.toDate(TimeUtils.getTime());

			// get oldSessions
			for (Session s : accountSessionList) {
				Date loginDate = TimeUtils.toDate(s.getLogin());
				Date fechaCaducidad = TimeUtils.add24HtoDate(loginDate);
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
			fields.put("lastActivity", TimeUtils.getTimeMillis());
			BasicDBObject action = new BasicDBObject("$set", fields);
			WriteResult wr = userCollection.update(query, action);

			if (wr.getLastError().getErrorMessage() == null) {
				if (wr.getN() != 1) {
					throw new AccountManagementException("could not update sessions");
				}
			} else {
				throw new AccountManagementException(wr.getLastError().getErrorMessage());
			}

			// Now login() returns a JSON object with: sessionId, accountId and
			// bucketId
			BasicDBObject result = new BasicDBObject("sessionId", session.getId());
			result.append("accountId", accountId);
			result.append("bucketId", "default");

			// return session.getId();
			return result.toString();
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

	@Override
	public void logout(String accountId, String sessionId) throws AccountManagementException {
		Session session = getSession(accountId, sessionId);
		if (session != null) {
			// INSERT DATA OBJECT IN MONGO
			session.setLogout(TimeUtils.getTime());
			BasicDBObject dataDBObject = (BasicDBObject) JSON.parse(gson.toJson(session));
			BasicDBObject query = new BasicDBObject("accountId", accountId);
			query.put("sessions.id", sessionId);

			updateMongo("push", query, "oldSessions", dataDBObject);
			query.removeField("sessions.id");
			BasicDBObject value = new BasicDBObject("id", sessionId);
			updateMongo("pull", query, "sessions", value);
			updateMongo("set", new BasicDBObject("accountId", accountId), "lastActivity", TimeUtils.getTimeMillis());
		} else {
			throw new AccountManagementException("logout");
		}
	}

	@Override
	public void logoutAnonymous(String accountId, String sessionId) {
		BasicDBObject query = new BasicDBObject();
		query.put("accountId", accountId);
		query.put("sessions.id", sessionId);
		userCollection.remove(query);
	}

	@Override
	public void changePassword(String accountId, String sessionId, String password, String nPassword1, String nPassword2)
			throws AccountManagementException {

		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);
		query.put("password", password);
		BasicDBObject fields = new BasicDBObject("password", nPassword1);
		fields.put("lastActivity", TimeUtils.getTimeMillis());
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

	@Override
	public void changeEmail(String accountId, String sessionId, String nEmail) throws AccountManagementException {
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);
		BasicDBObject fields = new BasicDBObject("email", nEmail);
		fields.put("lastActivity", TimeUtils.getTimeMillis());
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

		MailUtils.sendResetPasswordMail(email, message.toString());
	}

	@Override
	public String getAccountInfo(String accountId, String sessionId, String lastActivity)
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

	@Override
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

	/**
	 * Bucket methods ···
	 *****************************/
	@Override
	public String getBucketsList(String accountId, String sessionId) throws AccountManagementException {
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);

		DBObject item = userCollection.findOne(query);
		if (item != null) {
			String bucketsStr = item.get("buckets").toString();
			updateMongo("set", new BasicDBObject("accountId", accountId), "lastActivity", TimeUtils.getTimeMillis());
			return bucketsStr;
		} else {
			throw new AccountManagementException("invalid sessionId");
		}
	}

	@Override
	public void createBucket(String accountId, Bucket bucket, String sessionId) throws AccountManagementException {
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);
		
		BasicDBObject dataDBObject = (BasicDBObject) JSON.parse(gson.toJson(bucket));
		BasicDBObject action = new BasicDBObject();
		action.put("$push", new BasicDBObject("buckets", dataDBObject));
		action.put("$set", new BasicDBObject("lastActivity", TimeUtils.getTimeMillis()));
		WriteResult wr = userCollection.update(query, action);
		
		if (wr.getLastError().getErrorMessage() == null) {
			if (wr.getN() != 1) {
				throw new AccountManagementException("could not update database, account not found");
			}
			logger.info("bucket created");
		} else {
			throw new AccountManagementException("could not push the bucket");
		}
	}
	@Override
	public void deleteBucket(String accountId, String bucketId, String sessionId) throws AccountManagementException {
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);
		
		BasicDBObject action = new BasicDBObject();
		action.put("$pull", new BasicDBObject("buckets", new BasicDBObject("id",bucketId)));
		action.put("$set", new BasicDBObject("lastActivity", TimeUtils.getTimeMillis()));
		WriteResult wr = userCollection.update(query, action);
		logger.info(query);
		logger.info(action);
		
		if (wr.getLastError().getErrorMessage() == null) {
			if (wr.getN() != 1) {
				throw new AccountManagementException("could not update database, account not found");
			}
			logger.info("bucket deleted");
		} else {
			throw new AccountManagementException("could not delete the bucket");
		}
	}

	@Override
	// accountId, bucketId, objectItem, sessionId
	public void createObjectToBucket(String accountId, String bucketId, ObjectItem objectItem, String sessionId)
			throws AccountManagementException {

		// INSERT DATA OBJECT ON MONGO
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);
		query.put("buckets.id", bucketId.toLowerCase());
		BasicDBObject dataDBObject = (BasicDBObject) JSON.parse(gson.toJson(objectItem));
		BasicDBObject item = new BasicDBObject("buckets.$.objects", dataDBObject);
		BasicDBObject action = new BasicDBObject("$push", item);
		action.put("$set", new BasicDBObject("lastActivity", TimeUtils.getTimeMillis()));
		WriteResult wr = userCollection.update(query, action);

		// db.users.update({"accountId":"fsalavert","buckets.name":"Default"},{$push:{"buckets.$.objects":{"a":"a"}}})

		if (wr.getLastError().getErrorMessage() == null) {
			if (wr.getN() != 1) {
				throw new AccountManagementException("could not update database, with this parameters");
			}
			logger.info("data object created");
		} else {
			throw new AccountManagementException("could not update database, files will be deleted");
		}

	}

	@Override
	public void deleteObjectFromBucket(String accountId, String bucketId, Path objectId, String sessionId)
			throws AccountManagementException {
		// db.users.update({"accountId":"pako","buckets.id":"default"},{$pull:{"buckets.$.objects":{"id":"hola/como/estas/app.js"}}})
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);
		query.put("buckets.id", bucketId.toLowerCase());
		
		BasicDBObject bucketData = new BasicDBObject("buckets.$.objects", new BasicDBObject("id", objectId.toString()));
		BasicDBObject action = new BasicDBObject("$pull", bucketData);
		action.put("$set", new BasicDBObject("lastActivity", TimeUtils.getTimeMillis()));
		
		WriteResult wr = userCollection.update(query, action);
		if (wr.getLastError().getErrorMessage() == null) {
			if (wr.getN() != 1) {
				throw new AccountManagementException("deleteObjectFromBucket(): deleting data, with this parameters");
			}
			logger.info("data object deleted");
		} else {
			throw new AccountManagementException("deleteObjectFromBucket(): could not delete data item from database");
		}
	}
	@Override
	public void deleteObjectsFromBucket(String accountId, String bucketId, String sessionId)
			throws AccountManagementException {
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);
		query.put("buckets.id", bucketId.toLowerCase());
		
		BasicDBObject item = new BasicDBObject("buckets.$.objects", new BasicDBList());
		item.put("lastActivity", TimeUtils.getTimeMillis());
		BasicDBObject action = new BasicDBObject("$set", item);

		WriteResult wr = userCollection.update(query, action);
		if (wr.getLastError().getErrorMessage() == null) {
			if (wr.getN() != 1) {
				throw new AccountManagementException("deleteObjectsFromBucket(): deleting data, with this parameters");
			}
			logger.info("all data objects deleted");
		} else {
			throw new AccountManagementException("deleteObjectsFromBucket(): could not delete data item from database");
		}
	}

	@Override
	public ObjectItem getObjectFromBucket(String accountId, String bucketId, Path objectId, String sessionId)
			throws AccountManagementException {
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);
		query.put("buckets.id", bucketId.toLowerCase());

		BasicDBObject bucketData = new BasicDBObject("buckets.$.objects", "1");
		DBObject obj = userCollection.findOne(query, bucketData);
		if (obj != null) {
			Bucket[] buckets = gson.fromJson(obj.get("buckets").toString(), Bucket[].class);
			List<ObjectItem> dataList = buckets[0].getObjects();
			ObjectItem objectItem = null;
			for (int i = 0; i < dataList.size(); i++) {
				if (dataList.get(i).getId().equals(objectId.toString())) {
					objectItem = dataList.get(i);
					break;
				}
			}
			logger.info(objectItem.getId());
			if (objectItem != null) {
				return objectItem;
			} else {
				throw new AccountManagementException("data not found");
			}
		} else {
			throw new AccountManagementException("could not find data with this parameters");
		}
	}

	public void shareObject(String accountId, String bucketId, Path objectId, Acl acl, String sessionId)
			throws AccountManagementException {
		// TODO
	}

	/**
	 * Project methods ···
	 *****************************/

	@Override
	public String getProjectsList(String accountId, String sessionId) throws AccountManagementException {
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);

		DBObject item = userCollection.findOne(query);
		if (item != null) {
			String jsonStr = item.get("projects").toString();
			updateLastActivity(accountId);
			return jsonStr;
		} else {
			throw new AccountManagementException("invalid sessionId");
		}
	}

	@Override
	public void createProject(String accountId, Project project, String sessionId) throws AccountManagementException {
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);

		BasicDBObject dataDBObject = (BasicDBObject) JSON.parse(gson.toJson(project));
		BasicDBObject action = new BasicDBObject();
		action.put("$push", new BasicDBObject("projects", dataDBObject));
		action.put("$set", new BasicDBObject("lastActivity", TimeUtils.getTimeMillis()));
		WriteResult wr = userCollection.update(query, action);

		if (wr.getLastError().getErrorMessage() == null) {
			if (wr.getN() != 1) {
				throw new AccountManagementException("could not update database, account not found");
			}
			logger.info("project created");
		} else {
			throw new AccountManagementException("could not push the project");
		}
	}

	@Override
	public void createJob(String accountId, String projectId, Job job, String sessionId)
			throws AccountManagementException {
		BasicDBObject jobDBObject = (BasicDBObject) JSON.parse(gson.toJson(job));
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);
		query.put("projects.id", projectId);
		BasicDBObject item = new BasicDBObject();
		item.put("projects.$.jobs", jobDBObject);
		BasicDBObject action = new BasicDBObject("$push", item);
		action.put("$set", new BasicDBObject("lastActivity", TimeUtils.getTimeMillis()));
		WriteResult result = userCollection.update(query, action);

		if (result.getLastError().getErrorMessage() == null) {
			if (result.getN() != 1) {
				throw new AccountManagementException("deleting data, with this parameters");
			}
			logger.info("createJob(), job created in database");
		} else {
			throw new AccountManagementException("could not create job in database");
		}
	}

	@Override
	public void deleteJobFromProject(String accountId, String projectId, String jobId, String sessionId)
			throws AccountManagementException {
		// db.users.update({"accountId":"paco"},{$pull:{"jobs":{"id":"KIDicL1OpfJ97Cu"}}})
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);
		query.put("projects.id", projectId.toLowerCase());

		BasicDBObject jobObj = new BasicDBObject("projects.$.jobs", new BasicDBObject("id", jobId));
		BasicDBObject action = new BasicDBObject("$pull", jobObj);
		action.put("$set", new BasicDBObject("lastActivity", TimeUtils.getTimeMillis()));

		WriteResult wr = userCollection.update(query, action);
		if (wr.getLastError().getErrorMessage() == null) {
			if (wr.getN() != 1) {
				throw new AccountManagementException("deleteJobFromProject(): deleting job, with this parameters");
			}
			logger.info("job deleted");
		} else {
			throw new AccountManagementException("deleteJobFromProject(): could not delete job item from database");
		}
	}

	@Override
	public Job getJob(String accountId, String jobId, String sessionId) throws AccountManagementException {
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);
		query.put("projects.jobs.id", jobId);

		BasicDBObject jobObj = new BasicDBObject("projects.$", "1");
		DBObject obj = userCollection.findOne(query, jobObj);
		if (obj != null) {
			Project[] projects = gson.fromJson(obj.get("projects").toString(), Project[].class);
			List<Job> jobList = projects[0].getJobs();
			Job job = null;
			for (int i = 0; i < jobList.size(); i++) {
				if (jobList.get(i).getId().equals(jobId.toString())) {
					job = jobList.get(i);
					break;
				}
			}
			logger.info(job);
			if (job != null) {
				return job;
			} else {
				throw new AccountManagementException("job not found");
			}
		} else {
			throw new AccountManagementException("could not find job with this parameters");
		}
	}

	@Override
	public Project getJobProject(String accountId, String jobId, String sessionId) throws AccountManagementException {
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);
		query.put("projects.jobs.id", jobId);

		BasicDBObject jobObj = new BasicDBObject("projects.$", "1");
		DBObject obj = userCollection.findOne(query, jobObj);
		if (obj != null) {
			Project project = null;
			project = gson.fromJson(obj.get("projects").toString(), Project[].class)[0];
			if (project != null) {
				return project;
			} else {
				throw new AccountManagementException("job not found");
			}
		} else {
			throw new AccountManagementException("could not find job with this parameters");
		}
	}

	@Override
	public int getJobIndex(String accountId, String jobId, String sessionId) throws AccountManagementException {
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);
		query.put("projects.jobs.id", jobId);

		BasicDBObject jobObj = new BasicDBObject("projects.$", "1");
		DBObject obj = userCollection.findOne(query, jobObj);
		int position = -1;
		if (obj != null) {
			Project[] projects = gson.fromJson(obj.get("projects").toString(), Project[].class);
			List<Job> jobList = projects[0].getJobs();
			for (int i = 0; i < jobList.size(); i++) {
				if (jobList.get(i).getId().equals(jobId.toString())) {
					position = i;
					break;
				}
			}
			logger.info("getJobIndexFromProject: " + position);
			if (position != -1) {
				return position;
			} else {
				throw new AccountManagementException("job index not found");
			}
		} else {
			throw new AccountManagementException("could not find job index with this parameters");
		}
	}

	@Override
	public Path getJobPath(String accountId, String jobId, String sessionId) throws AccountManagementException {
		Job job = getJob(accountId, jobId, sessionId);
		return Paths.get(job.getOutdir());
	}

	@Override
	public String getJobStatus(String accountId, String jobId, String sessionId) throws AccountManagementException {
		Job job = getJob(accountId, jobId, sessionId);
		return job.getStatus();
	}

	@Override
	public void incJobVisites(String accountId, String jobId, String sessionId) throws AccountManagementException {
		int position = getJobIndex(accountId, jobId, sessionId);
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);
		query.put("projects.jobs.id", jobId);

		BasicDBObject item = new BasicDBObject("projects.$.jobs." + position + ".visites", 1);
		BasicDBObject action = new BasicDBObject("$inc", item);
		action.put("$set", new BasicDBObject("lastActivity", TimeUtils.getTimeMillis()));

		WriteResult result = userCollection.update(query, action);
		if (result.getLastError().getErrorMessage() == null) {
			if (result.getN() != 1) {
				throw new AccountManagementException("could not update database, with this parameters");
			}
		} else {
			throw new AccountManagementException("could not update database");
		}
	}

	@Override
	public void setJobCommandLine(String accountId, String jobId, String commandLine, String sessionId)
			throws AccountManagementException {
		// db.users.update({"accountId":"paco","projects.id":"default"},{$set:{"projects.$.jobs.0.commandLine":"hola"}})
		int position = getJobIndex(accountId, jobId, sessionId);
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		query.put("sessions.id", sessionId);
		query.put("projects.jobs.id", jobId);

		// If you have two $set actions to do, put them together, you can not
		// create a BasicDBObject with two $set keys as you cannot set two keys
		// with, the same name on a BasicDBObject, any previous BasicDBObject
		// put() will be overridden.
		// NOTE: this can be done with a query in the mongo console but not in
		// JAVA.
		BasicDBObject item = new BasicDBObject("projects.$.jobs." + position + ".commandLine", commandLine);
		item.put("lastActivity", TimeUtils.getTimeMillis());
		BasicDBObject action = new BasicDBObject("$set", item);

		WriteResult result = userCollection.update(query, action);
		if (result.getLastError().getErrorMessage() == null) {
			if (result.getN() != 1) {
				throw new AccountManagementException("could not update database, with this parameters");
			}
			logger.info("job commandLine updated");
		} else {
			throw new AccountManagementException("could not update database");
		}
	}

	/********************
	 * 
	 * ANALYSIS METHODS
	 * 
	 ********************/

	@Override
	public List<AnalysisPlugin> getUserAnalysis(String sessionId) throws AccountManagementException {
		BasicDBObject query = new BasicDBObject("sessions.id", sessionId);
		BasicDBObject fields = new BasicDBObject();
		fields.put("_id", 0);
		fields.put("plugins", 1);

		DBObject item = userCollection.findOne(query, fields);
		if (item != null) {
			AnalysisPlugin[] userAnalysis = gson.fromJson(item.get("plugins").toString(), AnalysisPlugin[].class);
			return Arrays.asList(userAnalysis);
		} else {
			throw new AccountManagementException("invalid session id");
		}
	}

	/********************
	 * 
	 * UTILS
	 * 
	 ********************/

	public List<Bucket> jsonToBucketList(String json) {

		Bucket[] buckets = gson.fromJson(json, Bucket[].class);
		return Arrays.asList(buckets);
	}

	private void updateMongo(String operator, DBObject filter, String field, Object value) {
		BasicDBObject set = null;
		if (String.class.isInstance(value)) {
			set = new BasicDBObject("$" + operator, new BasicDBObject().append(field, value));
		} else {
			set = new BasicDBObject("$" + operator, new BasicDBObject().append(field,
					(DBObject) JSON.parse(gson.toJson(value))));
		}
		userCollection.update(filter, set);
	}

	@SuppressWarnings("unused")
	private void updateMongo(BasicDBObject[] filter, String field, Object value) {
		BasicDBObject container = filter[0];
		for (int i = 1; i < filter.length; i++) {
			container.putAll(filter[i].toMap());
		}
		BasicDBObject set = new BasicDBObject("$set", new BasicDBObject().append(field, JSON.parse(gson.toJson(value))));
		userCollection.update(container, set);

	}

	private void updateLastActivity(String accountId) throws AccountManagementException {
		BasicDBObject query = new BasicDBObject("accountId", accountId);
		BasicDBObject action = new BasicDBObject("lastActivity", TimeUtils.getTimeMillis());

		WriteResult result = userCollection.update(query, action);
		if (result.getLastError().getErrorMessage() == null) {
			if (result.getN() != 1) {
				throw new AccountManagementException("could not update lastActivity, with this parameters");
			}
			logger.info("lastActivity updated");
		} else {
			throw new AccountManagementException("could not update database");
		}
	}

	/* TODO Mirar estos métodos */
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

	// private String getAccountPath(String accountId) {
	// return accounts + "/" + accountId;
	// }

	// private String getBucketPath(String accountId, String bucketId) {
	// return getAccountPath(accountId) + "/buckets/" + bucketId;
	// }

	// private String accountConfPath(String accountId) {
	// return accounts + "/" + accountId + "/" + "account.conf";
	// }

	// public void createAccount(String accountId, String password, String
	// accountName, String email, Session session)
	// throws AccountManagementException {
	//
	// checkAccountExists(accountId);
	//
	// Account account = null;
	//
	// File accountDir = new File(getAccountPath(accountId));
	// File accountConf = new File(accountConfPath(accountId));
	// if (accountDir.exists() && accountConf.exists()) {
	// // covert user mode file to mode mongo
	// // EL USUARIO NO EXISTE PERO TIENE CARPETA Y FICHERO DE
	// // CONFIGURACION
	// try {
	// BufferedReader br = new BufferedReader(new FileReader(accountConf));
	// account = gson.fromJson(br, Account.class);
	// account.addSession(session);
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// }
	//
	// }
	//
	// try {
	// ioManager.createAccount(accountId);
	// } catch (IOManagementException e) {
	// e.printStackTrace();
	// }
	//
	// if (account == null) {
	// account = new Account(accountId, accountName, password, email);
	// account.setLastActivity(GcsaUtils.getTime());
	// }
	// WriteResult wr = userCollection.insert((DBObject)
	// JSON.parse(gson.toJson(account)));
	// if (wr.getLastError().getErrorMessage() != null) {
	// throw new
	// AccountManagementException(wr.getLastError().getErrorMessage());
	// }
	// }

	// public String getUserByEmail(String email, String sessionId) {
	// String userStr = "";
	//
	// BasicDBObject query = new BasicDBObject("email", email);
	// query.put("sessions.id", sessionId);
	//
	// DBCursor iterator = userCollection.find(query);
	//
	// if (iterator.count() == 1) {
	// userStr = iterator.next().toString();
	// updateMongo("set", query, "lastActivity", GcsaUtils.getTimeMillis());
	// }
	//
	// return userStr;
	// }

	// @Override
	// public boolean checkSessionId(String accountId, String sessionId) {
	// boolean isValidSession = false;
	//
	// BasicDBObject query = new BasicDBObject("accountId", accountId);
	// query.put("sessions.id", sessionId);
	// DBCursor iterator = userCollection.find(query);
	//
	// if (iterator.count() > 0) {
	// isValidSession = true;
	// }
	//
	// return isValidSession;
	// }
}
