package com.nuexpert.schd.vo;

import java.util.Date;

import org.primefaces.model.DefaultScheduleEvent;

import com.nuexpert.schd.util.WebUtil;

@SuppressWarnings("serial")
public class Appointment extends DefaultScheduleEvent {
	private ServiceBooking serviceBooking=null;
	private int serviceTime;
	
	public int getServiceTime() {
		return serviceTime;
	}

	public void setServiceTime(int serviceTime) {
		this.serviceTime = serviceTime;
	}
	
	private String notes;

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Appointment(){
		super();
	}
	
	public Appointment(String title, Date start, Date end, boolean allDay){
		super(title, start, end, allDay);
		serviceTime=WebUtil.minuteBetween(start, end);
	}
	
	
	public Appointment(ServiceBooking serviceBooking, String title, Date start, Date end){
		super(title, start, end);
		
		this.serviceBooking = serviceBooking;
		serviceTime=WebUtil.minuteBetween(start, end);
	}

	public ServiceBooking getServiceBooking() {
		return serviceBooking;
	}

	public void setServiceBooking(ServiceBooking serviceBooking) {
		this.serviceBooking = serviceBooking;
	}
}
