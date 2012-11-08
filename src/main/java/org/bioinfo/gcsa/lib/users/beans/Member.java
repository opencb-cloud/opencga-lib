package org.bioinfo.gcsa.lib.users.beans;

public class Member {
	private String accountId;
	private String status;
	private boolean write;
	private boolean read;
	private boolean execute;
	
	public Member(){
		this.status ="";
		this.accountId = "";
		this.write = false;
		this.read = false;
		this.execute = false;
	}

	public Member(String accountId, String status, boolean write, boolean read,
			boolean execute) {
		super();
		this.accountId = accountId;
		this.status = status;
		this.write = write;
		this.read = read;
		this.execute = execute;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isWrite() {
		return write;
	}

	public void setWrite(boolean write) {
		this.write = write;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isExecute() {
		return execute;
	}

	public void setExecute(boolean execute) {
		this.execute = execute;
	}



}
