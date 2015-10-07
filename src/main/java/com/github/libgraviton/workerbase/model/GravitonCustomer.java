package com.github.libgraviton.workerbase.model;

public class GravitonCustomer {
    
    private String id;
    
    private String recordOrigin;
    
    private String customerNumber;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getRecordOrigin() {
        return recordOrigin;
    }
    
    public void setRecordOrigin(String recordOrigin) {
         this.recordOrigin = recordOrigin;
    }
    
    public boolean isCoreCustomer() {
        boolean retVal = false;
        if (!this.recordOrigin.isEmpty() && this.recordOrigin.equals("core"))
            retVal = true;
        return retVal;
    }
    
    public void setCustomerNumber(String customerNumber) {
		this.customerNumber = customerNumber;
	}
	
	public String getCustomerNumber() {
		return this.customerNumber;
	}
}
