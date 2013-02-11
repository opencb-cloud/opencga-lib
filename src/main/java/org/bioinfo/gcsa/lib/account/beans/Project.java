package org.bioinfo.gcsa.lib.account.beans;

import java.util.ArrayList;
import java.util.List;

public class Project {
	private String id;
	private String name;
	private String descripcion;
	private String ownerId;
	private int active;
	private List<Acl> acl;
	private List<Job> jobs = new ArrayList<>();
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public String getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
	public int getActive() {
		return active;
	}
	public void setActive(int active) {
		this.active = active;
	}
	public List<Acl> getAcl() {
		return acl;
	}
	public void setAcl(List<Acl> acl) {
		this.acl = acl;
	}
	public List<Job> getJobs() {
		return jobs;
	}
	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}
}
