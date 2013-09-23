package com.atlassian.plugins.tutorial;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import com.atlassian.jira.issue.Issue;

public class Burndown {

	private Map<DateTime, Long> mapResolutionDates = new HashMap<DateTime, Long>();
	private List<DateTime> dates;
	private Long totalPlanned = 0L;

	public Burndown(List<DateTime> dates, List<Issue> issues) {
		this.dates = dates;
		for (DateTime aDate : dates) {
			if (!aDate.isAfter(DateTime.now())) {
				// TODO verificare inizializzazione, non serve
				mapResolutionDates.put(aDate, 0L);
			}
		}
		for (Issue issue : issues) {
			add(issue);
		}
	}

	public void add(Issue issue) {
		try {
			if (issue.getOriginalEstimate() != null) {
				totalPlanned += issue.getOriginalEstimate();
				if (issue.getResolutionDate() != null) {
					DateTime dateTime = new DateMidnight(
							issue.getResolutionDate()).toDateTime();

					if (mapResolutionDates.get(dateTime) == null) {
						mapResolutionDates.put(dateTime, 0L);
					}

					mapResolutionDates.put(
							dateTime,
							mapResolutionDates.get(dateTime)
									+ issue.getOriginalEstimate());
				}
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	public BigDecimal planned(DateTime dateTime) {
		Long result = totalPlanned;
		for (DateTime aDate : dates) {
			if (aDate.isBefore(dateTime)) {
				result -= totalPlanned / dates.size();
			}
		}
		return new BigDecimal(result).divide(new BigDecimal(3600), 2,
				RoundingMode.HALF_EVEN).setScale(2, RoundingMode.HALF_EVEN);
	}

	public Long actual(DateTime dateTime) {
		if (!mapResolutionDates.containsKey(dateTime)) {
			return null;
		}

		Long result = totalPlanned;
		for (DateTime aDate : dates) {
			if (!aDate.isAfter(dateTime)
					&& mapResolutionDates.containsKey(aDate)) {
				result -= mapResolutionDates.get(aDate);
			}
		}
		return result / 3600;
	}
}
