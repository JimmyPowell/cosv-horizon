package tech.cspioneer.backend.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Component;
import tech.cspioneer.backend.common.ApiException;
import tech.cspioneer.backend.entity.Organization;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.VulnerabilityMetadata;
import tech.cspioneer.backend.mapper.LnkUserOrganizationMapper;
import tech.cspioneer.backend.mapper.OrganizationMapper;
import tech.cspioneer.backend.mapper.UserMapper;
import tech.cspioneer.backend.service.VulnerabilityService;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@Slf4j
public class SearchFacade {
    private static final ObjectMapper M = new ObjectMapper();
    private final SearchProperties props;
    private final VulnerabilityService vulnService;
    private final RestClient es; // may be null when disabled
    private final UserMapper userMapper;
    private final OrganizationMapper organizationMapper;
    private final LnkUserOrganizationMapper lnkUserOrganizationMapper;

    public SearchFacade(SearchProperties props,
                        VulnerabilityService vulnService,
                        org.springframework.beans.factory.ObjectProvider<RestClient> esProvider,
                        UserMapper userMapper,
                        OrganizationMapper organizationMapper,
                        LnkUserOrganizationMapper lnkUserOrganizationMapper) {
        this.props = props; this.vulnService = vulnService; this.es = esProvider.getIfAvailable();
        this.userMapper = userMapper; this.organizationMapper = organizationMapper; this.lnkUserOrganizationMapper = lnkUserOrganizationMapper;
    }

    public boolean isEsEnabled() {
        return "es".equalsIgnoreCase(props.getEngine()) && props.getEs().isEnabled() && es != null;
    }

    public Result search(String languages,
                         String severityLevels,
                         Float severityGe,
                         Float severityLe,
                         String status,
                         String q,
                         String identifierPrefix,
                         String organizationUuid,
                         String category,
                         int page,
                         int size,
                         String sortBy,
                         String sortOrder,
                         boolean withTotal) {
        if (!isEsEnabled()) throw new IllegalStateException("ES disabled");

        String enforcedStatus = status;
        if (organizationUuid != null && !organizationUuid.isBlank()) {
            Organization org = organizationMapper.findByUuid(organizationUuid);
            if (org == null) throw new ApiException(404, "组织不存在");
            if (!isAdmin()) {
                String userUuid = currentUserUuid();
                if (userUuid != null) {
                    User user = userMapper.findByUuid(userUuid);
                    var link = (user == null || org.getId() == null) ? null : lnkUserOrganizationMapper.findByOrgIdAndUserId(org.getId(), user.getId());
                    boolean isMember = (link != null);
                    if (!isMember) {
                        if (Boolean.TRUE.equals(org.getIsPublic())) {
                            if (enforcedStatus == null || enforcedStatus.isBlank()) enforcedStatus = tech.cspioneer.backend.enums.VulnerabilityStatus.ACTIVE.name();
                        } else {
                            throw new ApiException(404, "组织不存在");
                        }
                    }
                }
            }
        }

        String readAlias = props.getEs().getReadAlias();
        int from = Math.max(0, (page <= 1 ? 0 : (page - 1) * Math.max(1, size)));
        int sizeLimit = Math.max(1, Math.min(100, size));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("from", from);
        body.put("size", sizeLimit);
        Map<String, Object> sort = new LinkedHashMap<>();
        if ("severity".equalsIgnoreCase(sortBy)) sort.put("severityNum", Map.of("order", ("asc".equalsIgnoreCase(sortOrder) ? "asc" : "desc")));
        else sort.put("modified", Map.of("order", ("asc".equalsIgnoreCase(sortOrder) ? "asc" : "desc")));
        body.put("sort", List.of(sort));

        List<Object> must = new ArrayList<>();
        List<Object> filter = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            must.add(Map.of("simple_query_string", Map.of(
                    "query", q.trim(),
                    "fields", List.of("identifier^5", "summary^2", "details", "aliases^3", "tagCodes^1.5")
            )));
        }
        if (identifierPrefix != null && !identifierPrefix.isBlank()) {
            must.add(Map.of("prefix", Map.of("identifier.keyword", identifierPrefix.trim())));
        }
        if (languages != null && !languages.isBlank()) {
            var vals = Arrays.stream(languages.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
            filter.add(Map.of("terms", Map.of("language", vals)));
        }
        if (enforcedStatus != null && !enforcedStatus.isBlank()) filter.add(Map.of("term", Map.of("status", enforcedStatus.trim())));
        if (category != null && !category.isBlank()) filter.add(Map.of("term", Map.of("categoryCode", category.trim())));
        if (organizationUuid != null && !organizationUuid.isBlank()) filter.add(Map.of("term", Map.of("organizationUuid", organizationUuid.trim())));
        if (severityLevels != null && !severityLevels.isBlank()) {
            var lvls = Arrays.stream(severityLevels.split(",")).map(String::trim).filter(s -> !s.isEmpty()).map(String::toUpperCase).toList();
            List<Object> should = new ArrayList<>();
            if (lvls.contains("CRITICAL")) should.add(Map.of("range", Map.of("severityNum", Map.of("gte", 9.0))));
            if (lvls.contains("HIGH")) should.add(Map.of("range", Map.of("severityNum", Map.of("gte", 7.0, "lt", 9.0))));
            if (lvls.contains("MEDIUM")) should.add(Map.of("range", Map.of("severityNum", Map.of("gte", 4.0, "lt", 7.0))));
            if (lvls.contains("LOW")) should.add(Map.of("range", Map.of("severityNum", Map.of("gte", 0.0, "lt", 4.0))));
            filter.add(Map.of("bool", Map.of("should", should, "minimum_should_match", 1)));
        }
        if (severityGe != null) filter.add(Map.of("range", Map.of("severityNum", Map.of("gte", severityGe))));
        if (severityLe != null) filter.add(Map.of("range", Map.of("severityNum", Map.of("lte", severityLe))));
        Map<String, Object> query = Map.of("bool", Map.of("must", must, "filter", filter));
        body.put("query", query);
        if (withTotal) body.put("track_total_hits", true);

        try {
            Request req = new Request("POST", "/" + readAlias + "/_search");
            req.setEntity(new NStringEntity(M.writeValueAsString(body), ContentType.APPLICATION_JSON));
            Response resp = es.performRequest(req);
            Map<?,?> parsed = M.readValue(new java.io.InputStreamReader(resp.getEntity().getContent(), StandardCharsets.UTF_8), Map.class);
            Map hits = (Map) parsed.get("hits");
            long total = -1;
            if (withTotal && hits.get("total") instanceof Map totalMap) {
                Object val = totalMap.get("value");
                if (val instanceof Number n) total = n.longValue();
            }
            List<Map<String, Object>> views = new ArrayList<>();
            List<Map> items = (List<Map>) hits.get("hits");
            if (items != null) {
                for (Map h : items) {
                    Map src = (Map) h.get("_source");
                    if (src == null) continue;
                    Object id = src.get("uuid");
                    if (id == null) continue;
                    try {
                        VulnerabilityMetadata vm = vulnService.getByUuid(String.valueOf(id));
                        Map<String, Object> view = new HashMap<>();
                        view.put("uuid", vm.getUuid());
                        view.put("identifier", vm.getIdentifier());
                        view.put("summary", vm.getSummary());
                        view.put("details", vm.getDetails());
                        view.put("severityNum", vm.getSeverityNum());
                        view.put("modified", vm.getModified());
                        view.put("submitted", vm.getSubmitted());
                        view.put("published", vm.getPublished());
                        view.put("withdrawn", vm.getWithdrawn());
                        view.put("language", vm.getLanguage());
                        view.put("status", vm.getStatus());
                        view.put("schemaVersion", vm.getSchemaVersion());
                        view.put("confirmedType", vm.getConfirmedType());
                        views.add(view);
                    } catch (Exception ignore) {}
                }
            }
            // 若 ES 零命中，且允许自动降级，则回退 SQL（提升体验）
            if ((items == null || items.isEmpty()) && props.getEs().isAutoFallback()) {
                List<VulnerabilityMetadata> sqlItems = vulnService.list(null, enforcedStatus, identifierPrefix, q, languages, severityLevels, severityGe, severityLe, null, null, null, organizationUuid, category, null, null, null, null, sortBy, sortOrder, page, size);
                long sqlTotal = vulnService.count(null, enforcedStatus, identifierPrefix, q, languages, severityLevels, severityGe, severityLe, null, null, null, organizationUuid, category, null, null, null, null);
                List<Map<String, Object>> sqlViews = sqlItems.stream().map(vm -> {
                    Map<String, Object> v = new HashMap<>();
                    v.put("uuid", vm.getUuid());
                    v.put("identifier", vm.getIdentifier());
                    v.put("summary", vm.getSummary());
                    v.put("details", vm.getDetails());
                    v.put("severityNum", vm.getSeverityNum());
                    v.put("modified", vm.getModified());
                    v.put("submitted", vm.getSubmitted());
                    v.put("published", vm.getPublished());
                    v.put("withdrawn", vm.getWithdrawn());
                    v.put("language", vm.getLanguage());
                    v.put("status", vm.getStatus());
                    v.put("schemaVersion", vm.getSchemaVersion());
                    v.put("confirmedType", vm.getConfirmedType());
                    return v; }).toList();
                return new Result(sqlViews, sqlTotal);
            }
            return new Result(views, total);
        } catch (Exception e) {
            log.warn("[ES] search failed: {}", e.getMessage());
            if (props.getEs().isAutoFallback()) {
                List<VulnerabilityMetadata> items = vulnService.list(null, status, identifierPrefix, q, languages, severityLevels, severityGe, severityLe, null, null, null, organizationUuid, category, null, null, null, null, sortBy, sortOrder, page, size);
                long total = vulnService.count(null, status, identifierPrefix, q, languages, severityLevels, severityGe, severityLe, null, null, null, organizationUuid, category, null, null, null, null);
                List<Map<String, Object>> views = items.stream().map(vm -> {
                    Map<String, Object> v = new HashMap<>();
                    v.put("uuid", vm.getUuid());
                    v.put("identifier", vm.getIdentifier());
                    v.put("summary", vm.getSummary());
                    v.put("details", vm.getDetails());
                    v.put("severityNum", vm.getSeverityNum());
                    v.put("modified", vm.getModified());
                    v.put("submitted", vm.getSubmitted());
                    v.put("published", vm.getPublished());
                    v.put("withdrawn", vm.getWithdrawn());
                    v.put("language", vm.getLanguage());
                    v.put("status", vm.getStatus());
                    v.put("schemaVersion", vm.getSchemaVersion());
                    v.put("confirmedType", vm.getConfirmedType());
                    return v; }).toList();
                return new Result(views, total);
            }
            throw new ApiException(1500, "搜索服务不可用");
        }
    }

    public record Result(List<Map<String, Object>> items, long total) {}

    private String currentUserUuid() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }
    private boolean isAdmin() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;
        for (var a : auth.getAuthorities()) { if ("ROLE_ADMIN".equals(a.getAuthority())) return true; }
        return false;
    }
}
