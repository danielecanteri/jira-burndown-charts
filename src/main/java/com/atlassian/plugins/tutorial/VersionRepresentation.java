package com.atlassian.plugins.tutorial;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.jira.project.version.Version;

@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class VersionRepresentation {

	@XmlElement
	private String name;

	public static VersionRepresentation fromVersion(Version version) {
		VersionRepresentation versionRepresentation = new VersionRepresentation();
		versionRepresentation.setName(version.getName());
		return versionRepresentation;
	}

	private void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
