package tech.cspioneer.backend.search;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "search")
public class SearchProperties {
    private String engine = "sql"; // sql | es
    private Es es = new Es();

    public String getEngine() { return engine; }
    public void setEngine(String engine) { this.engine = engine; }
    public Es getEs() { return es; }
    public void setEs(Es es) { this.es = es; }

    public static class Es {
        private boolean enabled = false;
        private String uris = "http://localhost:9200";
        private String indexPrefix = "cosv_vulnerabilities";
        private String indexVersion = "v1";
        private String readAlias = "cosv_vulnerabilities_read";
        private String writeAlias = "cosv_vulnerabilities_write";
        private int shards = 1;
        private int replicas = 0;
        private boolean autoBootstrap = true;
        private boolean bootstrapReindex = true;
        private boolean autoFallback = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getUris() { return uris; }
        public void setUris(String uris) { this.uris = uris; }
        public String getIndexPrefix() { return indexPrefix; }
        public void setIndexPrefix(String indexPrefix) { this.indexPrefix = indexPrefix; }
        public String getIndexVersion() { return indexVersion; }
        public void setIndexVersion(String indexVersion) { this.indexVersion = indexVersion; }
        public String getReadAlias() { return readAlias; }
        public void setReadAlias(String readAlias) { this.readAlias = readAlias; }
        public String getWriteAlias() { return writeAlias; }
        public void setWriteAlias(String writeAlias) { this.writeAlias = writeAlias; }
        public int getShards() { return shards; }
        public void setShards(int shards) { this.shards = shards; }
        public int getReplicas() { return replicas; }
        public void setReplicas(int replicas) { this.replicas = replicas; }
        public boolean isAutoBootstrap() { return autoBootstrap; }
        public void setAutoBootstrap(boolean autoBootstrap) { this.autoBootstrap = autoBootstrap; }
        public boolean isBootstrapReindex() { return bootstrapReindex; }
        public void setBootstrapReindex(boolean bootstrapReindex) { this.bootstrapReindex = bootstrapReindex; }
        public boolean isAutoFallback() { return autoFallback; }
        public void setAutoFallback(boolean autoFallback) { this.autoFallback = autoFallback; }

        public String currentIndexName() { return indexPrefix + "_" + indexVersion; }
    }
}

