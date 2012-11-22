package org.bioinfo.gcsa.lib.users.beans;

import java.util.Map;

import org.bioinfo.gcsa.lib.GcsaUtils;


public class Session {
	private String id;
	private String ip;
	private String login;
	private String logout;
	private Map<String, String> attributes;

	public Session() {
		this.id = GcsaUtils.getSessionId();
		this.ip = "";
		this.login = GcsaUtils.getTime();
		this.logout = "";
	}
	
	public Session(String ip){

		this.id = GcsaUtils.getSessionId();
		this.ip = ip;
		this.login = GcsaUtils.getTime();
		this.logout = "";
	}
	
	public Session(String id, String ip, String logout) {
		this.id = id;
		this.ip = ip;
		this.login = GcsaUtils.getTime();
		this.logout = logout;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getLogout() {
		return logout;
	}

	public void setLogout(String logout) {
		this.logout = logout;
	}
	
}
