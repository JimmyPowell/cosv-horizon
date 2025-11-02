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
import tech.cspioneer.backend.service.*;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerLogoutTests {

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
    void logout_success() throws Exception {
        JwtService.TokenInfo info = new JwtService.TokenInfo();
        info.setJti("jti-1");
        info.setSubjectUuid("user-uuid");
        info.setRole("USER");
        info.setIssuedAt(Instant.now());
        info.setExpiresAt(Instant.now().plusSeconds(3600));
        Mockito.when(jwtService.parseRefreshToken(eq("REFRESH_TKN"))).thenReturn(info);

        String body = "{\n  \"refreshToken\": \"REFRESH_TKN\"\n}";
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        Mockito.verify(tokenBlacklistService).blacklistRefreshJti(eq("jti-1"), anyLong());
    }

    @Test
    void logout_invalidToken_returns1008() throws Exception {
        Mockito.when(jwtService.parseRefreshToken(anyString())).thenThrow(new IllegalArgumentException("bad token"));
        String body = "{\n  \"refreshToken\": \"BAD\"\n}";
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1008));
    }
}

