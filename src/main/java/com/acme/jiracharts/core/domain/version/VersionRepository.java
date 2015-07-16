package com.acme.jiracharts.core.domain.version;

import java.util.List;

import com.atlassian.jira.project.Project;

public interface VersionRepository {

	Version firstUnreleasedOfProject(Project project);

	List<Version> allUnreleasedVersions(Project project);

	Version byName(Project project, String versionName);

}
