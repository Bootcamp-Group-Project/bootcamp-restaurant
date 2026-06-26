package com.canteen.demo_canteen.dto;

public class UserRegisterDto {
    private String phone;
    private String password;
    private String role;
    private String specificId;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getSpecificId() { return specificId; }
    public void setSpecificId(String specificId) { this.specificId = specificId; }
}