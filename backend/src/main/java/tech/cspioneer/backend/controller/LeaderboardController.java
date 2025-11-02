package tech.cspioneer.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.common.ApiResponse;
import tech.cspioneer.backend.entity.Organization;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.mapper.OrganizationMapper;
import tech.cspioneer.backend.mapper.UserMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/leaderboard")
@Tag(name = "积分排行榜")
@SecurityRequirement(name = "bearerAuth")
public class LeaderboardController {
    private final UserMapper userMapper;
    private final OrganizationMapper organizationMapper;

    public LeaderboardController(UserMapper userMapper, OrganizationMapper organizationMapper) {
        this.userMapper = userMapper;
        this.organizationMapper = organizationMapper;
    }

    @GetMapping("/users")
    @Operation(summary = "用户积分排行榜")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ApiResponse<Map<String, Object>> users(@RequestParam(value = "limit", defaultValue = "10") int limit,
                                                  @RequestParam(value = "offset", defaultValue = "0") int offset,
                                                  @RequestParam(value = "withTotal", defaultValue = "false") boolean withTotal) {
        int l = Math.max(1, Math.min(100, limit));
        int o = Math.max(0, offset);
        List<User> items = userMapper.listByRating(l, o);
        List<Map<String, Object>> list = items.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("uuid", u.getUuid());
            m.put("name", u.getName());
            m.put("avatar", u.getAvatar());
            m.put("company", u.getCompany());
            m.put("rating", u.getRating());
            return m;
        }).toList();
        Map<String, Object> data = new HashMap<>();
        data.put("items", list);
        if (withTotal) data.put("total", userMapper.countAll());
        return ApiResponse.success(data);
    }

    @GetMapping("/organizations")
    @Operation(summary = "组织积分排行榜")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ApiResponse<Map<String, Object>> orgs(@RequestParam(value = "limit", defaultValue = "10") int limit,
                                                 @RequestParam(value = "offset", defaultValue = "0") int offset,
                                                 @RequestParam(value = "withTotal", defaultValue = "false") boolean withTotal) {
        int l = Math.max(1, Math.min(100, limit));
        int o = Math.max(0, offset);
        List<Organization> items = organizationMapper.listByRating(l, o);
        List<Map<String, Object>> list = items.stream().map(org -> {
            Map<String, Object> m = new HashMap<>();
            m.put("uuid", org.getUuid());
            m.put("name", org.getName());
            m.put("avatar", org.getAvatar());
            m.put("description", org.getDescription());
            m.put("rating", org.getRating());
            return m;
        }).toList();
        Map<String, Object> data = new HashMap<>();
        data.put("items", list);
        if (withTotal) data.put("total", organizationMapper.countAllActive());
        return ApiResponse.success(data);
    }
}

