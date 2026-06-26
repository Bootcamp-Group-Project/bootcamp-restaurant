package com.canteen.demo_canteen.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "teachers")
public class Teacher extends BaseUser {
    @Column(name = "employee_id", unique = true)
    private String employeeId;

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
}