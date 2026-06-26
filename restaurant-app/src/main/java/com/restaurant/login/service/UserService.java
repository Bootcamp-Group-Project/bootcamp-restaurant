package com.restaurant.login.service;

import com.restaurant.login.dto.AdminResetPasswordDto;
import com.restaurant.login.dto.ChangePasswordDto;
import com.restaurant.login.dto.LoginResp;
import com.restaurant.login.dto.UserLoginDto;
import com.restaurant.login.dto.UserRegisterDto;

public interface UserService {
    String registerUser(UserRegisterDto dto);
    LoginResp loginUser(UserLoginDto dto);
    LoginResp lookupByPhone(String phone);
    String updatePassword(ChangePasswordDto dto);
    String adminResetPassword(AdminResetPasswordDto dto);
}
