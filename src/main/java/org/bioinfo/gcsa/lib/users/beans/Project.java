package org.bioinfo.gcsa.lib.users.beans;
import java.util.List;
import java.util.Vector;


public class Project {
	private String id;
	private String name;
	private String status;
	private String diskUsage; 
	private String creationDate; 
	private String ownerId;
	private String type;
	private String descripcion;
	private List<Member> acl;
	private List<Job> jobs;
	private List<Data> data;
	
	public Project(){
		
		jobs = new Vector<Job>();
		jobs.add(new Job());
		
		acl = new Vector<Member>();
		acl.add(new Member());
		acl.add(new Member());
		
		data = new Vector<Data>();
		data.add(new Data());
		
		this.status ="";
		this.id = "";
		this.ownerId = "";
		this.name = "";
		this.diskUsage = "";
		this.creationDate = "";
		this.type = "";
		this.descripcion = "";
	}

	public Project(String id, String name, String status, String diskUsage,
			String creationDate, String ownerId, String type,
			String descripcion, List<Member> acl, List<Job> jobs,
			List<Data> data) {
		super();
		this.id = id;
		this.name = name;
		this.status = status;
		this.diskUsage = diskUsage;
		this.creationDate = creationDate;
		this.ownerId = ownerId;
		this.type = type;
		this.descripcion = descripcion;
		this.acl = acl;
		this.jobs = jobs;
		this.data = data;
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

	public List<Member> getAcl() {
		return acl;
	}

	public void setAcl(List<Member> acl) {
		this.acl = acl;
	}

	public List<Job> getJobs() {
		return jobs;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}

	public List<Data> getData() {
		return data;
	}

	public void setData(List<Data> data) {
		this.data = data;
	}




	
}
