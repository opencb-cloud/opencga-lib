package org.bioinfo.gcsa.lib.account.beans;

import java.util.ArrayList;
import java.util.List;

import org.bioinfo.gcsa.lib.GcsaUtils;

public class Job {
	private String id;
	private String status;
	private String percentage;
	private String message;
	private String description;
	private String dateTime;
	private String finishTime;
	private String toolName;
	private String name;
	private String commandLine;
	private String visites;
	private String diskUsage;
	private String creationTime;
	private List<String> inputData;
	
	public Job(){
		this.id = "";
		this.status ="";
		this.percentage = "";
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
		this.inputData = new ArrayList<String>();
	}

	public Job(String id, String percentage, String message, String dateTime,
			String finishTime, String toolName, String name, String status,
			String commandLine, String visites, String diskUsage, String description, List<String> inputData) {
		this.id = id;
		this.name = name;
		this.percentage = percentage;
		this.message = message;
		this.dateTime = dateTime;
		this.finishTime = finishTime;
		this.toolName = toolName;
		this.status = status;
		this.commandLine = commandLine;
		this.visites = visites;
		this.diskUsage = diskUsage;
		this.description = description;
		this.creationTime = GcsaUtils.getTime();
		this.inputData = inputData;
	}

	public String getJobId() {
		return id;
	}

	public void setJobId(String id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPercentage() {
		return percentage;
	}

	public void setPercentage(String percentage) {
		this.percentage = percentage;
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

	public List<String> getInputData() {
		return inputData;
	}

	public void setInputData(List<String> inputData) {
		this.inputData = inputData;
	}
}
