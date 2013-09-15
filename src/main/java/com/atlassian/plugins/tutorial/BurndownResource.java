package com.atlassian.plugins.tutorial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.ProjectService.GetProjectResult;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.query.Query;
import com.atlassian.sal.api.user.UserManager;

/**
 * REST resource that provides a list of projects in JSON format.
 */
@Path("/burndown")
public class BurndownResource {
	private UserManager userManager;
	private PermissionManager permissionManager;
	private UserUtil userUtil;
	private SearchService searchService;
	private ProjectService projectService;
	private VersionService versionService;

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
	public BurndownResource(ProjectService projectService,
			VersionService versionService, SearchService searchService,
			UserManager userManager, UserUtil userUtil,
			PermissionManager permissionManager) {
		this.projectService = projectService;
		this.versionService = versionService;
		this.searchService = searchService;
		System.out.println("===================");
		System.out.println("initialized");
		System.out.println("search service " + searchService);
		System.out.println("project service " + projectService);
		System.out.println("version service " + versionService);
		this.userManager = userManager;
		this.userUtil = userUtil;
		this.permissionManager = permissionManager;
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

		Long projectId = Long.valueOf(projectIdString.substring("project-"
				.length()));

		String username = userManager.getRemoteUsername(request);
		User user = userUtil.getUser(username);
		GetProjectResult projectByKey = projectService.getProjectById(user,
				projectId);
		Project project = projectByKey.getProject();
		Collection<Version> versions = project.getVersions();

		Version firstUnreleased = firstUnreleased(versions);

		VersionBurndown versionBurndown = new VersionBurndown();
		versionBurndown.setProject(ProjectRepresentation.fromProject(project));
		versionBurndown.setVersion(VersionRepresentation
				.fromVersion(firstUnreleased));

		Date startDate = startDate(firstUnreleased, versions);
		Date releaseDate = firstUnreleased.getReleaseDate();

		List<DateTime> dates = calculateVersionWorkingDays(startDate,
				releaseDate);
		System.out.println("dates calculated start " + startDate);
		System.out.println("dates calculated release " + releaseDate);
		System.out.println("dates calculated " + dates);
		versionBurndown.setDates(toString(dates));

		SearchResults search;
		try {
			Query query = JqlQueryBuilder.newBuilder().where()
					.fixVersion(firstUnreleased.getId()).buildQuery();
			search = searchService.search(user, query,
					PagerFilter.getUnlimitedFilter());
			System.out.println("total: " + search.getTotal());
			List<Issue> issues = search.getIssues();

			long totalOriginalEstimates = 0;
			List<User> assignees = new ArrayList<User>();
			for (Issue issue : issues) {
				totalOriginalEstimates += issue.getOriginalEstimate();
				if (!assignees.contains(issue.getAssignee())) {
					assignees.add(issue.getAssignee());
				}
			}
			// List<Burndown> burndowns = new ArrayList<Burndown>();
			// Burndown theVersionBurndown = calculateVersionBurndown(dates,
			// totalOriginalEstimates);
			// burndowns.add(theVersionBurndown);
			//
			// System.out.println(assignees);
			// for (User assignee : assignees) {
			// Burndown theAssigneeBurndown = calculateAssigneeBurndown(dates,
			// issues, assignee);
			// burndowns.add(theAssigneeBurndown);
			// }

			// versionBurndown.setBurndowns(burndowns);

			Collection<Collection<?>> dataTable = new ArrayList<Collection<?>>();
			versionBurndown.setDataTable(dataTable);

			List<String> headers = new ArrayList<String>();
			headers.add("Date");
			headers.add("Version planned");
			headers.add("Versione actual");

			Map<User, TheVersionBurndown> mapUserBurndown = new HashMap<User, TheVersionBurndown>();
			for (User aUser : assignees) {
				headers.add(aUser.getName() + " actual");
				mapUserBurndown.put(aUser, new TheVersionBurndown(dates,
						issuesForUser(issues, aUser)));
			}
			dataTable.add(headers);

			TheVersionBurndown thatVersionBurndown = new TheVersionBurndown(
					dates, issues);

			Map<DateTime, List<Object>> map = new HashMap<DateTime, List<Object>>();
			for (DateTime dateTime : dates) {
				ArrayList<Object> arrayList = new ArrayList<Object>();
				map.put(dateTime, arrayList);
				dataTable.add(arrayList);
				arrayList.add(dateTime.toString("dd/MM/yyyy"));
			}

			for (DateTime dateTime : dates) {
				List<Object> list = map.get(dateTime);

				list.add(thatVersionBurndown.planned(dateTime));
				list.add(thatVersionBurndown.actual(dateTime));
				for (User aUser : assignees) {
					if (mapUserBurndown.get(aUser).actual(dateTime) != null) {
						list.add(mapUserBurndown.get(aUser).actual(dateTime)
								* thatVersionBurndown.planned(dateTime)
								/ mapUserBurndown.get(aUser).planned(dateTime));
					} else {
						list.add(null);
					}
				}
			}

			for (User aUser : assignees) {
				int totalPlanned = 0;
				for (Issue issue : issues) {
					if (issue.getAssignee().equals(aUser)) {
						totalPlanned += issue.getOriginalEstimate();
					}
				}
				int burndownPlanned = totalPlanned;
				int step = totalPlanned / dates.size();
				for (DateTime dateTime : dates) {
					List<Object> list = map.get(dateTime);

					// list.add(thatVersionBurndown.planned(dateTime));
					// list.add(thatVersionBurndown.actual(dateTime));

					// if (list.size() > 4) {
					// list.add(burndownPlanned);
					// } else {
					// list.add(burndownPlanned);
					// }
					// burndownPlanned -= step;
					//
					// if (!dateTime.isAfter(DateTime.now())) {
					// if (list.size() > 5) {
					// list.add(actual(issues, aUser, dateTime)
					// + (Integer) map.get(dates.get(0)).get(
					// list.size() - 1));
					// } else {
					// list.add(actual(issues, aUser, dateTime));
					// }
					// } else {
					// list.add(null);
					// }
				}
			}

		} catch (SearchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Response.ok(versionBurndown).build();
	}

	private List<DateTime> calculateVersionWorkingDays(Date startDate,
			Date releaseDate) {
		List<DateTime> dates = new ArrayList<DateTime>();
		for (DateTime date = new DateTime(startDate); !date
				.isAfter(new DateTime(releaseDate)); date = date.plusDays(1)) {
			if (date.getDayOfWeek() != DateTimeConstants.SATURDAY
					&& date.getDayOfWeek() != DateTimeConstants.SUNDAY) {
				dates.add(date);
			}
		}
		return dates;
	}

	private List<Issue> issuesForUser(List<Issue> issues, User aUser) {
		List<Issue> result = new ArrayList<Issue>();
		for (Issue issue : issues) {
			if (issue.getAssignee().equals(aUser)) {
				result.add(issue);
			}
		}
		return result;
	}

	private int actual(List<Issue> issues, User aUser, DateTime dateTime) {
		int actual = 0;
		for (Issue issue : issues) {
			if (issue.getAssignee().equals(aUser)) {
				if (issue.getResolutionDate() == null
						|| dateTime.plusDays(1).isBefore(
								new DateTime(issue.getResolutionDate())))
					actual += issue.getOriginalEstimate();
			}
		}
		return actual;
	}

	private Burndown calculateAssigneeBurndown(List<DateTime> dates,
			List<Issue> issues, User assignee) {
		long totalEstimates = 0;
		for (Issue issue : issues) {
			if (issue.getAssignee().equals(assignee)) {
				System.out.println("res date: " + issue.getResolutionDate());
				totalEstimates += issue.getOriginalEstimate();
			}
		}
		Burndown burndown = new Burndown();
		List<Integer> result = calculateBurndownUpToNow(dates, totalEstimates);
		burndown.setData(result);
		return burndown;

	}

	private Burndown calculateVersionBurndown(List<DateTime> dates,
			long totalOriginalEstimates) {
		Burndown burndown = new Burndown();
		List<Integer> result = calculateBurndown(dates, totalOriginalEstimates);
		burndown.setData(result);
		return burndown;
	}

	private List<Integer> calculateBurndownUpToNow(List<DateTime> dates,
			long totalOriginalEstimates) {
		long step = totalOriginalEstimates / dates.size();
		List<Integer> result = new ArrayList<Integer>();
		for (DateTime dateTime : dates) {
			if (!dateTime.isAfter(DateTime.now())) {
				result.add((int) totalOriginalEstimates);
				totalOriginalEstimates -= step;
			}
		}
		return result;
	}

	private List<Integer> calculateBurndown(List<DateTime> dates,
			long totalOriginalEstimates) {
		long step = totalOriginalEstimates / dates.size();
		List<Integer> result = new ArrayList<Integer>();
		for (DateTime dateTime : dates) {
			result.add((int) totalOriginalEstimates);
			totalOriginalEstimates -= step;
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

	public static void main(String[] args) {
		System.out.println("ciao");
		DateTime start = new DateTime(new Date());
		Days daysBetween = Days.daysBetween(start, start.plusDays(10));
		System.out.println(daysBetween);

	}

	private Version firstUnreleased(Collection<Version> versions) {
		System.out.println("first unrelease of versions: " + versions);
		System.out.println("first unrelease of versions size: "
				+ versions.size());
		Version result = null;
		for (Version version : versions) {
			if (!version.isArchived() && !version.isReleased()) {
				if (result == null) {
					result = version;
				} else {
					System.out.println("archived: " + version.isArchived());
					System.out.println("released: " + version.isReleased());
					if (startDate(version, versions).before(
							startDate(result, versions))) {
						result = version;
					}
				}
			}
		}
		return result;
	}

	private Date startDate(Version version, Collection<Version> allVersions) {
		try {
			return version.getStartDate();
		} catch (NoSuchMethodError e) {
			Version previousVersion = null;
			for (Version version2 : allVersions) {
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
			return new DateMidnight(previousVersion.getReleaseDate()).plusDays(
					1).toDate();
		}
	}
}