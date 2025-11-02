package tech.cspioneer.backend.service;

import org.springframework.stereotype.Service;
import tech.cspioneer.backend.mapper.AppSettingMapper;
import tech.cspioneer.backend.entity.AppSetting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PointsPolicyService {
    private final AppSettingMapper settingMapper;

    public PointsPolicyService(AppSettingMapper settingMapper) {
        this.settingMapper = settingMapper;
    }

    public enum SeverityMode { NONE, LEVEL_MULTIPLIER, SCORE_LINEAR }

    public static class PointsSettings {
        public int submittedUserDelta;
        public int submittedOrgDelta;
        public int publishedUserDelta;
        public int publishedOrgDelta;
        public int rejectedUserDelta;
        public int rejectedOrgDelta;
        public SeverityMode severityMode;
        public double levelCritical;
        public double levelHigh;
        public double levelMedium;
        public double levelLow;
        public double scoreK;
        public double scoreB;
    }

    public static class PreviewReq {
        public String event; // SUBMITTED / PUBLISHED / REJECTED
        public Double severityNum; // optional
        public String severityLevel; // optional: CRITICAL/HIGH/MEDIUM/LOW
    }

    public static class PreviewResp {
        public int userDelta;
        public int orgDelta;
        public Map<String, Object> details;
    }

    public Map<String, Object> getSettingsAsMap() {
        PointsSettings s = loadSettings(null);
        Map<String, Object> m = new HashMap<>();
        Map<String, Object> events = new HashMap<>();
        Map<String, Object> submitted = new HashMap<>();
        submitted.put("userDelta", s.submittedUserDelta);
        submitted.put("orgDelta", s.submittedOrgDelta);
        Map<String, Object> published = new HashMap<>();
        published.put("userDelta", s.publishedUserDelta);
        published.put("orgDelta", s.publishedOrgDelta);
        Map<String, Object> rejected = new HashMap<>();
        rejected.put("userDelta", s.rejectedUserDelta);
        rejected.put("orgDelta", s.rejectedOrgDelta);
        events.put("submitted", submitted);
        events.put("published", published);
        events.put("rejected", rejected);
        Map<String, Object> severity = new HashMap<>();
        severity.put("mode", s.severityMode.name());
        Map<String, Object> levels = new HashMap<>();
        levels.put("critical", s.levelCritical);
        levels.put("high", s.levelHigh);
        levels.put("medium", s.levelMedium);
        levels.put("low", s.levelLow);
        Map<String, Object> linear = new HashMap<>();
        linear.put("k", s.scoreK);
        linear.put("b", s.scoreB);
        severity.put("levels", levels);
        severity.put("linear", linear);
        m.put("events", events);
        m.put("severity", severity);
        return m;
    }

    public void updateSettingsFromMap(Map<String, Object> patch) {
        if (patch == null) return;
        // events
        var events = asMap(patch.get("events"));
        if (events != null) {
            upsertDelta(events, "submitted", "userDelta", "points.events.submitted.userDelta");
            upsertDelta(events, "submitted", "orgDelta", "points.events.submitted.orgDelta");
            upsertDelta(events, "published", "userDelta", "points.events.published.userDelta");
            upsertDelta(events, "published", "orgDelta", "points.events.published.orgDelta");
            // REJECTED 不启用，忽略
        }
        // severity
        var sev = asMap(patch.get("severity"));
        if (sev != null) {
            Object mode = sev.get("mode");
            if (mode != null) settingMapper.upsert("points.severity.mode", String.valueOf(mode));
            var levels = asMap(sev.get("levels"));
            if (levels != null) {
                upsertDouble(levels, "critical", "points.severity.level.critical");
                upsertDouble(levels, "high", "points.severity.level.high");
                upsertDouble(levels, "medium", "points.severity.level.medium");
                upsertDouble(levels, "low", "points.severity.level.low");
            }
            var linear = asMap(sev.get("linear"));
            if (linear != null) {
                upsertDouble(linear, "k", "points.severity.score.k");
                upsertDouble(linear, "b", "points.severity.score.b");
            }
        }
    }

    private void upsertDelta(Map<String, Object> events, String key, String fld, String settingKey) {
        var m = asMap(events.get(key));
        if (m != null && m.get(fld) != null) {
            settingMapper.upsert(settingKey, String.valueOf(asInt(m.get(fld), 0)));
        }
    }

    private void upsertDouble(Map<String, Object> m, String fld, String settingKey) {
        if (m.get(fld) != null) {
            settingMapper.upsert(settingKey, String.valueOf(asDouble(m.get(fld), 0)));
        }
    }

    private Map<String, Object> asMap(Object o) {
        if (o instanceof Map) return (Map<String, Object>) o;
        return null;
    }
    private int asInt(Object o, int d) {
        try { return o == null ? d : Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return d; }
    }
    private double asDouble(Object o, double d) {
        try { return o == null ? d : Double.parseDouble(String.valueOf(o)); } catch (Exception e) { return d; }
    }

    public PointsSettings loadSettings(String orgUuid) {
        List<AppSetting> items = settingMapper.listByPrefix("points.");
        Map<String, String> m = new HashMap<>();
        if (items != null) for (AppSetting s : items) m.put(s.getKey(), s.getValue());
        // 组织覆盖：以 points.org.{orgUuid}. 前缀的键覆盖
        if (orgUuid != null && !orgUuid.isBlank()) {
            String prefix = "points.org." + orgUuid + ".";
            List<AppSetting> orgItems = settingMapper.listByPrefix(prefix);
            if (orgItems != null) {
                for (AppSetting s : orgItems) {
                    String k = s.getKey();
                    if (k != null && k.startsWith(prefix)) {
                        String sub = "points." + k.substring(prefix.length());
                        m.put(sub, s.getValue());
                    }
                }
            }
        }
        PointsSettings s = new PointsSettings();
        s.submittedUserDelta = parseInt(m.get("points.events.submitted.userDelta"), 0);
        s.submittedOrgDelta = parseInt(m.get("points.events.submitted.orgDelta"), 2);
        s.publishedUserDelta = parseInt(m.get("points.events.published.userDelta"), 5);
        s.publishedOrgDelta = parseInt(m.get("points.events.published.orgDelta"), 10);
        // REJECTED 不使用，固定为 0
        s.rejectedUserDelta = 0;
        s.rejectedOrgDelta = 0;
        String mode = m.getOrDefault("points.severity.mode", "LEVEL_MULTIPLIER");
        try { s.severityMode = SeverityMode.valueOf(mode.toUpperCase()); } catch (Exception e) { s.severityMode = SeverityMode.LEVEL_MULTIPLIER; }
        s.levelCritical = parseDouble(m.get("points.severity.level.critical"), 2.0);
        s.levelHigh = parseDouble(m.get("points.severity.level.high"), 1.5);
        s.levelMedium = parseDouble(m.get("points.severity.level.medium"), 1.0);
        s.levelLow = parseDouble(m.get("points.severity.level.low"), 0.5);
        s.scoreK = parseDouble(m.get("points.severity.score.k"), 1.0);
        s.scoreB = parseDouble(m.get("points.severity.score.b"), 0.0);
        return s;
    }

    private int parseInt(String v, int d) { try { return v == null ? d : Integer.parseInt(v); } catch (Exception e) { return d; } }
    private double parseDouble(String v, double d) { try { return v == null ? d : Double.parseDouble(v); } catch (Exception e) { return d; } }

    public PreviewResp preview(PreviewReq req) {
        PointsSettings s = loadSettings(null);
        String ev = (req.event == null) ? "" : req.event.trim().toUpperCase();
        int baseUser = switch (ev) {
            case "SUBMITTED" -> s.submittedUserDelta;
            case "PUBLISHED" -> s.publishedUserDelta;
            case "REJECTED" -> 0;
            default -> 0;
        };
        int baseOrg = switch (ev) {
            case "SUBMITTED" -> s.submittedOrgDelta;
            case "PUBLISHED" -> s.publishedOrgDelta;
            case "REJECTED" -> 0;
            default -> 0;
        };
        double factor = severityFactor(s, req.severityNum, req.severityLevel);
        PreviewResp resp = new PreviewResp();
        resp.userDelta = (int) Math.round(baseUser * factor);
        resp.orgDelta = (int) Math.round(baseOrg * factor);
        Map<String, Object> details = new HashMap<>();
        details.put("baseUser", baseUser);
        details.put("baseOrg", baseOrg);
        details.put("factor", factor);
        details.put("mode", s.severityMode.name());
        resp.details = details;
        return resp;
    }

    // ========== Org override APIs ==========
    public Map<String, Object> getSettingsAsMapForOrg(String orgUuid) {
        PointsSettings s = loadSettings(orgUuid);
        Map<String, Object> m = new HashMap<>();
        Map<String, Object> events = new HashMap<>();
        Map<String, Object> submitted = new HashMap<>();
        submitted.put("userDelta", s.submittedUserDelta);
        submitted.put("orgDelta", s.submittedOrgDelta);
        Map<String, Object> published = new HashMap<>();
        published.put("userDelta", s.publishedUserDelta);
        published.put("orgDelta", s.publishedOrgDelta);
        events.put("submitted", submitted);
        events.put("published", published);
        Map<String, Object> severity = new HashMap<>();
        severity.put("mode", s.severityMode.name());
        Map<String, Object> levels = new HashMap<>();
        levels.put("critical", s.levelCritical);
        levels.put("high", s.levelHigh);
        levels.put("medium", s.levelMedium);
        levels.put("low", s.levelLow);
        Map<String, Object> linear = new HashMap<>();
        linear.put("k", s.scoreK);
        linear.put("b", s.scoreB);
        severity.put("levels", levels);
        severity.put("linear", linear);
        m.put("events", events);
        m.put("severity", severity);
        return m;
    }

    public void updateSettingsFromMapForOrg(String orgUuid, Map<String, Object> patch) {
        if (patch == null) return;
        String prefix = "points.org." + orgUuid + ".";
        var events = asMap(patch.get("events"));
        if (events != null) {
            upsertDeltaOrg(prefix, events, "submitted", "userDelta", "events.submitted.userDelta");
            upsertDeltaOrg(prefix, events, "submitted", "orgDelta", "events.submitted.orgDelta");
            upsertDeltaOrg(prefix, events, "published", "userDelta", "events.published.userDelta");
            upsertDeltaOrg(prefix, events, "published", "orgDelta", "events.published.orgDelta");
        }
        var sev = asMap(patch.get("severity"));
        if (sev != null) {
            Object mode = sev.get("mode");
            if (mode != null) settingMapper.upsert(prefix + "severity.mode", String.valueOf(mode));
            var levels = asMap(sev.get("levels"));
            if (levels != null) {
                upsertDoubleOrg(prefix, levels, "critical", "severity.level.critical");
                upsertDoubleOrg(prefix, levels, "high", "severity.level.high");
                upsertDoubleOrg(prefix, levels, "medium", "severity.level.medium");
                upsertDoubleOrg(prefix, levels, "low", "severity.level.low");
            }
            var linear = asMap(sev.get("linear"));
            if (linear != null) {
                upsertDoubleOrg(prefix, linear, "k", "severity.score.k");
                upsertDoubleOrg(prefix, linear, "b", "severity.score.b");
            }
        }
    }

    private void upsertDeltaOrg(String prefix, Map<String, Object> events, String key, String fld, String suffix) {
        var m = asMap(events.get(key));
        if (m != null && m.get(fld) != null) {
            settingMapper.upsert(prefix + suffix, String.valueOf(asInt(m.get(fld), 0)));
        }
    }
    private void upsertDoubleOrg(String prefix, Map<String, Object> m, String fld, String suffix) {
        if (m.get(fld) != null) {
            settingMapper.upsert(prefix + suffix, String.valueOf(asDouble(m.get(fld), 0)));
        }
    }

    public PreviewResp previewForOrg(String orgUuid, PreviewReq req) {
        PointsSettings s = loadSettings(orgUuid);
        String ev = (req.event == null) ? "" : req.event.trim().toUpperCase();
        int baseUser = switch (ev) {
            case "SUBMITTED" -> s.submittedUserDelta;
            case "PUBLISHED" -> s.publishedUserDelta;
            case "REJECTED" -> 0;
            default -> 0;
        };
        int baseOrg = switch (ev) {
            case "SUBMITTED" -> s.submittedOrgDelta;
            case "PUBLISHED" -> s.publishedOrgDelta;
            case "REJECTED" -> 0;
            default -> 0;
        };
        double factor = severityFactor(s, req.severityNum, req.severityLevel);
        PreviewResp resp = new PreviewResp();
        resp.userDelta = (int) Math.round(baseUser * factor);
        resp.orgDelta = (int) Math.round(baseOrg * factor);
        Map<String, Object> details = new HashMap<>();
        details.put("baseUser", baseUser);
        details.put("baseOrg", baseOrg);
        details.put("factor", factor);
        details.put("mode", s.severityMode.name());
        resp.details = details;
        return resp;
    }

    private double severityFactor(PointsSettings s, Double severityNum, String severityLevel) {
        if (s.severityMode == SeverityMode.NONE) return 1.0;
        if (s.severityMode == SeverityMode.SCORE_LINEAR) {
            double v = (severityNum == null ? 0.0 : severityNum.doubleValue());
            return Math.max(0.0, s.scoreK * v + s.scoreB);
        }
        // LEVEL_MULTIPLIER
        String lvl = (severityLevel != null && !severityLevel.isBlank()) ? severityLevel.trim().toUpperCase() : null;
        if (lvl == null && severityNum != null) {
            double n = severityNum;
            if (n >= 9.0) lvl = "CRITICAL"; else if (n >= 7.0) lvl = "HIGH"; else if (n >= 4.0) lvl = "MEDIUM"; else lvl = "LOW";
        }
        if (lvl == null) return 1.0;
        return switch (lvl) {
            case "CRITICAL" -> s.levelCritical;
            case "HIGH" -> s.levelHigh;
            case "LOW" -> s.levelLow;
            default -> s.levelMedium;
        };
    }
}
