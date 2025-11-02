package tech.cspioneer.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger配置类
 * 用于配置API文档生成
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("COSV Horizon API")
                        .description("COSV Horizon（Common Open Source Vulnerability）系统API文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("COSV Horizon Team")
                                .email("support@cosv-horizon.com")
                                .url("https://github.com/JimmyPowell/cosv-horizon"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        // JWT Bearer
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Use Authorization: Bearer {access_token}")
                        )
                        // API Key via X-API-Key header
                        .addSecuritySchemes("apiKeyAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-API-Key")
                                        .description("Provide full API key, e.g. cosv_<prefix>_<secret>")
                        )
                        // Alternative: API Key via Authorization header (value must start with 'ApiKey ')
                        .addSecuritySchemes("apiKeyAuthAlt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")
                                        .description("Use Authorization: ApiKey {cosv_<prefix>_<secret>}")
                        )
                )
                // Global security: support both JWT and API Key
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth")
                        .addList("apiKeyAuth")
                        .addList("apiKeyAuthAlt")
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("开发环境"),
                        new Server()
                                .url("https://api.cosv-horizon.com")
                                .description("生产环境")
                ));
    }
}
