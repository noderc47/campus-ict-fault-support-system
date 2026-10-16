package com.campusict.model;

/**
 * Represents one record from the technicians table.
 */
public class Technician {

    private Integer techId;
    private String techName;
    private String department;
    private String email;
    private String status;

    /**
     * Creates an empty technician object.
     */
    public Technician() {
    }

    /**
     * Creates a technician object with values from the technicians table.
     *
     * @param techId generated MySQL primary key
     * @param techName technician's name
     * @param department technician's department
     * @param email technician's unique email address
     * @param status Available, Busy, or On Leave
     */
    public Technician(Integer techId, String techName, String department, String email, String status) {
        this.techId = techId;
        this.techName = techName;
        this.department = department;
        this.email = email;
        this.status = status;
    }

    public Integer getTechId() {
        return techId;
    }

    public void setTechId(Integer techId) {
        this.techId = techId;
    }

    public String getTechName() {
        return techName;
    }

    public void setTechName(String techName) {
        this.techName = techName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}