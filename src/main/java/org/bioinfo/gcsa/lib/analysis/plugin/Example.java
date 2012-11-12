package org.bioinfo.gcsa.lib.analysis.plugin;

public class Example {
	private String executionId, file;
	
	public Example(String executionId, String file) {
		this.executionId = executionId;
		this.file = file;
	}

	public String getExecutionId() {
		return executionId;
	}

	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}
}
