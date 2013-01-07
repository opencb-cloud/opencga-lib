package org.bioinfo.gcsa.lib.account.beans;

import java.util.ArrayList;
import java.util.List;

import org.bioinfo.gcsa.lib.GcsaUtils;

public class Job {
	
	private String id;
	private String name;
	private String bucketId;
	private String toolName;
	private long diskUsage;
	private String status;
	private String date;
	private String startTime;
	private String endTime;
	private String ouputError;
	private int visites;
	private String commandLine;
	private String description;
	private List<String> inputData;
	private List<String> outputData;
	
	public static final String QUEUED = "queued";
	public static final String RUNNING = "running";
	public static final String DONE = "done";
	

	public Job(String id, String name, String bucketId, String toolName, String status, String commandLine, String description, List<String> inputData) {
		this(id, name, "", toolName, 0, status, GcsaUtils.getTime(), "", "", "", -2, commandLine, description, inputData, new ArrayList<String>());
	}
	
	public Job(String id, String name, String bucketId, String toolName, long diskUsage, String status, String date, String startTime,
			String endTime, String ouputError, int visites, String commandLine, String description, List<String> inputData, List<String> outputData) {
		this.id = id;
		this.name = name;
		this.bucketId = bucketId;
		this.toolName = toolName;
		this.diskUsage = diskUsage;
		this.status = status;
		this.date = date;
		this.startTime = startTime;
		this.endTime = endTime;
		this.ouputError = ouputError;
		this.visites = visites;
		this.commandLine = commandLine;
		this.description = description;
		this.inputData = inputData;
		this.outputData = outputData;
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

	public String getBucketId() {
		return bucketId;
	}

	public void setBucketId(String bucketId) {
		this.bucketId = bucketId;
	}

	public String getToolName() {
		return toolName;
	}

	public void setToolName(String toolName) {
		this.toolName = toolName;
	}

	public long getDiskUsage() {
		return diskUsage;
	}

	public void setDiskUsage(long diskUsage) {
		this.diskUsage = diskUsage;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getOuputError() {
		return ouputError;
	}

	public void setOuputError(String ouputError) {
		this.ouputError = ouputError;
	}

	public int getVisites() {
		return visites;
	}

	public void setVisites(int visites) {
		this.visites = visites;
	}

	public String getCommandLine() {
		return commandLine;
	}

	public void setCommandLine(String commandLine) {
		this.commandLine = commandLine;
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

	public List<String> getOutputData() {
		return outputData;
	}

	public void setOutputData(List<String> outputData) {
		this.outputData = outputData;
	}

}
