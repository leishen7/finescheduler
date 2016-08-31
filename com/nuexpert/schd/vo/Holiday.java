package com.nuexpert.schd.vo;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class Holiday implements Serializable{
	private String holidayCalId;
	private String holidayName;
	private Date date;
	public String getHolidayCalId() {
		return holidayCalId;
	}
	public void setHolidayCalId(String holidayCalId) {
		this.holidayCalId = holidayCalId;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getHolidayName() {
		return holidayName;
	}
	public void setHolidayName(String holidayName) {
		this.holidayName = holidayName;
	}
}
