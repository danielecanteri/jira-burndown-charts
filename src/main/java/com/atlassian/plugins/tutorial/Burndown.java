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

	public BigDecimal ideal(DateTime dateTime) {
		Long result = totalPlanned;
		for (DateTime aDate : dates) {
			if (!aDate.isAfter(dateTime)) {
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

	public BigDecimal forecast(DateTime dateTime) {
		if (dateTime.isBefore(DateTime.now().toDateMidnight())) {
			return null;
		}

		if (dateTime.equals(DateTime.now().toDateMidnight())) {
			return new BigDecimal(actual(dateTime));
		} else {
			Long actual = actual(DateTime.now().toDateMidnight().toDateTime());
			if (actual == null) {
				return null;
			}
			Long result = actual * 3600;
			Long currentVelocity = velocity(DateTime.now().toDateMidnight()
					.toDateTime(), result);

			for (DateTime aDate : dates) {
				if (aDate.isAfter(DateTime.now())
						&& (aDate.isBefore(dateTime) || aDate.isEqual(dateTime))) {
					result -= currentVelocity;
				}
			}

			if (result > 0) {
				return new BigDecimal(result).divide(new BigDecimal(3600), 2,
						RoundingMode.HALF_EVEN).setScale(2,
						RoundingMode.HALF_EVEN);
			} else {
				return null;
			}
		}

	}

	private Long velocity(DateTime dateTime, Long result) {
		Long numberOfDays = 0L;
		for (DateTime dateTime2 : dates) {
			if (!dateTime2.isAfter(dateTime)) {
				numberOfDays++;
			}
		}
		return (totalPlanned - result) / numberOfDays;
	}

	public BigDecimal totalPlanned() {
		return new BigDecimal(totalPlanned).divide(new BigDecimal(3600), 2,
				RoundingMode.HALF_EVEN).setScale(2, RoundingMode.HALF_EVEN);
	}
}
