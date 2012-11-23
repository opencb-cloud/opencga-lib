package org.bioinfo.gcsa.lib.users.beans;

import java.util.ArrayList;
import java.util.List;

public class User {
	private String accountId;
	private String accountName;
	private String email;
	private String password;
	private String status;
	private String mailingList;
	private String diskQuota;
	private String diskUsage;
	private String lastActivity;
	private List<Session> sessions = new ArrayList<Session>();
	private List<Session> oldSessions = new ArrayList<Session>();
	private List<Project> projects = new ArrayList<Project>();
	private List<Account> accounts = new ArrayList<Account>();
	private List<Plugin> plugins = new ArrayList<Plugin>();
	private List<Config> configs = new ArrayList<Config>();
	private List<Data> data = new ArrayList<Data>();

	public User() {
		oldSessions = new ArrayList<Session>();
		oldSessions.add(new Session());
		oldSessions.add(new Session());
		projects.add(new Project());
		accounts = new ArrayList<Account>();
		accounts.add(new Account());
		this.status = "";
		this.password = "";
		this.email = "";
		this.lastActivity = "";
		this.mailingList = "";
		this.accountId = "";
		this.accountName = "";
		this.diskQuota = "";
		this.diskUsage = "";
	}

	public User(String accountId, String accountName, String password, String email,Session session) {
		this.accountId = accountId;
		this.accountName = accountName;
		this.email = email;
		this.password = password;
		this.status = "1";
		this.mailingList = "";
		this.lastActivity = "";
		this.diskQuota = "2000000";
		this.diskUsage = "";
		this.sessions.add(session);
		this.projects.add(new Project());
		this.data.add(new Data());
	}

	public User(String accountId, String accountName, String email,
			String password, String status, String mailingList,
			String diskQuota, String diskUsage, Session session,
			List<Session> oldSessions, List<Project> projects,String lastActivity,
			List<Account> accounts, List<Plugin> plugins, List<Config> configs) {
		this.accountId = accountId;
		this.accountName = accountName;
		this.email = email;
		this.password = password;
		this.status = status;
		this.mailingList = mailingList;
		this.diskQuota = diskQuota;
		this.diskUsage = diskUsage;
		this.sessions.add(session);
		this.lastActivity = lastActivity;
		this.oldSessions = oldSessions;
		this.projects = projects;
		this.accounts = accounts;
		this.plugins = plugins;
		this.configs = configs;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public String getLastActivity() {
		return lastActivity;
	}

	public void setLastActivity(String lastActivity) {
		this.lastActivity = lastActivity;
	}

	public void setDiskUsage(String diskUsage) {
		this.diskUsage = diskUsage;
	}

	public List<Session> getSessions() {
		return sessions;
	}

	public void setSessions(List<Session> sessions) {
		this.sessions = sessions;
	}
	
	public void addSession(Session session){
		this.sessions.add(session);
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
