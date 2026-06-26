package com.restaurant.login.service.impl;

import com.restaurant.login.dto.AdminResetPasswordDto;
import com.restaurant.login.dto.ChangePasswordDto;
import com.restaurant.login.dto.LoginResp;
import com.restaurant.login.dto.UserLoginDto;
import com.restaurant.login.dto.UserRegisterDto;
import com.restaurant.login.entity.*;
import com.restaurant.login.repository.BaseUserRepository;
import com.restaurant.login.service.UserService;
import com.restaurant.login.util.JwtUtil;
import com.restaurant.shared.exception.BadRequestException;
import com.restaurant.shared.exception.ConflictException;
import com.restaurant.shared.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String ADMIN_REGISTER_ACCESS_RIGHT = "a12345678";

    private final BaseUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeValue(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isEightDigits(String value) {
        return value != null && value.matches("\\d{8}");
    }

    private boolean isStudentId(String value) {
        return value != null && value.matches("s\\d{8}");
    }

    private boolean isTeacherId(String value) {
        return value != null && value.matches("t\\d{8}");
    }

    private boolean isAdminId(String value) {
        return value != null && value.matches("a\\d{8}");
    }

    @Override
    public String registerUser(UserRegisterDto dto) {
        String phone      = normalizeValue(dto.getPhone());
        String password   = normalizeValue(dto.getPassword());
        String role       = normalizeRole(dto.getRole());
        String specificId = normalizeValue(dto.getSpecificId());

        if (!isEightDigits(phone))    throw new BadRequestException("電話號碼必須為 8 位數字");
        if (!isEightDigits(password)) throw new BadRequestException("密碼必須為 8 位數字");
        if (userRepository.findByPhone(phone) != null) throw new ConflictException("此電話號碼已經註冊");

        BaseUser user;
        switch (role) {
            case "STUDENT": {
                if (!isStudentId(specificId)) throw new BadRequestException("學生編號格式必須為 s12345678");
                Student student = new Student();
                student.setStudentId(specificId);
                user = student;
                break;
            }
            case "TEACHER": {
                if (!isTeacherId(specificId)) throw new BadRequestException("教師編號格式必須為 t12345678");
                Teacher teacher = new Teacher();
                teacher.setEmployeeId(specificId);
                user = teacher;
                break;
            }
            case "ADMIN": {
                if (!ADMIN_REGISTER_ACCESS_RIGHT.equals(specificId))
                    throw new BadRequestException("Admin Access Right 必須填 a12345678 才可以註冊");
                AdminUser admin = new AdminUser();
                admin.setAdminLevel(specificId);
                user = admin;
                break;
            }
            case "KITCHEN": {
                if (specificId.isBlank()) throw new BadRequestException("Kitchen Name 不可以留空");
                KitchenUser kitchen = new KitchenUser();
                kitchen.setVendorName(specificId);
                user = kitchen;
                break;
            }
            default:
                throw new BadRequestException("請選擇正確角色 STUDENT / TEACHER / ADMIN / KITCHEN");
        }

        user.setPhone(phone);
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(password));

        try {
            userRepository.save(user);
            return "註冊成功：" + role + " 帳號已建立";
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("電話號碼或編號已經被使用");
        }
    }

    @Override
    public LoginResp loginUser(UserLoginDto dto) {
        String phone    = normalizeValue(dto.getPhone());
        String password = normalizeValue(dto.getPassword());
        String role     = normalizeRole(dto.getRole());

        if (!isEightDigits(phone))    throw new BadRequestException("電話號碼必須為 8 位數字");
        if (!isEightDigits(password)) throw new BadRequestException("密碼必須為 8 位數字");
        if (role.isBlank())           throw new BadRequestException("請選擇角色");

        BaseUser user = userRepository.findByPhoneAndRole(phone, role);
        if (user == null) throw new UnauthorizedException("找不到此角色的帳號");

        if (!passwordEncoder.matches(password, user.getPasswordHash()))
            throw new UnauthorizedException("密碼錯誤");

        String token = jwtUtil.generate(user.getId(), user.getRole());
        return new LoginResp(user.getId(), user.getRole(), user.getPhone(),
                "登入成功！歡迎 " + user.getRole() + " 登入系統。", token);
    }

    @Override
    public LoginResp lookupByPhone(String phone) {
        String normalized = normalizeValue(phone);
        if (!isEightDigits(normalized)) throw new BadRequestException("電話號碼必須為 8 位數字");
        BaseUser user = userRepository.findByPhone(normalized);
        if (user == null) throw new BadRequestException("找不到此電話號碼的用戶");
        return new LoginResp(user.getId(), user.getRole(), user.getPhone(), "查詢成功");
    }

    @Override
    public String updatePassword(ChangePasswordDto dto) {
        String role        = normalizeRole(dto.getRole());
        String identifier  = normalizeValue(dto.getIdentifier());
        String newPassword = normalizeValue(dto.getNewPassword());

        if (!isEightDigits(newPassword)) throw new BadRequestException("新密碼必須為 8 位數字");
        if (role.isBlank())              throw new BadRequestException("請選擇角色");

        BaseUser targetUser = null;
        for (BaseUser user : userRepository.findAll()) {
            if (!role.equals(user.getRole())) continue;
            if (user instanceof Student s && isStudentId(identifier)
                    && identifier.equals(s.getStudentId())) { targetUser = user; break; }
            if (user instanceof Teacher t && isTeacherId(identifier)
                    && identifier.equals(t.getEmployeeId())) { targetUser = user; break; }
            if (user instanceof AdminUser a && isAdminId(identifier)
                    && identifier.equals(a.getAdminLevel())) { targetUser = user; break; }
            if (user instanceof KitchenUser && isEightDigits(identifier)
                    && identifier.equals(user.getPhone())) { targetUser = user; break; }
        }

        if (targetUser == null) {
            switch (role) {
                case "STUDENT": throw new BadRequestException("學生必須用 s12345678 格式的學生編號改密碼");
                case "TEACHER": throw new BadRequestException("教師必須用 t12345678 格式的教師編號改密碼");
                case "ADMIN":   throw new BadRequestException("Admin 必須用 a 開頭編號改密碼");
                case "KITCHEN": throw new BadRequestException("Kitchen 必須用 8 位電話號碼改密碼");
                default:        throw new BadRequestException("未知角色");
            }
        }

        targetUser.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(targetUser);
        return "密碼修改成功";
    }

    @Override
    public String adminResetPassword(AdminResetPasswordDto dto) {
        String adminPhone    = normalizeValue(dto.getAdminPhone());
        String adminPassword = normalizeValue(dto.getAdminPassword());
        String targetPhone   = normalizeValue(dto.getTargetPhone());
        String newPassword   = normalizeValue(dto.getNewPassword());

        if (!isEightDigits(adminPhone))  throw new BadRequestException("管理員電話號碼必須為 8 位數字");
        if (!isEightDigits(adminPassword)) throw new BadRequestException("管理員密碼必須為 8 位數字");
        if (!isEightDigits(targetPhone)) throw new BadRequestException("目標用戶電話號碼必須為 8 位數字");
        if (!isEightDigits(newPassword)) throw new BadRequestException("新密碼必須為 8 位數字");

        BaseUser admin = userRepository.findByPhoneAndRole(adminPhone, "ADMIN");
        if (admin == null || !passwordEncoder.matches(adminPassword, admin.getPasswordHash()))
            throw new UnauthorizedException("管理員驗證失敗");

        BaseUser target = userRepository.findByPhone(targetPhone);
        if (target == null) throw new BadRequestException("找不到此電話號碼的用戶");

        target.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(target);
        return "用戶 " + targetPhone + " 的密碼已重置成功";
    }
}
