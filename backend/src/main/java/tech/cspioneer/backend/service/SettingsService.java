package tech.cspioneer.backend.service;

import org.springframework.stereotype.Service;
import tech.cspioneer.backend.mapper.AppSettingMapper;
import tech.cspioneer.backend.entity.AppSetting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SettingsService {
    private final AppSettingMapper mapper;

    public SettingsService(AppSettingMapper mapper) {
        this.mapper = mapper;
    }

    public Map<String, String> getSiteSettings() {
        // keys under prefix 'footer.'
        List<AppSetting> items = mapper.listByPrefix("footer.");
        Map<String, String> m = new HashMap<>();
        if (items != null) {
            for (AppSetting s : items) {
                m.put(s.getKey(), s.getValue());
            }
        }
        return m;
    }

    public void updateSiteSettings(Map<String, String> patch) {
        if (patch == null) return;
        for (Map.Entry<String, String> e : patch.entrySet()) {
            String k = e.getKey(); String v = e.getValue();
            if (k == null || k.isBlank()) continue;
            mapper.upsert(k, v);
        }
    }
}

