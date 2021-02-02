package com.github.libgraviton.workerbase.gdk.data;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;
import java.util.List;

/**
 * Just a complex class used for serialization tests.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComplexClass implements GravitonBase {

    private String id;

    private String name;

    private Date date;

    private ComplexClass aClass;

    private List<ComplexClass> classes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ComplexClass getaClass() {
        return aClass;
    }

    public void setaClass(ComplexClass aClass) {
        this.aClass = aClass;
    }

    public List<ComplexClass> getClasses() {
        return classes;
    }

    public void setClasses(List<ComplexClass> classes) {
        this.classes = classes;
    }
}
