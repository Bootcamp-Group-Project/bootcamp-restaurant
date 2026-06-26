package com.canteen.demo_canteen.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.canteen.demo_canteen.dto.LoginResp;
import com.canteen.demo_canteen.dto.UserLoginDto;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResp> login(@RequestBody UserLoginDto dto) {
        return ResponseEntity.ok(userService.loginUser(dto));
    }
}
