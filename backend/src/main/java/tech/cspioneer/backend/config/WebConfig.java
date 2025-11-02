package tech.cspioneer.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;
    @Value("${cors.allowed-methods:GET,POST,PATCH,DELETE,OPTIONS}")
    private String allowedMethods;
    @Value("${cors.allowed-headers:Authorization,Content-Type,X-API-Key}")
    private String allowedHeaders;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        String[] methods = allowedMethods.split(",");
        String[] headers = allowedHeaders.split(",");
        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods(methods)
                .allowedHeaders(headers)
                .allowCredentials(false);
    }
}

