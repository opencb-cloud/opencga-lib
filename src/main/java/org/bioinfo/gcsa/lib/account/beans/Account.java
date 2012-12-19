package org.bioinfo.gcsa.lib.account.beans;

import java.util.ArrayList;
import java.util.List;

public class Account {
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
	private List<Bucket> buckets = new ArrayList<Bucket>();
	private List<Credential> credentials = new ArrayList<Credential>();
	private List<Plugin> plugins = new ArrayList<Plugin>();
	private List<Config> configs = new ArrayList<Config>();
	private List<Job> jobs;

	public Account() {
		this.status = "1";
		this.password = "";
		this.email = "";
		this.lastActivity = "";
		this.mailingList = "";
		this.accountId = "";
		this.accountName = "";
		this.diskQuota = "";
		this.diskUsage = "";
		this.sessions.add(new Session());
		this.buckets.add(new Bucket());
		this.setJobs(new ArrayList<Job>());
	}

	public Account(String accountId, String accountName, String password, String email) {
		this.accountId = accountId;
		this.accountName = accountName;
		this.email = email;
		this.password = password;
		this.status = "1";
		this.mailingList = "";
		this.lastActivity = "";
		this.diskQuota = "";
		this.diskUsage = "";
		this.buckets.add(new Bucket());
		this.setJobs(new ArrayList<Job>());
	}

	public Account(String accountId, String accountName, String email,
			String password, String status, String mailingList,
			String diskQuota, String diskUsage, Session session,
			List<Session> oldSessions, List<Bucket> buckets,String lastActivity,
			List<Credential> accounts, List<Plugin> plugins, List<Config> configs) {
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
		this.buckets = buckets;
		this.credentials = accounts;
		this.plugins = plugins;
		this.configs = configs;
		this.setJobs(new ArrayList<Job>());
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

	public List<Bucket> getBuckets() {
		return buckets;
	}

	public void setBuckets(List<Bucket> projects) {
		this.buckets = projects;
	}

	public List<Credential> getAccounts() {
		return credentials;
	}

	public void setAccounts(List<Credential> accounts) {
		this.credentials = accounts;
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

	public List<Job> getJobs() {
		return jobs;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}

}
