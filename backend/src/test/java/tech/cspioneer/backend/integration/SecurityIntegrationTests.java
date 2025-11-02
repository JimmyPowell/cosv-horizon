package tech.cspioneer.backend.integration;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tech.cspioneer.backend.dto.OrgWithRole;
import tech.cspioneer.backend.entity.Organization;
import tech.cspioneer.backend.service.JwtService;
import tech.cspioneer.backend.service.OrganizationService;
import tech.cspioneer.backend.enums.OrganizationStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTests {

    @Autowired MockMvc mockMvc;

    @MockBean JwtService jwtService;
    @MockBean OrganizationService organizationService;

    @Test
    void unauthorized_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/orgs/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void unauthorized_withInvalidToken_returns401() throws Exception {
        Mockito.when(jwtService.parseAccessToken(eq("bad"))).thenThrow(new IllegalArgumentException("bad"));
        mockMvc.perform(get("/orgs/me").header("Authorization", "Bearer bad"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void authorized_withValidToken_returns200() throws Exception {
        JwtService.TokenInfo info = new JwtService.TokenInfo();
        info.setSubjectUuid("user-uuid");
        info.setRole("USER");
        info.setIssuedAt(java.time.Instant.now());
        info.setExpiresAt(java.time.Instant.now().plusSeconds(600));
        Mockito.when(jwtService.parseAccessToken(eq("good"))).thenReturn(info);

        Organization o = new Organization(); o.setUuid("org-uuid"); o.setName("Acme"); o.setDateCreated(LocalDateTime.now()); o.setStatus(OrganizationStatus.ACTIVE);
        OrgWithRole ow = new OrgWithRole(); ow.setOrganization(o); ow.setRole("ADMIN");
        Mockito.when(organizationService.listMyOrgs(eq("user-uuid"))).thenReturn(List.of(ow));

        mockMvc.perform(get("/orgs/me").header("Authorization", "Bearer good"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items[0].uuid").value("org-uuid"));
    }
}
