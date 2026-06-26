package com.restaurant.login.dto;

public class AdminResetPasswordDto {
    private String adminPhone;
    private String adminPassword;
    private String targetPhone;
    private String newPassword;

    public String getAdminPhone()    { return adminPhone; }
    public void setAdminPhone(String adminPhone) { this.adminPhone = adminPhone; }
    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
    public String getTargetPhone()   { return targetPhone; }
    public void setTargetPhone(String targetPhone) { this.targetPhone = targetPhone; }
    public String getNewPassword()   { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
