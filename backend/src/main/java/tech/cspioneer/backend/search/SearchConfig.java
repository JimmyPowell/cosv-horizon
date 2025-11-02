package tech.cspioneer.backend.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SearchProperties.class)
public class SearchConfig {

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "search.es", name = "enabled", havingValue = "true")
    public RestClient esLowLevelClient(SearchProperties props) {
        String[] parts = props.getEs().getUris().split(",");
        HttpHost[] hosts = new HttpHost[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String uri = parts[i].trim();
            if (uri.isEmpty()) continue;
            java.net.URI u = java.net.URI.create(uri);
            int port = (u.getPort() > 0) ? u.getPort() : ("https".equalsIgnoreCase(u.getScheme()) ? 443 : 80);
            hosts[i] = new HttpHost(u.getHost(), port, u.getScheme());
        }
        // OpenSearch 2.x 不接受 ES Java Client 默认的 vnd.elasticsearch+json;compatible-with=8
        // 通过设置默认头为 application/json 以兼容 OpenSearch
        Header[] defaultHeaders = new Header[] {
                new BasicHeader("Accept", "application/json"),
                new BasicHeader("Content-Type", "application/json")
        };
        return RestClient.builder(hosts).setDefaultHeaders(defaultHeaders).build();
    }

    // Note: We intentionally avoid creating ElasticsearchClient for OpenSearch compatibility
    // and use low-level RestClient in our code.
}
