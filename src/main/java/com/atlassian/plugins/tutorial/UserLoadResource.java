package com.atlassian.plugins.tutorial;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;

import com.acme.jiracharts.core.domain.issue.IssueRepository;
import com.acme.jiracharts.core.domain.version.Version;
import com.acme.jiracharts.core.domain.version.VersionRepository;
import com.acme.jiracharts.jira.JiraIssueRepository;
import com.acme.jiracharts.jira.JiraVersionRepository;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.ProjectService.GetProjectResult;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.user.UserManager;

/**
 * REST resource that provides a list of projects in JSON format.
 */
@Path("/userload")
public class UserLoadResource {
	private UserManager userManager;
	private PermissionManager permissionManager;
	private UserUtil userUtil;
	private SearchService searchService;
	private ProjectService projectService;
	private VersionService versionService;
	private IssueRepository issueRepository;
	private VersionRepository versionRepository;

	/**
	 * Constructor.
	 * 
	 * @param userManager
	 *            a SAL object used to find remote usernames in Atlassian
	 *            products
	 * @param userUtil
	 *            a JIRA object to resolve usernames to JIRA's internal
	 *            {@code com.opensymphony.os.User} objects
	 * @param permissionManager
	 *            the JIRA object which manages permissions for users and
	 *            projects
	 */
	public UserLoadResource(ProjectService projectService,
			VersionService versionService, SearchService searchService,
			UserManager userManager, UserUtil userUtil,
			PermissionManager permissionManager) {
		this.projectService = projectService;
		this.versionService = versionService;
		this.searchService = searchService;
		this.userManager = userManager;
		this.userUtil = userUtil;
		this.permissionManager = permissionManager;
		this.issueRepository = new JiraIssueRepository(searchService);
		this.versionRepository = new JiraVersionRepository();
	}

	/**
	 * Returns the list of projects browsable by the user in the specified
	 * request.
	 * 
	 * @param request
	 *            the context-injected {@code HttpServletRequest}
	 * @return a {@code Response} with the marshalled projects
	 */
	@GET
	@AnonymousAllowed
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getBurndown(@Context HttpServletRequest request,
			@QueryParam("projectId") String projectIdString) {
		try {
			Long projectId = Long.valueOf(projectIdString.substring("project-"
					.length()));

			String username = userManager.getRemoteUsername(request);
			User user = userUtil.getUser(username);
			GetProjectResult projectByKey = projectService.getProjectById(user,
					projectId);
			Project project = projectByKey.getProject();

			Version firstUnreleased = versionRepository
					.firstUnreleasedOfProject(project);

			VersionBurndown versionBurndown = new VersionBurndown();
			versionBurndown.setProject(ProjectRepresentation
					.fromProject(project));
			versionBurndown.setVersion(VersionRepresentation
					.fromVersion(firstUnreleased));

			List<DateTime> dates = firstUnreleased.workingDays();
			versionBurndown.setDates(toString(dates));

			List<Issue> issues = issueRepository.allIssuesForVersion(user,
					firstUnreleased);

			List<User> assignees = new ArrayList<User>();
			for (Issue issue : issues) {
				if (issue.getOriginalEstimate() != null
						&& issue.getOriginalEstimate() != 0
						&& !assignees.contains(issue.getAssignee())) {
					assignees.add(issue.getAssignee());
				}
			}

			Collection<Collection<?>> dataTable = new ArrayList<Collection<?>>();
			versionBurndown.setDataTable(dataTable);

			List<String> headers = new ArrayList<String>();
			headers.add("Date");

			Map<User, Burndown> mapUserBurndown = new HashMap<User, Burndown>();
			for (User aUser : assignees) {
				if (aUser != null) {
					headers.add(aUser.getName());
				} else {
					headers.add("Unassigned");
				}
				mapUserBurndown.put(aUser,
						new Burndown(dates, issuesForUser(issues, aUser)));
			}
			dataTable.add(headers);

			Map<DateTime, List<Object>> map = new HashMap<DateTime, List<Object>>();
			for (DateTime dateTime : dates) {
				ArrayList<Object> arrayList = new ArrayList<Object>();
				map.put(dateTime, arrayList);
				dataTable.add(arrayList);
				arrayList.add(dateTime.toString("dd/MM/yyyy"));
			}

			for (DateTime dateTime : dates) {
				List<Object> list = map.get(dateTime);

				for (User aUser : assignees) {
					if (mapUserBurndown.get(aUser).actual(dateTime) != null) {
						list.add(new BigDecimal(mapUserBurndown.get(aUser)
								.actual(dateTime)));
					} else {
						if (dates.get(0).equals(dateTime))
							list.add(BigDecimal.ZERO);
						else
							list.add(null);
					}
				}
			}

			return Response.ok(versionBurndown).build();
		} catch (RuntimeException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);

			return Response.ok(sw.getBuffer()).build();
		}
	}

	private List<Issue> issuesForUser(List<Issue> issues, User aUser) {
		List<Issue> result = new ArrayList<Issue>();
		for (Issue issue : issues) {
			if (issue.getAssignee() == null) {
				if (aUser == null) {
					result.add(issue);
				}
			} else if (issue.getAssignee().equals(aUser)) {
				result.add(issue);
			}
		}
		return result;
	}

	private List<String> toString(List<DateTime> dates) {
		List<String> stringDates = new ArrayList<String>();
		for (DateTime aDate : dates) {
			stringDates.add(aDate.toString("dd/MM/yyyy"));
		}
		return stringDates;
	}

}