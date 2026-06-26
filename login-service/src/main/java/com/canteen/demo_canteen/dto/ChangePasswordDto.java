package com.canteen.demo_canteen.dto;

public class ChangePasswordDto {
    private String role;        // STUDENT, TEACHER, ADMIN, KITCHEN
    private String identifier;  // STUDENT=s12345678, TEACHER=t12345678, ADMIN=a12345678, KITCHEN=phone
    private String newPassword; // 8 位數字

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
