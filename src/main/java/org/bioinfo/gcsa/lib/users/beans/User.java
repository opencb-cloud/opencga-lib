package org.bioinfo.gcsa.lib.users.beans;
import java.util.List;
import java.util.Vector;

public class User {
	private String id;
	private String accountId;
	private String accountName;
	private String email;
	private String pass;
	private String status;
	private String mailingList;
	private String diskQuota;
	private String diskUsage;
	private Session session;
	private List<Session> oldSessions;
	private List<Project> projects;
	private List<Account> accounts;
	private List<Plugin> plugins;
	private List<Config> configs;

	public User() {
		session = new Session();
		oldSessions = new Vector<Session>();
		oldSessions.add(new Session());
		oldSessions.add(new Session());
		projects = new Vector<Project>();
		projects.add(new Project());
		accounts = new Vector<Account>();
		accounts.add(new Account());
		this.status = "";
		this.id = "";
		this.pass = "";
		this.email = "";
		this.mailingList = "";
		this.accountId = "";
		
		this.accountName = "";
		this.diskQuota= ""; 
		this.diskUsage= "";
	}

	public User(String id, String accountId, String accountName, String email,
			String pass, String status, String mailingList, String diskQuota,
			String diskUsage, Session session, List<Session> oldSessions,
			List<Project> projects, List<Account> accounts,
			List<Plugin> plugins, List<Config> configs) {
		super();
		this.id = id;
		this.accountId = accountId;
		this.accountName = accountName;
		this.email = email;
		this.pass = pass;
		this.status = status;
		this.mailingList = mailingList;
		this.diskQuota = diskQuota;
		this.diskUsage = diskUsage;
		this.session = session;
		this.oldSessions = oldSessions;
		this.projects = projects;
		this.accounts = accounts;
		this.plugins = plugins;
		this.configs = configs;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMailingList() {
		return mailingList;
	}

	public void setMailingList(String mailingList) {
		this.mailingList = mailingList;
	}

	public String getDiskQuota() {
		return diskQuota;
	}

	public void setDiskQuota(String diskQuota) {
		this.diskQuota = diskQuota;
	}

	public String getDiskUsage() {
		return diskUsage;
	}

	public void setDiskUsage(String diskUsage) {
		this.diskUsage = diskUsage;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public List<Session> getOldSessions() {
		return oldSessions;
	}

	public void setOldSessions(List<Session> oldSessions) {
		this.oldSessions = oldSessions;
	}

	public List<Project> getProjects() {
		return projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

	public List<Account> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<Account> accounts) {
		this.accounts = accounts;
	}

	public List<Plugin> getPlugins() {
		return plugins;
	}

	public void setPlugins(List<Plugin> plugins) {
		this.plugins = plugins;
	}

	public List<Config> getConfigs() {
		return configs;
	}

	public void setConfigs(List<Config> configs) {
		this.configs = configs;
	}

	

}
