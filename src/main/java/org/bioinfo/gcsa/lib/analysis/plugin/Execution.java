package org.bioinfo.gcsa.lib.analysis.plugin;

import java.util.List;

public class Execution {
	private String id, name, executable, outParam;
	private List<String> inputParam;
	private List<Option> options;
	private List<String> dataType;
	private List<ConfigAttr> configAttr;
	
	public Execution(String id, String name, String executable,
			List<Option> options, List<String> dataType,
			List<ConfigAttr> configAttr) {
		this.id = id;
		this.name = name;
		this.executable = executable;
		this.options = options;
		this.dataType = dataType;
		this.configAttr = configAttr;
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

	public String getExecutable() {
		return executable;
	}

	public void setExecutable(String executable) {
		this.executable = executable;
	}

	public String getOutParam() {
		return outParam;
	}

	public void setOutParam(String outParam) {
		this.outParam = outParam;
	}

	public List<String> getInputParam() {
		return inputParam;
	}

	public void setInputParam(List<String> inputParam) {
		this.inputParam = inputParam;
	}

	public List<Option> getOptions() {
		return options;
	}

	public void setOptions(List<Option> options) {
		this.options = options;
	}

	public List<String> getDataType() {
		return dataType;
	}

	public void setDataType(List<String> dataType) {
		this.dataType = dataType;
	}

	public List<ConfigAttr> getConfigAttr() {
		return configAttr;
	}

	public void setConfigAttr(List<ConfigAttr> configAttr) {
		this.configAttr = configAttr;
	}
}
