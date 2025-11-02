package tech.cspioneer.backend.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.common.ApiResponse;
import tech.cspioneer.backend.service.SettingsService;
import tech.cspioneer.backend.service.PointsPolicyService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/settings")
@Tag(name = "管理员-系统设置")
@SecurityRequirement(name = "bearerAuth")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class AdminSettingsController {
    private final SettingsService service;
    private final PointsPolicyService pointsPolicyService;

    public AdminSettingsController(SettingsService service, PointsPolicyService pointsPolicyService) {
        this.service = service;
        this.pointsPolicyService = pointsPolicyService;
    }

    @GetMapping("/site")
    @Operation(summary = "网站底部设置（读取）")
    public ApiResponse<Map<String, Object>> getSite() {
        Map<String, String> raw = service.getSiteSettings();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> settings = new HashMap<>();
        settings.put("icpRecord", raw.getOrDefault("footer.icp_record", null));
        settings.put("psbRecord", raw.getOrDefault("footer.psb_record", null));
        settings.put("copyrightText", raw.getOrDefault("footer.copyright_text", null));
        settings.put("fontFamily", raw.getOrDefault("footer.copyright_font_family", null));
        settings.put("fontSize", raw.getOrDefault("footer.copyright_font_size", null));
        data.put("settings", settings);
        return ApiResponse.success(data);
    }

    public static class SiteSettingsReq {
        public String icpRecord; public String psbRecord; public String copyrightText; public String fontFamily; public String fontSize;
    }

    @PutMapping("/site")
    @Operation(summary = "网站底部设置（更新）")
    public ApiResponse<Map<String,Object>> updateSite(@Valid @RequestBody SiteSettingsReq req) {
        Map<String, String> patch = new HashMap<>();
        if (req.icpRecord != null) patch.put("footer.icp_record", req.icpRecord.trim());
        if (req.psbRecord != null) patch.put("footer.psb_record", req.psbRecord.trim());
        if (req.copyrightText != null) patch.put("footer.copyright_text", req.copyrightText.trim());
        if (req.fontFamily != null) patch.put("footer.copyright_font_family", req.fontFamily.trim());
        if (req.fontSize != null) patch.put("footer.copyright_font_size", req.fontSize.trim());
        service.updateSiteSettings(patch);
        return getSite();
    }

    // ===== Points settings =====
    @GetMapping("/points")
    @Operation(summary = "积分策略设置（读取）")
    public ApiResponse<Map<String, Object>> getPoints() {
        Map<String, Object> data = new HashMap<>();
        data.put("settings", pointsPolicyService.getSettingsAsMap());
        return ApiResponse.success(data);
    }

    public static class PointsSettingsReq { public Map<String, Object> events; public Map<String, Object> severity; }

    @PutMapping("/points")
    @Operation(summary = "积分策略设置（更新）")
    public ApiResponse<Map<String, Object>> updatePoints(@Valid @RequestBody PointsSettingsReq req) {
        Map<String, Object> patch = new HashMap<>();
        if (req.events != null) patch.put("events", req.events);
        if (req.severity != null) patch.put("severity", req.severity);
        pointsPolicyService.updateSettingsFromMap(patch);
        return getPoints();
    }

    public static class PointsPreviewReq { public String event; public Double severityNum; public String severityLevel; }

    @PostMapping("/points/preview")
    @Operation(summary = "积分策略预览计算")
    public ApiResponse<Map<String, Object>> preview(@Valid @RequestBody PointsPreviewReq req) {
        PointsPolicyService.PreviewReq r = new PointsPolicyService.PreviewReq();
        r.event = req.event; r.severityNum = req.severityNum; r.severityLevel = req.severityLevel;
        var out = pointsPolicyService.preview(r);
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        result.put("userDelta", out.userDelta);
        result.put("orgDelta", out.orgDelta);
        result.put("details", out.details);
        data.put("result", result);
        return ApiResponse.success(data);
    }
}
