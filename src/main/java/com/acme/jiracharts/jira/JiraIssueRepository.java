package com.acme.jiracharts.jira;

import java.util.List;

import com.acme.jiracharts.core.domain.issue.IssueRepository;
import com.acme.jiracharts.core.domain.version.Version;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;

public class JiraIssueRepository implements IssueRepository {

	private SearchService searchService;

	public JiraIssueRepository(SearchService searchService) {
		this.searchService = searchService;
	}

	@Override
	public List<Issue> allIssuesForVersion(User user, Version firstUnreleased) {
		try {
			Query query = JqlQueryBuilder.newBuilder().where()
					.fixVersion(firstUnreleased.getId()).buildQuery();
			SearchResults search = searchService.search(user, query,
					PagerFilter.getUnlimitedFilter());
			return search.getIssues();
		} catch (SearchException e) {
			throw new RuntimeException(e);
		}
	}
}
