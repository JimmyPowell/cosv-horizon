package tech.cspioneer.backend.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.common.ApiResponse;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.service.AdminUserService;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/users")
@Tag(name = "管理员-用户管理")
@SecurityRequirement(name = "bearerAuth")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final AdminUserService service;

    public AdminUserController(AdminUserService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "用户列表（管理员）")
    public ApiResponse<Map<String, Object>> list(@RequestParam(value = "q", required = false) String q,
                                                 @RequestParam(value = "role", required = false) String role,
                                                 @RequestParam(value = "status", required = false) String status,
                                                 @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                                 @RequestParam(value = "size", required = false, defaultValue = "20") int size,
                                                 @RequestParam(value = "withTotal", required = false, defaultValue = "false") boolean withTotal) {
        var p = service.list(q, role, status, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("size", size);
        data.put("items", p.items());
        if (withTotal) data.put("total", p.total());
        return ApiResponse.success(data);
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "用户详情（管理员）")
    public ApiResponse<Map<String, Object>> get(@PathVariable("uuid") String uuid) {
        User u = service.getByUuid(uuid);
        Map<String, Object> data = new HashMap<>();
        data.put("user", userView(u));
        return ApiResponse.success(data);
    }

    public static class UpdateReq { public String role; public String status; }

    @PatchMapping("/{uuid}")
    @Operation(summary = "更新用户角色/状态（管理员）")
    public ApiResponse<Map<String, Object>> update(@PathVariable("uuid") String uuid,
                                                   @Valid @RequestBody UpdateReq req,
                                                   Principal principal) {
        User updated = service.updateRoleStatus(uuid, req.role, req.status);
        Map<String, Object> data = new HashMap<>();
        data.put("user", userView(updated));
        return ApiResponse.success(data);
    }

    public static class UpdateProfileReq {
        public String name;
        public String email;
        public String avatar;
        public String company;
        public String location;
        public String gitHub;
        public String website;
        public String freeText;
        public String realName;
    }

    @PatchMapping("/{uuid}/profile")
    @Operation(summary = "编辑用户资料（管理员）")
    public ApiResponse<Map<String, Object>> updateProfile(@PathVariable("uuid") String uuid,
                                                         @Valid @RequestBody UpdateProfileReq req) {
        User updated = service.updateProfileByAdmin(uuid, req.name, req.email, req.avatar, req.company, req.location, req.gitHub, req.website, req.freeText, req.realName);
        Map<String, Object> data = new HashMap<>();
        data.put("user", userView(updated));
        return ApiResponse.success(data);
    }

    public static class ResetPasswordReq { public String newPassword; }

    @PostMapping("/{uuid}/reset-password")
    @Operation(summary = "重置用户密码（管理员，吊销所有旧令牌）")
    public ApiResponse<Void> resetPassword(@PathVariable("uuid") String uuid,
                                           @Valid @RequestBody ResetPasswordReq req) {
        service.resetPasswordByAdmin(uuid, req == null ? null : req.newPassword);
        return ApiResponse.success(null);
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
}
