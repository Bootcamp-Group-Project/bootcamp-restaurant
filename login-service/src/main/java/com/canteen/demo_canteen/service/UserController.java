package com.canteen.demo_canteen.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.canteen.demo_canteen.dto.ChangePasswordDto;
import com.canteen.demo_canteen.dto.LoginResp;
import com.canteen.demo_canteen.dto.UserLoginDto;
import com.canteen.demo_canteen.dto.UserRegisterDto;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

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
}
