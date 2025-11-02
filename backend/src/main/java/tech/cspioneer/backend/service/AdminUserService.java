package tech.cspioneer.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.cspioneer.backend.common.ApiException;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.enums.UserRole;
import tech.cspioneer.backend.enums.UserStatus;
import tech.cspioneer.backend.mapper.UserMapper;

import java.util.List;

@Service
public class AdminUserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final tech.cspioneer.backend.service.TokenRevocationService tokenRevocationService;

    public AdminUserService(UserMapper userMapper,
                            PasswordEncoder passwordEncoder,
                            tech.cspioneer.backend.service.TokenRevocationService tokenRevocationService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenRevocationService = tokenRevocationService;
    }

    public record Page<T>(List<T> items, long total) {}

    public Page<User> list(String q, String role, String status, int page, int size) {
        int limit = Math.max(1, Math.min(100, size <= 0 ? 20 : size));
        int offset = Math.max(0, page <= 0 ? 0 : (page - 1) * limit);
        String r = emptyToNull(normalizeRole(role));
        String s = emptyToNull(normalizeStatus(status));
        var items = userMapper.listAdmin(emptyToNull(trimToNull(q)), r, s, limit, offset);
        long total = userMapper.countAdmin(emptyToNull(trimToNull(q)), r, s);
        items.forEach(u -> u.setPassword(null));
        return new Page<>(items, total);
    }

    public User getByUuid(String uuid) {
        User u = userMapper.findByUuid(uuid);
        if (u == null) throw new ApiException(404, "用户不存在");
        u.setPassword(null);
        return u;
    }

    public User updateRoleStatus(String uuid, String role, String status) {
        User u = userMapper.findByUuid(uuid);
        if (u == null) throw new ApiException(404, "用户不存在");
        if (role != null && !role.isBlank()) {
            var r = normalizeRole(role);
            userMapper.updateRoleByUuid(uuid, r);
        }
        if (status != null && !status.isBlank()) {
            var s = normalizeStatus(status);
            userMapper.updateStatusByUuid(uuid, s);
        }
        User updated = userMapper.findByUuid(uuid);
        if (updated != null) updated.setPassword(null);
        return updated;
    }

    public User updateProfileByAdmin(String uuid,
                                     String name,
                                     String email,
                                     String avatar,
                                     String company,
                                     String location,
                                     String gitHub,
                                     String website,
                                     String freeText,
                                     String realName) {
        User u = userMapper.findByUuid(uuid);
        if (u == null) throw new ApiException(404, "用户不存在");
        // name unique check
        if (name != null && !name.isBlank()) {
            User existed = userMapper.findByName(name.trim());
            if (existed != null && !uuid.equals(existed.getUuid())) throw new ApiException(1006, "用户名已被占用");
            userMapper.updateNameByUuid(uuid, name.trim());
        }
        // email unique check
        if (email != null && !email.isBlank()) {
            User existedByEmail = userMapper.findByEmail(email.trim());
            if (existedByEmail != null && !uuid.equals(existedByEmail.getUuid())) throw new ApiException(1006, "邮箱已被占用");
            userMapper.updateEmailByUuid(uuid, email.trim());
        }
        // profile fields
        userMapper.updateProfileByUuid(uuid, avatar, company, location, gitHub, website, freeText, realName);
        User fresh = userMapper.findByUuid(uuid);
        if (fresh != null) fresh.setPassword(null);
        return fresh;
    }

    public void resetPasswordByAdmin(String uuid, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) throw new ApiException(1001, "新密码不能为空");
        User u = userMapper.findByUuid(uuid);
        if (u == null) throw new ApiException(404, "用户不存在");
        String encoded = passwordEncoder.encode(newPassword);
        int n = userMapper.updatePasswordByUuid(uuid, encoded);
        if (n != 1) throw new ApiException(1500, "重置密码失败");
        // revoke all tokens
        tokenRevocationService.revokeAll(uuid);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) return null;
        try { return UserRole.fromCode(role.trim()).getCode(); } catch (Exception e) {
            throw new ApiException(1001, "参数错误: 不支持的role");
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try { return UserStatus.fromCode(status.trim()).getCode(); } catch (Exception e) {
            throw new ApiException(1001, "参数错误: 不支持的status");
        }
    }

    private String trimToNull(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
    private String emptyToNull(String s) { return (s == null || s.isBlank()) ? null : s; }
}
