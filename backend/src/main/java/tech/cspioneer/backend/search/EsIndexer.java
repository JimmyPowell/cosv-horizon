package tech.cspioneer.backend.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tech.cspioneer.backend.entity.Organization;
import tech.cspioneer.backend.entity.VulnerabilityMetadata;
import tech.cspioneer.backend.mapper.CategoryMapper;
import tech.cspioneer.backend.mapper.OrganizationMapper;
import tech.cspioneer.backend.mapper.TagMapper;
import tech.cspioneer.backend.mapper.VulnerabilityMetadataMapper;

import java.util.*;

@Component
@Slf4j
@ConditionalOnProperty(prefix = "search.es", name = "enabled", havingValue = "true")
public class EsIndexer {
    private final RestClient es;
    private final SearchProperties props;
    private final VulnerabilityMetadataMapper vmMapper;
    private final TagMapper tagMapper;
    private final CategoryMapper categoryMapper;
    private final OrganizationMapper organizationMapper;

    public EsIndexer(RestClient es, SearchProperties props, VulnerabilityMetadataMapper vmMapper, TagMapper tagMapper, CategoryMapper categoryMapper, OrganizationMapper organizationMapper) {
        this.es = es; this.props = props; this.vmMapper = vmMapper; this.tagMapper = tagMapper; this.categoryMapper = categoryMapper; this.organizationMapper = organizationMapper;
    }

    public int reindexAll() {
        int pageSize = 500;
        long lastId = 0;
        int total = 0;
        List<VulnerabilityMetadata> batch;
        while (true) {
            batch = vmMapper.scanForIndexing(lastId, pageSize);
            if (batch == null || batch.isEmpty()) break;
            StringBuilder ndjson = new StringBuilder();
            for (var vm : batch) {
                Map<String, Object> doc = toDoc(vm);
                Map<String, Object> action = Map.of("index", Map.of("_index", props.getEs().getWriteAlias(), "_id", vm.getUuid()));
                try {
                    ndjson.append(OBJECT_MAPPER.writeValueAsString(action)).append('\n');
                    ndjson.append(OBJECT_MAPPER.writeValueAsString(doc)).append('\n');
                } catch (com.fasterxml.jackson.core.JsonProcessingException jpe) {
                    log.warn("[ES] serialize failed uuid={} : {}", vm.getUuid(), jpe.getMessage());
                    continue;
                }
                if (vm.getId() != null && vm.getId() > lastId) lastId = vm.getId();
                total++;
            }
            try {
                Request req = new Request("POST", "/_bulk");
                req.setEntity(new NStringEntity(ndjson.toString(), ContentType.create("application/x-ndjson")));
                es.performRequest(req);
            } catch (Exception e) {
                log.warn("[ES] bulk failed: {}", e.getMessage());
            }
            if (batch.size() < pageSize) break;
        }
        try { Request r = new Request("POST", "/" + props.getEs().getReadAlias() + "/_refresh"); es.performRequest(r);} catch (Exception ignore) {}
        return total;
    }

    public void indexOne(String uuid) {
        try {
            VulnerabilityMetadata vm = vmMapper.findByUuid(uuid);
            if (vm == null) return;
            Map<String, Object> doc = toDoc(vm);
            Request req = new Request("PUT", "/" + props.getEs().getWriteAlias() + "/_doc/" + uuid);
            req.setEntity(new NStringEntity(OBJECT_MAPPER.writeValueAsString(doc), ContentType.APPLICATION_JSON));
            es.performRequest(req);
            try { es.performRequest(new Request("POST", "/" + props.getEs().getReadAlias() + "/_refresh")); } catch (Exception ignore) {}
        } catch (Exception e) {
            log.warn("[ES] indexOne failed uuid={} : {}", uuid, e.getMessage());
        }
    }

    public void deleteOne(String uuid) {
        try {
            Request req = new Request("DELETE", "/" + props.getEs().getWriteAlias() + "/_doc/" + uuid);
            es.performRequest(req);
            try { es.performRequest(new Request("POST", "/" + props.getEs().getReadAlias() + "/_refresh")); } catch (Exception ignore) {}
        } catch (Exception e) {
            log.warn("[ES] deleteOne failed uuid={} : {}", uuid, e.getMessage());
        }
    }

    private Map<String, Object> toDoc(VulnerabilityMetadata vm) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("uuid", vm.getUuid());
        m.put("identifier", vm.getIdentifier());
        m.put("summary", vm.getSummary());
        m.put("details", vm.getDetails());
        m.put("language", vm.getLanguage() == null ? null : vm.getLanguage().name());
        m.put("severityNum", vm.getSeverityNum());
        m.put("status", vm.getStatus() == null ? null : vm.getStatus().name());
        m.put("modified", vm.getModified());
        m.put("submitted", vm.getSubmitted());
        // category
        if (vm.getCategoryId() != null) {
            var c = categoryMapper.findById(vm.getCategoryId());
            if (c != null) m.put("categoryCode", c.getCode());
        }
        // tags
        if (vm.getId() != null) {
            var tags = tagMapper.listByVulnerabilityId(vm.getId());
            if (tags != null) m.put("tagCodes", tags.stream().map(t -> t.getCode()).filter(Objects::nonNull).toList());
        }
        // aliases
        // keep lightweight: vmMapper will include alias in DB LIKE; here skipping full fetch to reduce queries
        // organization
        if (vm.getOrganizationId() != null) {
            Organization o = organizationMapper.findById(vm.getOrganizationId());
            if (o != null) {
                m.put("organizationUuid", o.getUuid());
                m.put("orgIsPublic", o.getIsPublic());
                m.put("orgStatus", o.getStatus() == null ? null : o.getStatus().name());
                m.put("orgIsVerified", o.getIsVerified());
            }
        }
        return m;
    }
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
}
