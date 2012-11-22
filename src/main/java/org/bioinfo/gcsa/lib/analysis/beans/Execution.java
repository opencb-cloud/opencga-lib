package org.bioinfo.gcsa.lib.analysis.beans;

import java.util.List;

public class Execution {
	private String id, name, executable, outParam, testCmd;
	private List<String> inputParam;
	private List<Option> validParams;
	private List<String> dataType;
	private List<ConfigAttr> configAttr;
	
	public Execution(String id, String name, String executable,
			List<String> inputParam, String outParam,
			List<Option> validParams, List<String> dataType,
			List<ConfigAttr> configAttr, String testCmd) {
		this.id = id;
		this.name = name;
		this.executable = executable;
		this.inputParam = inputParam;
		this.outParam = outParam;
		this.validParams = validParams;
		this.dataType = dataType;
		this.configAttr = configAttr;
		this.testCmd = testCmd;
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

	public List<Option> getValidParams() {
		return validParams;
	}

	public void setValidParams(List<Option> validParams) {
		this.validParams = validParams;
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
	
	public String getTestCmd() {
		return testCmd;
	}
	
	public void setTestCmd(String testCmd) {
		this.testCmd = testCmd;
	}
}
