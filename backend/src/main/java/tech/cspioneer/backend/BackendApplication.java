package tech.cspioneer.backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@SpringBootApplication
@Slf4j
public class BackendApplication {

    private final Environment env;

    public BackendApplication(Environment env) {
        this.env = env;
    }

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        String app = env.getProperty("spring.application.name", "backend");
        String port = env.getProperty("local.server.port", env.getProperty("server.port", "8080"));
        String profiles = String.join(",", env.getActiveProfiles());
        String ds = sanitizeDatasource(env.getProperty("spring.datasource.url", ""));
        String redisHost = env.getProperty("spring.redis.host", "");
        String redisPort = env.getProperty("spring.redis.port", "");
        String mailMock = env.getProperty("mail.mock", "false");
        log.info("app started name={} port={} profiles={} datasource={} redis={}:{} mail.mock={}",
                app, port, profiles.isEmpty() ? "default" : profiles, ds, redisHost, redisPort, mailMock);
    }

    private String sanitizeDatasource(String url) {
        if (url == null) return "";
        // strip credentials if present: jdbc:mysql://user:pass@host:port/db?...
        try {
            int schemeIdx = url.indexOf("//");
            if (schemeIdx > 0) {
                String right = url.substring(schemeIdx + 2);
                int at = right.indexOf('@');
                if (at > 0) {
                    return url.substring(0, schemeIdx + 2) + right.substring(at + 1);
                }
            }
            return url;
        } catch (Exception ignored) {
            return url;
        }
    }
}
