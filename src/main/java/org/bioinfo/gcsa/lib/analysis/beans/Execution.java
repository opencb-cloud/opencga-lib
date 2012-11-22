package org.bioinfo.gcsa.lib.analysis.beans;

import java.util.List;

public class Execution {
	private String id, name, executable, outputParam, testCmd;
	private List<InputParam> inputParams;
	private List<Option> validParams;
	private List<ConfigAttr> configAttr;
	
	public Execution(String id, String name, String executable,
			List<InputParam> inputParams, String outputParam,
			List<Option> validParams, List<ConfigAttr> configAttr, String testCmd) {
		this.id = id;
		this.name = name;
		this.executable = executable;
		this.inputParams = inputParams;
		this.outputParam = outputParam;
		this.validParams = validParams;
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

	public String getOutputParam() {
		return outputParam;
	}

	public void setOutputParam(String outputParam) {
		this.outputParam = outputParam;
	}

	public List<InputParam> getInputParams() {
		return inputParams;
	}

	public void setInputParams(List<InputParam> inputParams) {
		this.inputParams = inputParams;
	}

	public List<Option> getValidParams() {
		return validParams;
	}

	public void setValidParams(List<Option> validParams) {
		this.validParams = validParams;
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
