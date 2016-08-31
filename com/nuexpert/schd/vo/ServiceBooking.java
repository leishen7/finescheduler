package com.nuexpert.schd.vo;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class ServiceBooking implements Serializable{
	private Integer serviceBookingId;
	private String serviceId;
	private String userId;
	private String serviceName;
	private Date startTime;
	private Date endTime;
	private String status;
	private String userName;
	private String email;
	private String phone;
	private String phoneProvider;
	private String notes;
	private String provider;
	private String providerId;
	public String getProviderId() {
		return providerId;
	}
	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}
	public String getPhoneProvider() {
		return phoneProvider;
	}
	public void setPhoneProvider(String phoneProvider) {
		this.phoneProvider = phoneProvider;
	}

	
	public String getProvider() {
		return provider;
	}
	public void setProvider(String provider) {
		this.provider = provider;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public Integer getServiceBookingId() {
		return serviceBookingId;
	}
	public void setServiceBookingId(Integer serviceBookingId) {
		this.serviceBookingId = serviceBookingId;
	}
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	@Override
	public String toString() {
		return "ServiceBooking [serviceBookingId=" + serviceBookingId
				+ ", serviceId=" + serviceId + ", userId=" + userId
				+ ", serviceName=" + serviceName + ", startTime=" + startTime
				+ ", endTime=" + endTime + ", status=" + status + "]";
	}
	
}
