package tech.cspioneer.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tech.cspioneer.backend.entity.ApiKey;
import tech.cspioneer.backend.enums.ApiKeyStatus;
import tech.cspioneer.backend.service.ApiKeyService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ApiKeyController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.security.test.context.support.WithMockUser(roles = "USER")
class ApiKeyControllerUpdateTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ApiKeyService apiKeyService;

    @Test
    void update_success() throws Exception {
        ApiKey k = new ApiKey();
        k.setUuid("k-uuid");
        k.setKeyPrefix("abcd1234");
        k.setDescription("desc");
        k.setScopes("vuln:read,org:read");
        k.setStatus(ApiKeyStatus.ACTIVE);
        k.setExpireTime(LocalDateTime.parse("2030-01-01T00:00:00"));

        Mockito.when(apiKeyService.update(eq("u-uuid"), eq("k-uuid"), any(), any(), any())).thenReturn(k);

        String body = "{\n  \"description\": \"desc\",\n  \"scopes\": [\"vuln:read\", \"org:read\"],\n  \"expireTime\": \"2030-01-01T00:00:00\"\n}";

        mockMvc.perform(patch("/api-keys/k-uuid")
                        .principal(() -> "u-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.apiKey.uuid").value("k-uuid"))
                .andExpect(jsonPath("$.data.apiKey.scopes").value("vuln:read,org:read"));
    }
}

