package tech.cspioneer.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import tech.cspioneer.backend.dto.OrgWithRole;
import tech.cspioneer.backend.entity.Organization;
import tech.cspioneer.backend.service.OrganizationService;
import tech.cspioneer.backend.enums.OrganizationStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrganizationController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.security.test.context.support.WithMockUser(roles = "USER")
class OrganizationControllerTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean OrganizationService organizationService;

    @Test
    void create_org_success() throws Exception {
        Organization o = new Organization();
        o.setUuid("org-uuid");
        o.setName("Acme");
        o.setStatus(OrganizationStatus.ACTIVE);
        o.setDateCreated(LocalDateTime.now());
        Mockito.when(organizationService.createOrganization(anyString(), eq("Acme"), isNull(), isNull())).thenReturn(o);

        String body = "{\n  \"name\": \"Acme\"\n}";
        mockMvc.perform(post("/orgs").principal(() -> "user-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.organization.uuid").value("org-uuid"))
                .andExpect(jsonPath("$.data.organization.name").value("Acme"));
    }

    @Test
    void my_orgs_success() throws Exception {
        Organization o = new Organization();
        o.setUuid("org-uuid");
        o.setName("Acme");
        OrgWithRole ow = new OrgWithRole();
        ow.setOrganization(o);
        ow.setRole("ADMIN");
        Mockito.when(organizationService.listMyOrgs(anyString())).thenReturn(List.of(ow));

        mockMvc.perform(get("/orgs/me").principal(() -> "user-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items[0].uuid").value("org-uuid"))
                .andExpect(jsonPath("$.data.items[0].role").value("ADMIN"));
    }
}
