package org.bioinfo.gcsa.lib.users.persistence;

import java.net.UnknownHostException;
import java.util.List;

import org.bioinfo.gcsa.lib.users.IOManager;
import org.bioinfo.gcsa.lib.users.beans.Project;
import org.bioinfo.gcsa.lib.users.beans.User;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class UserMongoDBManager implements UserManager {
	private IOManager ioManager = new IOManager();
	public UserMongoDBManager() {

	}

	public void createAccountId(String accountId, String password,
			String accountName, String email) throws UserManagementException {
		ioManager.createAccountId(accountId);
		User user = new User(accountId, accountName, password, email);
		
		
		////////////////////////////
		
		
		Mongo m;
		StringBuilder strbuild = new StringBuilder();
		String[] info = infoChrom.split("[:-]");
		try {

			m = new Mongo("127.0.0.1", 27017);
			
			DB db = m.getDB("cellbase");
			DBCollection docs = db.getCollection("features");
			// La consulta a crear es
			// db.features.find({seqname:"chr1",start:{$lt:9999},end:{$gt:0}})
			
			
			BasicDBObject query = new BasicDBObject();
			query.put("seqname", info[0]);
			query.put("start", new BasicDBObject("$lt", Integer.parseInt(info[2])));
			query.put("end", new BasicDBObject("$gt", Integer.parseInt(info[1])));
			
			
			System.out.println(query.toString());
			
			DBCursor iterador = docs.find(query);
			System.out.println(iterador.count());
			
			 while (iterador.hasNext()) {
				 strbuild.append(iterador.next().toString()).append("\n");
			 }

			 System.out.println(iterador.count());
			 
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		}
		return strbuild.toString();

	
		
		
		////////////////////////////
		
		
		
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

	public void createProject(Project project,
			String accountId, String sessionId) throws UserManagementException {
		// TODO Auto-generated method stub
		
	}

	public void createUser(String accountId, String password,
			String accountName, String email) throws UserManagementException {
		// TODO Auto-generated method stub
		
	}

}
