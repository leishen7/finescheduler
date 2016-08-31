package com.nuexpert.schd.vo;

import java.io.Serializable;


@SuppressWarnings("serial")
public class Weather implements Serializable{
	private String cityCode;
	private String widget;
	private String city;
	private String url;
	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public String getWidget() {
		return widget;
	}

	public void setWidget(String widget) {
		this.widget = widget;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}

