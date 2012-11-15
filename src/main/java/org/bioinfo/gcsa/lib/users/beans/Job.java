package org.bioinfo.gcsa.lib.users.beans;

import org.bioinfo.gcsa.lib.GcsaUtils;

public class Job {
	private String proyectId;
	private String status;
	private String percetage;
	private String message;
	private String dateTime;
	private String finishTime;
	private String toolName;
	private String name;
	private String commandLine;
	private String visites;
	private String diskUsage;
	private String creationTime;
	private String description;
	
	public Job(){
		this.status ="";
		this.proyectId = "";
		this.percetage = "";
		this.message = "";
		this.description = "";
		this.dateTime = "";
		this.finishTime = "";
		this.toolName = "";
		this.name = "";
		this.commandLine = "";
		this.visites = "";
		this.diskUsage = "";
		this.creationTime = GcsaUtils.getTime();
	}



	public Job(String id, String percetage, String message, String dateTime,
			String finishTime, String toolName, String name,
			String commandLine, String visites, String diskUsage, String description) {
		this.proyectId = id;
		this.percetage = percetage;
		this.message = message;
		this.dateTime = dateTime;
		this.finishTime = finishTime;
		this.toolName = toolName;
		this.name = name;
		this.commandLine = commandLine;
		this.visites = visites;
		this.diskUsage = diskUsage;
		this.creationTime = GcsaUtils.getTime();
		this.description = description;
	}



	public String getProyectId() {
		return proyectId;
	}



	public void setProyectId(String proyectId) {
		this.proyectId = proyectId;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}



	public String getPercetage() {
		return percetage;
	}



	public void setPercetage(String percetage) {
		this.percetage = percetage;
	}



	public String getMessage() {
		return message;
	}



	public void setMessage(String message) {
		this.message = message;
	}



	public String getDateTime() {
		return dateTime;
	}



	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}



	public String getFinishTime() {
		return finishTime;
	}



	public void setFinishTime(String finishTime) {
		this.finishTime = finishTime;
	}



	public String getToolName() {
		return toolName;
	}



	public void setToolName(String toolName) {
		this.toolName = toolName;
	}



	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public String getCommandLine() {
		return commandLine;
	}



	public void setCommandLine(String commandLine) {
		this.commandLine = commandLine;
	}



	public String getVisites() {
		return visites;
	}



	public void setVisites(String visites) {
		this.visites = visites;
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



	public String getDescription() {
		return description;
	}



	public void setDescription(String description) {
		this.description = description;
	}

	
	
}
