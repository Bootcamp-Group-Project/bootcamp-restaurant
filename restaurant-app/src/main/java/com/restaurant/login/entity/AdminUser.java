package com.restaurant.login.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "admin_users")
public class AdminUser extends BaseUser {

    @Column(name = "admin_level", unique = true)
    private String adminLevel;

    public String getAdminLevel() { return adminLevel; }
    public void setAdminLevel(String adminLevel) { this.adminLevel = adminLevel; }
}
