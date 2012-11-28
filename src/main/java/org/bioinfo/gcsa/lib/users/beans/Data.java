package org.bioinfo.gcsa.lib.users.beans;
import java.util.ArrayList;
import java.util.List;

import org.bioinfo.gcsa.lib.GcsaUtils;


public class Data {
	private String id;
	private String type;
	private String fileName;
	private String multiple;
	private String diskUsage;
	private String creationTime;
	private String responsible;
	private String organization;
	private String date;
	private String description;
	private String status;
	private String statusMessage;
	private List<Acl> members;
	
	public Data(){
		this.id = "";
		this.type = "";
		this.fileName = "";
		this.multiple = "";
		this.diskUsage = "";
		this.creationTime = GcsaUtils.getTime();
		this.responsible = "";
		this.organization = "";
		this.date = "";
		this.description = "";
		this.status = "";
		this.statusMessage = "";
		this.members = new ArrayList<Acl>();
	}

	public Data(String id, String type, String fileName, String multiple, String diskUsage,
			String responsible, String organization, String date, String description,
			String status, String statusMessage, List<Acl> members) {
		this.id = id;
		this.type = type;
		this.fileName = fileName;
		this.multiple = multiple;
		this.diskUsage = diskUsage;
		this.creationTime = GcsaUtils.getTime();
		this.responsible = responsible;
		this.organization = organization;
		this.date = date;
		this.description = description;
		this.status = status;
		this.statusMessage = statusMessage;
		this.members = members;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getMultiple() {
		return multiple;
	}

	public void setMultiple(String multiple) {
		this.multiple = multiple;
	}

	public String getDiskUsage() {
		return diskUsage;
	}

	public void setDiskUsage(String diskUsage) {
		this.diskUsage = diskUsage;
	}

	public String getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(String creationTime) {
		this.creationTime = creationTime;
	}

	public String getResponsible() {
		return responsible;
	}

	public void setResponsible(String responsible) {
		this.responsible = responsible;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public List<Acl> getMembers() {
		return members;
	}

	public void setMembers(List<Acl> members) {
		this.members = members;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
