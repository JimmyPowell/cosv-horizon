package tech.cspioneer.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.common.ApiResponse;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.service.UserService;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Tag(name = "用户资料")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "公共用户资料（精简）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ApiResponse<Map<String, Object>> publicProfile(@PathVariable("uuid") String uuid) {
        User user = userService.requireUserByUuid(uuid);
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> u = new HashMap<>();
        u.put("uuid", user.getUuid());
        u.put("name", user.getName());
        u.put("avatar", user.getAvatar());
        u.put("company", user.getCompany());
        u.put("location", user.getLocation());
        u.put("gitHub", user.getGitHub());
        u.put("website", user.getWebsite());
        u.put("freeText", user.getFreeText());
        data.put("user", u);
        return ApiResponse.success(data);
    }

    public static class UpdateProfileReq {
        public String name;
        public String avatar;
        public String company;
        public String location;
        public String gitHub;
        public String website;
        public String freeText;
        public String realName;
    }

    @GetMapping("/me")
    @Operation(summary = "查看我的个人资料")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ApiResponse<Map<String, Object>> me(Principal principal) {
        String userUuid = principal.getName();
        User user = userService.requireUserByUuid(userUuid);
        Map<String, Object> data = new HashMap<>();
        data.put("user", userView(withoutPassword(user)));
        return ApiResponse.success(data);
    }

    @PatchMapping("/me")
    @Operation(summary = "修改我的个人资料（非敏感字段）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ApiResponse<Map<String, Object>> updateMe(@Valid @RequestBody UpdateProfileReq req, Principal principal) {
        String userUuid = principal.getName();
        User updated = userService.updateProfile(userUuid, req.avatar, req.company, req.location, req.gitHub, req.website, req.freeText, req.realName);
        if (req.name != null) {
            updated = userService.updateUsername(userUuid, req.name);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("user", userView(withoutPassword(updated)));
        return ApiResponse.success(data);
    }

    @GetMapping("/me/stats")
    @Operation(summary = "获取我的统计信息")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ApiResponse<Map<String, Object>> getMyStats(Principal principal) {
        String userUuid = principal.getName();
        Map<String, Object> stats = userService.getUserStats(userUuid);
        Map<String, Object> data = new HashMap<>();
        data.put("stats", stats);
        return ApiResponse.success(data);
    }

    @GetMapping("/me/contributions")
    @Operation(summary = "获取我的贡献日历数据")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ApiResponse<Map<String, Object>> getMyContributions(
            @RequestParam(value = "year", required = false) Integer year,
            Principal principal) {
        String userUuid = principal.getName();
        Map<String, Object> contributions = userService.getUserContributions(userUuid, year);
        Map<String, Object> data = new HashMap<>();
        data.put("contributions", contributions);
        return ApiResponse.success(data);
    }

    @GetMapping("/{uuid}/stats")
    @Operation(summary = "用户统计信息（公共）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ApiResponse<Map<String, Object>> getUserStatsPublic(@PathVariable("uuid") String uuid) {
        Map<String, Object> stats = userService.getUserStats(uuid);
        Map<String, Object> data = new HashMap<>();
        data.put("stats", stats);
        return ApiResponse.success(data);
    }

    @GetMapping("/{uuid}/contributions")
    @Operation(summary = "用户贡献日历数据（公共）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ApiResponse<Map<String, Object>> getUserContributionsPublic(@PathVariable("uuid") String uuid,
                                                                       @RequestParam(value = "year", required = false) Integer year) {
        Map<String, Object> contributions = userService.getUserContributions(uuid, year);
        Map<String, Object> data = new HashMap<>();
        data.put("contributions", contributions);
        return ApiResponse.success(data);
    }

    private Map<String, Object> userView(User user) {
        Map<String, Object> u = new HashMap<>();
        u.put("uuid", user.getUuid());
        u.put("name", user.getName());
        u.put("email", user.getEmail());
        u.put("avatar", user.getAvatar());
        u.put("role", user.getRole());
        u.put("status", user.getStatus());
        u.put("company", user.getCompany());
        u.put("location", user.getLocation());
        u.put("gitHub", user.getGitHub());
        u.put("website", user.getWebsite());
        u.put("freeText", user.getFreeText());
        u.put("realName", user.getRealName());
        u.put("createDate", user.getCreateDate());
        u.put("updateDate", user.getUpdateDate());
        return u;
    }

    private User withoutPassword(User user) {
        if (user != null) user.setPassword(null);
        return user;
    }
}
