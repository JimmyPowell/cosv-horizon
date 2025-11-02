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
import tech.cspioneer.backend.entity.OrgPointsLedger;
import tech.cspioneer.backend.entity.UserPointsLedger;
import tech.cspioneer.backend.service.OrganizationService;
import tech.cspioneer.backend.service.PointsService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PointsController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.security.test.context.support.WithMockUser(roles = "USER")
class PointsControllerTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean PointsService pointsService;
    @MockBean OrganizationService organizationService;

    @Test
    void myPoints_success() throws Exception {
        PointsService.Page<UserPointsLedger> p = new PointsService.Page<>(List.of(), 0);
        Mockito.when(pointsService.listUserPoints(eq("u-uuid"), anyInt(), anyInt())).thenReturn(p);
        mockMvc.perform(get("/users/me/points").principal(() -> "u-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void orgPoints_success() throws Exception {
        PointsService.Page<OrgPointsLedger> p = new PointsService.Page<>(List.of(), 0);
        Mockito.when(pointsService.listOrgPoints(eq("u-uuid"), eq("org-uuid"), anyInt(), anyInt())).thenReturn(p);
        mockMvc.perform(get("/orgs/org-uuid/points").principal(() -> "u-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(0));
    }
}

