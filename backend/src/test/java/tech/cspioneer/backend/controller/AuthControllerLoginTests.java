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
import tech.cspioneer.backend.service.*;
import tech.cspioneer.backend.enums.UserStatus;
import tech.cspioneer.backend.enums.UserRole;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerLoginTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean VerificationCodeService verificationCodeService;
    @MockBean RegistrationSessionService registrationSessionService;
    @MockBean EmailService emailService;
    @MockBean RateLimiterService rateLimiterService;
    @MockBean UserService userService;
    @MockBean JwtService jwtService;
    @MockBean TokenBlacklistService tokenBlacklistService;

    @Test
    void login_success() throws Exception {
        User u = new User(); u.setUuid("u-uuid"); u.setName("alice"); u.setStatus(UserStatus.ACTIVE); u.setPassword("enc"); u.setRole(UserRole.USER);
        Mockito.when(userService.findByLogin(eq("alice"))).thenReturn(u);
        Mockito.when(userService.verifyPassword(eq(u), eq("pwd"))).thenReturn(true);
        Mockito.when(jwtService.generateAccessToken(eq("u-uuid"), eq("USER"))).thenReturn("AT");
        Mockito.when(jwtService.generateRefreshToken(eq("u-uuid"), eq("USER"), isNull())).thenReturn("RT");

        String body = "{\n  \"login\": \"alice\",\n  \"password\": \"pwd\"\n}";
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessToken").value("AT"))
                .andExpect(jsonPath("$.data.refreshToken").value("RT"));
    }
}
