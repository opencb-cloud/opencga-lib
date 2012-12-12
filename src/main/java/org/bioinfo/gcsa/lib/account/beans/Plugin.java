package org.bioinfo.gcsa.lib.account.beans;

public class Plugin {
	private String id;
	private String ownerId;
	private String name;

	public Plugin(){
		this.ownerId = "";
		this.id = "";
		this.name = "";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
