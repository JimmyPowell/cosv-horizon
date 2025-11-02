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
import tech.cspioneer.backend.service.ApiKeyService;
import tech.cspioneer.backend.enums.ApiKeyStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ApiKeyController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.security.test.context.support.WithMockUser(roles = "USER")
class ApiKeyControllerTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ApiKeyService apiKeyService;

    @Test
    void create_returnsPlaintextOnce() throws Exception {
        ApiKey meta = new ApiKey();
        meta.setUuid("k-uuid"); meta.setKeyPrefix("abcdEF12"); meta.setStatus(ApiKeyStatus.ACTIVE); meta.setScopes("vuln:read,vuln:write"); meta.setExpireTime(LocalDateTime.now().plusDays(7));
        ApiKeyService.CreatedKey created = new ApiKeyService.CreatedKey(meta, "cosv_abcdEF12_secret");
        Mockito.when(apiKeyService.create(eq("u-uuid"), isNull(), isNull(), isNull(), isNull())).thenReturn(created);

        String body = "{}";
        mockMvc.perform(post("/api-keys").principal(() -> "u-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.uuid").value("k-uuid"))
                .andExpect(jsonPath("$.data.apiKey").value("cosv_abcdEF12_secret"));
    }

    @Test
    void list_success() throws Exception {
        ApiKey k = new ApiKey(); k.setUuid("k-uuid"); k.setKeyPrefix("abcd"); k.setStatus(ApiKeyStatus.ACTIVE);
        Mockito.when(apiKeyService.listMine(eq("u-uuid"))).thenReturn(List.of(k));
        mockMvc.perform(get("/api-keys").principal(() -> "u-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items[0].uuid").value("k-uuid"));
    }

    @Test
    void usage_success() throws Exception {
        ApiKeyService.UsagePage page = new ApiKeyService.UsagePage(List.of(), 0);
        Mockito.when(apiKeyService.usage(eq("u-uuid"), eq("k-uuid"), anyInt(), anyInt(), any(), any())).thenReturn(page);
        mockMvc.perform(get("/api-keys/k-uuid/usage").principal(() -> "u-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(0));
    }
}
