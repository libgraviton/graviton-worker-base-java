package com.github.libgraviton.workerbase.model;


public class GravitonConsultation {

	private GravitonCustomer customer;
	
	private String date;
	
	private String batchName;
	
	private String creationDate;
	
	public GravitonCustomer getCustomer() {
		return this.customer;
	}
	
	public void setCustomer(GravitonCustomer customer) {
		this.customer = customer;
	}
	
	public String getCreationDate() {
		return this.creationDate;
	}
	
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}
	
}
