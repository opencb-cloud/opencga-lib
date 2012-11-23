package org.bioinfo.gcsa.lib.users.beans;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bioinfo.gcsa.lib.GcsaUtils;


public class Project {
	private String id;
	private String name;
	private String status;
	private String diskUsage; 
	private String creationDate; 
	private String ownerId;
	private String type;
	private String descripcion;
	private List<Acl> acl;
	private List<Job> jobs = new ArrayList<Job>();
	
	
	public Project(){
		this.status ="1";
		this.id = "";//Esto hay que ver como lo numeramos
		this.ownerId = ""; //Este id nos lo otorga mongo cuando hacemos la inserccion
		this.name = "Default";
		this.diskUsage = "";
		this.creationDate = GcsaUtils.getTime();
		this.type = "";
		this.descripcion = "Default Project";
//		this.jobs.add(new Job());
	}

	public Project(String id, String name, String status, String diskUsage
			, String ownerId, String type, String descripcion, List<Acl> acl, List<Job> jobs) {
		this.id = id;
		this.name = name;
		this.status = status;
		this.diskUsage = diskUsage;
		this.creationDate = GcsaUtils.getTime();
		this.ownerId = ownerId;
		this.type = type;
		this.descripcion = descripcion;
		this.acl = acl;
		this.jobs = jobs;
	}

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDiskUsage() {
		return diskUsage;
	}

	public void setDiskUsage(String diskUsage) {
		this.diskUsage = diskUsage;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
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
