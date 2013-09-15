package com.atlassian.plugins.tutorial;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class VersionBurndown {

	@XmlElement
	private VersionRepresentation version;

	@XmlElement
	private ProjectRepresentation project;

	@XmlElement
	private Collection<String> dates;

	@XmlElement
	private Collection<Collection<?>> dataTable;

	public VersionBurndown() {
	}

	public VersionRepresentation getVersion() {
		return version;
	}

	public void setVersion(VersionRepresentation version) {
		this.version = version;
	}

	public Collection<String> getDates() {
		return dates;
	}

	public void setDates(Collection<String> dates) {
		this.dates = dates;
	}

	public Collection<Collection<?>> getDataTable() {
		return dataTable;
	}

	public void setDataTable(Collection<Collection<?>> dataTable) {
		this.dataTable = dataTable;
	}

	public ProjectRepresentation getProject() {
		return project;
	}

	public void setProject(ProjectRepresentation project) {
		this.project = project;
	}

}
