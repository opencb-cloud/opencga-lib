package org.bioinfo.gcsa.lib.account.beans;

import java.util.ArrayList;
import java.util.List;

import org.bioinfo.gcsa.lib.GcsaUtils;

public class Bucket {
	private String id;
	private String name;
	private String status;
	private String diskUsage; 
	private String creationDate; 
	private String ownerId;
	private String type;
	private String descripcion;
	private List<Acl> acl;
	private List<ObjectItem> objects;
	
	public Bucket(){
		this.status ="1";
		this.id = "";
		this.ownerId = ""; //Este id nos lo otorga mongo cuando hacemos la inserccion
		this.name = "Default";
		this.diskUsage = "";
		this.creationDate = GcsaUtils.getTime();
		this.type = "";
		this.descripcion = "Default Project";
		this.objects = new ArrayList<ObjectItem>();
	}
	
	public Bucket(String nameProject){
		this.status ="1";
		this.id = "";//Esto hay que ver como lo numeramos
		this.ownerId = ""; //Este id nos lo otorga mongo cuando hacemos la inserccion
		this.name = nameProject;
		this.diskUsage = "";
		this.creationDate = GcsaUtils.getTime();
		this.type = "";
		this.descripcion = "";
		this.objects = new ArrayList<ObjectItem>();
	}

	public Bucket(String id, String name, String status, String diskUsage
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
		this.objects = new ArrayList<ObjectItem>();
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

	public List<ObjectItem> getData() {
		return objects;
	}

	public void setData(List<ObjectItem> objectItemList) {
		this.objects = objectItemList;
	}
	
	
}
