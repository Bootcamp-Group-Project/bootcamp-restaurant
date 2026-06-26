package com.restaurant.login.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student extends BaseUser {

    @Column(name = "student_id", unique = true)
    private String studentId;

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
}
