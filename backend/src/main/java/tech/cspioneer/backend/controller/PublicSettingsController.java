package tech.cspioneer.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.cspioneer.backend.common.ApiResponse;
import tech.cspioneer.backend.service.SettingsService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/settings")
@Tag(name = "公共-系统设置")
public class PublicSettingsController {
    private final SettingsService service;

    public PublicSettingsController(SettingsService service) {
        this.service = service;
    }

    @GetMapping("/site")
    @Operation(summary = "网站底部设置（公共读取）")
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
}

