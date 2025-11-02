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
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.enums.UserRole;
import tech.cspioneer.backend.enums.UserStatus;
import tech.cspioneer.backend.service.UserService;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.security.test.context.support.WithMockUser(roles = "USER")
class UserControllerTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean UserService userService;

    @Test
    void me_success() throws Exception {
        User u = new User();
        u.setUuid("u-uuid");
        u.setName("alice");
        u.setEmail("alice@example.com");
        u.setCompany("ACME");
        u.setRole(UserRole.USER);
        u.setStatus(UserStatus.ACTIVE);
        Mockito.when(userService.requireUserByUuid(eq("u-uuid"))).thenReturn(u);

        mockMvc.perform(get("/users/me").principal(() -> "u-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.user.uuid").value("u-uuid"))
                .andExpect(jsonPath("$.data.user.name").value("alice"))
                .andExpect(jsonPath("$.data.user.company").value("ACME"));
    }

    @Test
    void update_success() throws Exception {
        User u = new User();
        u.setUuid("u-uuid");
        u.setName("alice");
        u.setEmail("alice@example.com");
        u.setCompany("NewCo");
        u.setWebsite("https://example.com");
        u.setRole(UserRole.USER);
        u.setStatus(UserStatus.ACTIVE);
        Mockito.when(userService.updateProfile(eq("u-uuid"), eq(null), eq("NewCo"), eq(null), eq(null), eq("https://example.com"), eq(null), eq(null)))
                .thenReturn(u);

        String body = "{\n  \"company\": \"NewCo\",\n  \"website\": \"https://example.com\"\n}";
        mockMvc.perform(patch("/users/me")
                        .principal(() -> "u-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.user.company").value("NewCo"))
                .andExpect(jsonPath("$.data.user.website").value("https://example.com"));
    }
}

