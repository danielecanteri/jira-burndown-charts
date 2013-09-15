package com.atlassian.plugins.tutorial;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class Burndown {

	@XmlElement
	private Collection<Integer> data;

	@XmlElement
	private String fillColor = "rgba(220,220,220,0.5)";
	@XmlElement
	private String strokeColor = "rgba(220,220,220,1)";
	@XmlElement
	private String pointColor = "rgba(220,220,220,1)";
	@XmlElement
	private String pointStrokeColor = "#fff";

	public Collection<Integer> getData() {
		return data;
	}

	public void setData(Collection<Integer> data) {
		this.data = data;
	}

	public String getFillColor() {
		return fillColor;
	}

	public void setFillColor(String fillColor) {
		this.fillColor = fillColor;
	}

	public String getStrokeColor() {
		return strokeColor;
	}

	public void setStrokeColor(String strokeColor) {
		this.strokeColor = strokeColor;
	}

	public String getPointColor() {
		return pointColor;
	}

	public void setPointColor(String pointColor) {
		this.pointColor = pointColor;
	}

	public String getPointStrokeColor() {
		return pointStrokeColor;
	}

	public void setPointStrokeColor(String pointStrokeColor) {
		this.pointStrokeColor = pointStrokeColor;
	}

}
