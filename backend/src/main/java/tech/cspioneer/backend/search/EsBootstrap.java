package tech.cspioneer.backend.search;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(prefix = "search.es", name = "enabled", havingValue = "true")
public class EsBootstrap {
    private final RestClient es;
    private final SearchProperties props;
    private final EsIndexer indexer;

    public EsBootstrap(RestClient es, SearchProperties props, EsIndexer indexer) {
        this.es = es; this.props = props; this.indexer = indexer;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        if (!props.getEs().isAutoBootstrap()) return;
        String index = props.getEs().currentIndexName();
        try {
            if (!indexExists(index)) {
                log.info("[ES] creating index {} shards={} replicas={}", index, props.getEs().getShards(), props.getEs().getReplicas());
                String body = "{" +
                        "\"settings\":{\"number_of_shards\":" + props.getEs().getShards() + ",\"number_of_replicas\":" + props.getEs().getReplicas() + "}," +
                        "\"mappings\":{\"properties\":{" +
                        "\"uuid\":{\"type\":\"keyword\"}," +
                        "\"identifier\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}}," +
                        "\"summary\":{\"type\":\"text\"}," +
                        "\"details\":{\"type\":\"text\"}," +
                        "\"aliases\":{\"type\":\"keyword\"}," +
                        "\"tagCodes\":{\"type\":\"keyword\"}," +
                        "\"categoryCode\":{\"type\":\"keyword\"}," +
                        "\"language\":{\"type\":\"keyword\"}," +
                        "\"severityNum\":{\"type\":\"float\"}," +
                        "\"status\":{\"type\":\"keyword\"}," +
                        "\"modified\":{\"type\":\"date\"}," +
                        "\"submitted\":{\"type\":\"date\"}," +
                        "\"organizationUuid\":{\"type\":\"keyword\"}," +
                        "\"orgIsPublic\":{\"type\":\"boolean\"}," +
                        "\"orgStatus\":{\"type\":\"keyword\"}," +
                        "\"orgIsVerified\":{\"type\":\"boolean\"}" +
                        "}}" +
                        "}";
                Request req = new Request("PUT", "/" + index);
                req.setJsonEntity(body);
                es.performRequest(req);
            }
            // ensure aliases
            ensureAlias(index, props.getEs().getReadAlias());
            ensureAlias(index, props.getEs().getWriteAlias());

            if (props.getEs().isBootstrapReindex()) {
                log.info("[ES] bootstrap reindex start");
                int n = indexer.reindexAll();
                log.info("[ES] bootstrap reindex done count={}", n);
            }
        } catch (Exception e) {
            log.warn("[ES] bootstrap failed: {}", e.getMessage());
        }
    }

    private boolean indexExists(String index) throws Exception {
        Request head = new Request("HEAD", "/" + index);
        Response resp = es.performRequest(head);
        int code = resp.getStatusLine().getStatusCode();
        return code == 200;
    }

    private void ensureAlias(String index, String alias) throws Exception {
        Request head = new Request("HEAD", "/_alias/" + alias);
        Response resp = es.performRequest(head);
        int code = resp.getStatusLine().getStatusCode();
        if (code == 404) {
            String body = "{\"actions\":[{\"add\":{\"index\":\"" + index + "\",\"alias\":\"" + alias + "\"}}]}";
            Request req = new Request("POST", "/_aliases");
            req.setJsonEntity(body);
            es.performRequest(req);
            log.info("[ES] alias {} -> {} created", alias, index);
        }
    }
}
