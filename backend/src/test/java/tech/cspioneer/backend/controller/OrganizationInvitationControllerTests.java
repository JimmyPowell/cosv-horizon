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
import tech.cspioneer.backend.dto.OrgMemberView;
import tech.cspioneer.backend.service.OrganizationService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrganizationController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.security.test.context.support.WithMockUser(roles = "USER")
class OrganizationInvitationControllerTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean OrganizationService organizationService;

    @Test
    void members_list_success() throws Exception {
        OrgMemberView m = new OrgMemberView();
        m.setUuid("user-uuid");
        m.setName("alice");
        m.setEmail("a@b.c");
        m.setRole("ADMIN");
        Mockito.when(organizationService.listMembers(eq("principal-uuid"), eq("org-uuid"))).thenReturn(List.of(m));

        mockMvc.perform(get("/orgs/org-uuid/members").principal(() -> "principal-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items[0].uuid").value("user-uuid"))
                .andExpect(jsonPath("$.data.items[0].role").value("ADMIN"));
    }

    @Test
    void invite_success() throws Exception {
        String body = "{\n  \"loginOrEmail\": \"bob\"\n}";
        mockMvc.perform(post("/orgs/org-uuid/invitations/invite")
                        .principal(() -> "principal-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        Mockito.verify(organizationService).sendInvite(eq("principal-uuid"), eq("org-uuid"), eq("bob"));
    }

    @Test
    void accept_invite_success() throws Exception {
        mockMvc.perform(post("/orgs/invitations/inv-uuid/accept").principal(() -> "principal-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        Mockito.verify(organizationService).acceptInvitation(eq("principal-uuid"), eq("inv-uuid"));
    }

    @Test
    void reject_invite_success() throws Exception {
        mockMvc.perform(post("/orgs/invitations/inv-uuid/reject").principal(() -> "principal-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        Mockito.verify(organizationService).rejectInvitation(eq("principal-uuid"), eq("inv-uuid"));
    }
}
