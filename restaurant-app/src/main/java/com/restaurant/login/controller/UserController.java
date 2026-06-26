package com.restaurant.login.controller;

import com.restaurant.login.dto.AdminResetPasswordDto;
import com.restaurant.login.dto.ChangePasswordDto;
import com.restaurant.login.dto.LoginResp;
import com.restaurant.login.dto.UserLoginDto;
import com.restaurant.login.dto.UserRegisterDto;
import com.restaurant.login.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRegisterDto dto) {
        return ResponseEntity.ok(userService.registerUser(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResp> login(@RequestBody UserLoginDto dto) {
        return ResponseEntity.ok(userService.loginUser(dto));
    }

    @GetMapping("/lookup")
    public ResponseEntity<LoginResp> lookupByPhone(@RequestParam String phone) {
        return ResponseEntity.ok(userService.lookupByPhone(phone));
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordDto dto) {
        return ResponseEntity.ok(userService.updatePassword(dto));
    }

    @PutMapping("/admin/reset-password")
    public ResponseEntity<String> adminResetPassword(@RequestBody AdminResetPasswordDto dto) {
        return ResponseEntity.ok(userService.adminResetPassword(dto));
    }
}
