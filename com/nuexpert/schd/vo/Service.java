package com.nuexpert.schd.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@SuppressWarnings("serial")
public class Service implements Serializable{
	private String serviceId;
	private String serviceName;
	private String provider;
	private String startTime;
	private String endTime;
	
	private String description;
	private String category;
	private int minTime;
	private int incrementTime;
	private int maxTime;
	private int bufferBetweenBookings;
	private float unitPrice;
	private float discount;
	private String currency;
	private boolean needApproval=true;
	private Integer[] noServiceDay;
	private String promotion;
	
	private User providerUser;
	private String language;

	

	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public User getProviderUser() {
		return providerUser;
	}
	public void setProviderUser(User providerUser) {
		this.providerUser = providerUser;
	}
	public String getPromotion() {
		return promotion;
	}
	public void setPromotion(String promotion) {
		this.promotion = promotion;
	}
	public Integer[] getNoServiceDay() {
		return noServiceDay;
	}
	public void setNoServiceDay(Integer[] noServiceDay) {
		this.noServiceDay = noServiceDay;
	}
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		if(serviceId.length()>11)
			this.serviceId = serviceId.substring(6);
		else
			this.serviceId = serviceId;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getProvider() {
		return provider;
	}
	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startDate) {
		this.startTime = startDate;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endDate) {
		this.endTime = endDate;
	}
	@Override
	public String toString() {
		return "Service [serviceId=" + serviceId + ", serviceName="
				+ serviceName + ", provider=" + provider + "]";
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String catergory) {
		this.category = catergory;
	}
	public int getMinTime() {
		return minTime;
	}
	public void setMinTime(int minTime) {
		this.minTime = minTime;
	}
	public int getIncrementTime() {
		return incrementTime;
	}
	public void setIncrementTime(int incrementTime) {
		this.incrementTime = incrementTime;
	}
	public int getMaxTime() {
		return maxTime;
	}
	public void setMaxTime(int maxTime) {
		this.maxTime = maxTime;
	}
	public int getBufferBetweenBookings() {
		return bufferBetweenBookings;
	}
	public void setBufferBetweenBookings(int bufferBetweenBookings) {
		this.bufferBetweenBookings = bufferBetweenBookings;
	}
	public float getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(float unitPrice) {
		this.unitPrice = unitPrice;
	}
	public float getDiscount() {
		return discount;
	}
	public void setDiscount(float discount) {
		this.discount = discount;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public boolean getNeedApproval() {
		return needApproval;
	}
	public void setNeedApproval(boolean needApproval) {
		this.needApproval = needApproval;
	}
}
