package com.canteen.demo_canteen.service;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.canteen.demo_canteen.dto.BaseUserRepository;
import com.canteen.demo_canteen.dto.ChangePasswordDto;
import com.canteen.demo_canteen.dto.LoginResp;
import com.canteen.demo_canteen.dto.UserLoginDto;
import com.canteen.demo_canteen.dto.UserRegisterDto;
import com.canteen.demo_canteen.entity.AdminUser;
import com.canteen.demo_canteen.entity.BaseUser;
import com.canteen.demo_canteen.entity.KitchenUser;
import com.canteen.demo_canteen.entity.Student;
import com.canteen.demo_canteen.entity.Teacher;
import com.canteen.demo_canteen.exception.BadRequestException;
import com.canteen.demo_canteen.exception.ConflictException;
import com.canteen.demo_canteen.exception.UnauthorizedException;
import com.canteen.demo_canteen.util.JwtUtil;

@Service
public class UserService {

    private static final String ADMIN_REGISTER_ACCESS_RIGHT = "a12345678";
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private BaseUserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

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

    public LoginResp lookupByPhone(String phone) {
        String normalized = normalizeValue(phone);
        if (!isEightDigits(normalized)) throw new BadRequestException("電話號碼必須為 8 位數字");
        BaseUser user = userRepository.findByPhone(normalized);
        if (user == null) throw new BadRequestException("找不到此電話號碼的用戶");
        return new LoginResp(user.getId(), user.getRole(), user.getPhone(), "查詢成功");
    }

    public String updatePassword(ChangePasswordDto dto) {
        String role        = normalizeRole(dto.getRole());
        String identifier  = normalizeValue(dto.getIdentifier());
        String newPassword = normalizeValue(dto.getNewPassword());

        if (!isEightDigits(newPassword)) throw new BadRequestException("新密碼必須為 8 位數字");
        if (role.isBlank())              throw new BadRequestException("請選擇角色");

        BaseUser targetUser = null;
        for (BaseUser user : userRepository.findAll()) {
            if (!role.equals(user.getRole())) continue;
            if (user instanceof Student && isStudentId(identifier)
                    && identifier.equals(((Student) user).getStudentId())) { targetUser = user; break; }
            if (user instanceof Teacher && isTeacherId(identifier)
                    && identifier.equals(((Teacher) user).getEmployeeId())) { targetUser = user; break; }
            if (user instanceof AdminUser && isAdminId(identifier)
                    && identifier.equals(((AdminUser) user).getAdminLevel())) { targetUser = user; break; }
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
}
