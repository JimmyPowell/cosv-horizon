package tech.cspioneer.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.cspioneer.backend.common.ApiException;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.mapper.UserMapper;
import tech.cspioneer.backend.mapper.VulnerabilityMetadataMapper;
import tech.cspioneer.backend.enums.UserRole;
import tech.cspioneer.backend.enums.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final VulnerabilityMetadataMapper vulnerabilityMetadataMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper,
                      VulnerabilityMetadataMapper vulnerabilityMetadataMapper,
                      PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.vulnerabilityMetadataMapper = vulnerabilityMetadataMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public User findByLogin(String login) {
        return userMapper.findByEmailOrName(login);
    }

    public User findByEmail(String email) { return userMapper.findByEmail(email); }
    public User findByName(String name) { return userMapper.findByName(name); }
    public User findByUuid(String uuid) { return userMapper.findByUuid(uuid); }

    public User requireUserByUuid(String uuid) {
        User u = userMapper.findByUuid(uuid);
        if (u == null) throw new ApiException(1005, "用户不存在或状态异常");
        return u;
    }

    @Transactional
    public User registerUser(String email, String username, String rawPassword,
                             String realName, String company, String location) {
        if (userMapper.findByEmail(email) != null) {
            throw new ApiException(1006, "邮箱已被占用");
        }
        if (userMapper.findByName(username) != null) {
            throw new ApiException(1006, "用户名已被占用");
        }
        User user = new User();
        user.setUuid(UUID.randomUUID().toString());
        user.setEmail(email);
        user.setName(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setRealName(realName);
        user.setCompany(company);
        user.setLocation(location);
        int rows = userMapper.insert(user);
        if (rows != 1) {
            throw new ApiException(1500, "创建用户失败");
        }
        // 读取数据库回填默认时间等字段
        User saved = userMapper.findByUuid(user.getUuid());
        if (saved != null) saved.setPassword(null);
        return saved;
    }

    public boolean verifyPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    @Transactional
    public User updateProfile(String userUuid,
                              String avatar,
                              String company,
                              String location,
                              String gitHub,
                              String website,
                              String freeText,
                              String realName) {
        // 确保用户存在
        requireUserByUuid(userUuid);
        userMapper.updateProfileByUuid(userUuid, avatar, company, location, gitHub, website, freeText, realName);
        User fresh = userMapper.findByUuid(userUuid);
        if (fresh != null) fresh.setPassword(null);
        return fresh;
    }

    @Transactional
    public User updateUsername(String userUuid, String newName) {
        User current = requireUserByUuid(userUuid);
        if (newName == null) {
            throw new ApiException(1001, "参数错误: 用户名不能为空");
        }
        String trimmed = newName.trim();
        if (trimmed.isEmpty()) {
            throw new ApiException(1001, "参数错误: 用户名不能为空");
        }
        if (trimmed.equals(current.getName())) {
            // 未变化，直接返回当前用户信息
            current.setPassword(null);
            return current;
        }
        User existed = userMapper.findByName(trimmed);
        if (existed != null && !userUuid.equals(existed.getUuid())) {
            throw new ApiException(1006, "用户名已被占用");
        }
        int n = userMapper.updateNameByUuid(userUuid, trimmed);
        if (n != 1) {
            throw new ApiException(1500, "更新用户名失败");
        }
        User fresh = userMapper.findByUuid(userUuid);
        if (fresh != null) fresh.setPassword(null);
        return fresh;
    }

    /**
     * 通过邮箱重置密码（不抛出用户是否存在的异常），返回是否成功更新。
     */
    @Transactional
    public boolean resetPasswordByEmail(String email, String rawPassword) {
        User u = userMapper.findByEmail(email);
        if (u == null) return false;
        String encoded = passwordEncoder.encode(rawPassword);
        int rows = userMapper.updatePasswordByEmail(email, encoded);
        return rows == 1;
    }

    /**
     * 获取用户统计信息
     */
    public Map<String, Object> getUserStats(String userUuid) {
        User user = requireUserByUuid(userUuid);

        // 获取总漏洞数
        long totalVulnerabilities = vulnerabilityMetadataMapper.countByUserId(user.getId());

        // 获取今年的贡献数
        int currentYear = LocalDate.now().getYear();
        LocalDate startOfYear = LocalDate.of(currentYear, 1, 1);
        LocalDate endOfYear = LocalDate.of(currentYear + 1, 1, 1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<Map<String, Object>> yearContributions = vulnerabilityMetadataMapper.getContributionsByUserIdAndDateRange(
                user.getId(),
                startOfYear.format(formatter),
                endOfYear.format(formatter)
        );

        long totalContributions = yearContributions.stream()
                .mapToLong(m -> ((Number) m.get("count")).longValue())
                .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVulnerabilities", totalVulnerabilities);
        stats.put("totalContributions", totalContributions);
        stats.put("currentYear", currentYear);

        return stats;
    }

    /**
     * 获取用户贡献日历数据
     */
    public Map<String, Object> getUserContributions(String userUuid, Integer year) {
        User user = requireUserByUuid(userUuid);

        // 如果没有指定年份，使用当前年份
        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        // 计算日期范围（最近365天）
        LocalDate endDate = LocalDate.now().plusDays(1);
        LocalDate startDate = endDate.minusDays(365);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<Map<String, Object>> contributions = vulnerabilityMetadataMapper.getContributionsByUserIdAndDateRange(
                user.getId(),
                startDate.format(formatter),
                endDate.format(formatter)
        );

        // 转换为 Map<String, Integer> 格式
        Map<String, Integer> contributionsByDate = new HashMap<>();
        for (Map<String, Object> item : contributions) {
            String date = item.get("date").toString();
            Integer count = ((Number) item.get("count")).intValue();
            contributionsByDate.put(date, count);
        }

        // 计算总贡献数
        long total = contributionsByDate.values().stream()
                .mapToLong(Integer::longValue)
                .sum();

        Map<String, Object> result = new HashMap<>();
        result.put("year", targetYear);
        result.put("startDate", startDate.format(formatter));
        result.put("endDate", endDate.format(formatter));
        result.put("total", total);
        result.put("contributionsByDate", contributionsByDate);

        return result;
    }
}
