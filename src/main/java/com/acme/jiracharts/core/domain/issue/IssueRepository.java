package com.acme.jiracharts.core.domain.issue;

import java.util.List;

import com.acme.jiracharts.core.domain.version.Version;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;

public interface IssueRepository {

	List<Issue> allIssuesForVersion(User user, Version firstUnreleased);

}
