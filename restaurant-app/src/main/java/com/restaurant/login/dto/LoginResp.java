package com.restaurant.login.dto;

public class LoginResp {
    private Long userId;
    private String role;
    private String phone;
    private String message;
    private String token;

    public LoginResp(Long userId, String role, String phone, String message) {
        this.userId  = userId;
        this.role    = role;
        this.phone   = phone;
        this.message = message;
    }

    public LoginResp(Long userId, String role, String phone, String message, String token) {
        this(userId, role, phone, message);
        this.token = token;
    }

    public Long   getUserId()  { return userId; }
    public String getRole()    { return role; }
    public String getPhone()   { return phone; }
    public String getMessage() { return message; }
    public String getToken()   { return token; }
}
