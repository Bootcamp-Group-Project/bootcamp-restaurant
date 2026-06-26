package com.restaurant.login.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "kitchen_users")
public class KitchenUser extends BaseUser {

    @Column(name = "vendor_name")
    private String vendorName;

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }
}
