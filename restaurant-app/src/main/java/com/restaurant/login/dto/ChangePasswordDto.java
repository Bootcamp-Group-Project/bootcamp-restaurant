package com.restaurant.login.dto;

public class ChangePasswordDto {
    private String role;
    private String identifier;
    private String newPassword;

    public String getRole()        { return role; }
    public void setRole(String role) { this.role = role; }
    public String getIdentifier()  { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
