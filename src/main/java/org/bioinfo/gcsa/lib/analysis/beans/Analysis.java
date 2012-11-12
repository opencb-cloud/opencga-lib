package org.bioinfo.gcsa.lib.analysis.beans;

import java.util.List;

public class Analysis {
	
	private Author author;
	private String version, name, description, icon;
	private List<Execution> executions;
	private List<Example> examples;
	private List<Acl> acl;
	
	public Analysis(Author author, String version, String name, String description, String icon, List<Execution> executions, List<Example> examples, List<Acl> acl) {
		this.author = author;
		this.version = version;
		this.name = name;
		this.description = description;
		this.icon = icon;
		this.executions = executions;
		this.examples = examples;
		this.acl = acl;
	}

	public Author getAuthor() {
		return author;
	}

	public void setAuthor(Author author) {
		this.author = author;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}

	public List<Execution> getExecutions() {
		return executions;
	}

	public void setExecutions(List<Execution> executions) {
		this.executions = executions;
	}

	public List<Example> getExamples() {
		return examples;
	}

	public void setExamples(List<Example> examples) {
		this.examples = examples;
	}

	public List<Acl> getAcl() {
		return acl;
	}

	public void setAcl(List<Acl> acl) {
		this.acl = acl;
	}
}
