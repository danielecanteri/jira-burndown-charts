package com.atlassian.plugins.tutorial;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class VersionRepresentation {

	@XmlElement
	private String name;

	public static VersionRepresentation fromVersion(
			com.acme.jiracharts.core.domain.version.Version firstUnreleased) {
		VersionRepresentation versionRepresentation = new VersionRepresentation();
		versionRepresentation.setName(firstUnreleased.getName());
		return versionRepresentation;
	}

	private void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
