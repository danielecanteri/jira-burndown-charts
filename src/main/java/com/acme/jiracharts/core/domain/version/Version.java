package com.acme.jiracharts.core.domain.version;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

public class Version {

	private Long id;
	private String name;
	private Date startDate;
	private Date releaseDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public List<DateTime> workingDays() {
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

}
