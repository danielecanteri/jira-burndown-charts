package com.acme.jiracharts.jira;

import java.util.Collection;
import java.util.Date;

import org.joda.time.DateMidnight;

import com.acme.jiracharts.core.domain.version.Version;
import com.acme.jiracharts.core.domain.version.VersionRepository;
import com.atlassian.jira.project.Project;

public class JiraVersionRepository implements VersionRepository {

	@Override
	public Version firstUnreleasedOfProject(Project project) {
		Collection<com.atlassian.jira.project.version.Version> versions = project
				.getVersions();
		return firstUnreleased(versions);
	}

	private Version firstUnreleased(
			Collection<com.atlassian.jira.project.version.Version> versions) {
		com.atlassian.jira.project.version.Version result = null;
		for (com.atlassian.jira.project.version.Version version : versions) {
			if (version.getReleaseDate() != null) {
				if (!version.isArchived() && !version.isReleased()) {
					if (result == null) {
						result = version;
					} else {
						if (startDate(version, versions).before(
								startDate(result, versions))) {
							result = version;
						}
					}
				}
			}
		}
		return toVersion(result, versions);
	}

	private Version toVersion(
			com.atlassian.jira.project.version.Version jiraVersion,
			Collection<com.atlassian.jira.project.version.Version> allVersions) {
		Version version = new Version();
		version.setId(jiraVersion.getId());
		version.setName(jiraVersion.getName());
		version.setStartDate(startDate(jiraVersion, allVersions));
		version.setReleaseDate(jiraVersion.getReleaseDate());
		return version;
	}

	private Date startDate(com.atlassian.jira.project.version.Version version,
			Collection<com.atlassian.jira.project.version.Version> allVersions) {
		try {
			return version.getStartDate();
		} catch (NoSuchMethodError e) {
			com.atlassian.jira.project.version.Version previousVersion = null;
			for (com.atlassian.jira.project.version.Version version2 : allVersions) {
				if (version2.getReleaseDate() != null) {
					if (previousVersion == null) {
						previousVersion = version2;
					} else {
						if (version2.getReleaseDate().before(
								version.getReleaseDate())
								&& version2.getReleaseDate().after(
										previousVersion.getReleaseDate())) {
							previousVersion = version2;
						}
					}
				}
			}
			return new DateMidnight(previousVersion.getReleaseDate()).plusDays(
					1).toDate();
		}
	}

}
