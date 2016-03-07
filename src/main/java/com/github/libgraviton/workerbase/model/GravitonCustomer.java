package com.github.libgraviton.workerbase.model;

/**
 * <p>GravitonCustomer class.</p>
 *
 * @author Dario Nuevo
 * @version $Id: $Id
 */
public class GravitonCustomer {
    
    private String id;
    
    private String recordOrigin;

    private Integer customerNumber;
    
    /**
     * <p>Getter for the field <code>id</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getId() {
        return id;
    }
    
    /**
     * <p>Setter for the field <code>id</code>.</p>
     *
     * @param id a {@link java.lang.String} object.
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * <p>Getter for the field <code>recordOrigin</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRecordOrigin() {
        return recordOrigin;
    }

    /**
     * <p>Setter for the field <code>recordOrigin</code>.</p>
     *
     * @param recordOrigin a {@link java.lang.String} object.
     */
    public void setRecordOrigin(String recordOrigin) {
        this.recordOrigin = recordOrigin;
    }

    /**
     * <p>Getter for the field <code>customerNumber</code>.</p>
     *
     * @return a {@link java.lang.Integer} object.
     * @since 0.9.0
     */
    public Integer getCustomerNumber() {
        return customerNumber;
    }

    /**
     * <p>Setter for the field <code>customerNumber</code>.</p>
     *
     * @param customerNumber a {@link java.lang.Integer} object.
     * @since 0.9.0
     */
    public void setCustomerNumber(Integer customerNumber) {
        this.customerNumber = customerNumber;
    }

    /**
     * <p>isCoreCustomer.</p>
     *
     * @return a boolean.
     */
    public boolean isCoreCustomer() {
        return RecordOrigin.CORE.equals(recordOrigin);
    }
}
