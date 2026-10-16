package com.campusict.model;

/**
 * Represents one record from the users table.
 */
public class User {

    private Integer userId;
    private String fullName;
    private String email;
    private String phone;
    private String role;

    /**
     * Creates an empty user object.
     */
    public User() {
    }

    /**
     * Creates a user object with values from the users table.
     *
     * @param userId generated MySQL primary key
     * @param fullName reporter's full name
     * @param email reporter's unique email address
     * @param phone optional phone number
     * @param role Student or Staff
     */
    public User(Integer userId, String fullName, String email, String phone, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}