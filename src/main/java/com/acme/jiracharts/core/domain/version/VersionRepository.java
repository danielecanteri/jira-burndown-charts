package com.acme.jiracharts.core.domain.version;

import com.atlassian.jira.project.Project;

public interface VersionRepository {

	Version firstUnreleasedOfProject(Project project);

}
