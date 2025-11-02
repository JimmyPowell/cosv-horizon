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

import java.time.Duration;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerRefreshTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean VerificationCodeService verificationCodeService;
    @MockBean RegistrationSessionService registrationSessionService;
    @MockBean EmailService emailService;
    @MockBean RateLimiterService rateLimiterService;
    @MockBean UserService userService;
    @MockBean JwtService jwtService;
    @MockBean TokenBlacklistService tokenBlacklistService;

    @Test
    void refresh_success_returnsNewTokens_andBlacklistsOld() throws Exception {
        JwtService.TokenInfo info = new JwtService.TokenInfo();
        info.setJti("old-jti");
        info.setSubjectUuid("user-uuid");
        info.setRole("USER");
        info.setIssuedAt(Instant.now().minusSeconds(60));
        info.setExpiresAt(Instant.now().plusSeconds(3600));

        Mockito.when(jwtService.parseRefreshToken(eq("REFRESH_OLD"))).thenReturn(info);
        Mockito.when(tokenBlacklistService.isRefreshBlacklisted(eq("old-jti"))).thenReturn(false);
        Mockito.when(jwtService.generateAccessToken(eq("user-uuid"), eq("USER"))).thenReturn("ACCESS_NEW");
        Mockito.when(jwtService.generateRefreshToken(eq("user-uuid"), eq("USER"), isNull())).thenReturn("REFRESH_NEW");
        Mockito.when(jwtService.getAccessTtl()).thenReturn(Duration.ofMinutes(15));
        Mockito.when(jwtService.getRefreshTtl()).thenReturn(Duration.ofDays(30));

        String body = "{\n  \"refreshToken\": \"REFRESH_OLD\"\n}";

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessToken").value("ACCESS_NEW"))
                .andExpect(jsonPath("$.data.refreshToken").value("REFRESH_NEW"));

        Mockito.verify(tokenBlacklistService).blacklistRefreshJti(eq("old-jti"), anyLong());
    }

    @Test
    void refresh_blacklisted_returnsError() throws Exception {
        JwtService.TokenInfo info = new JwtService.TokenInfo();
        info.setJti("old-jti");
        info.setSubjectUuid("user-uuid");
        info.setRole("USER");
        info.setIssuedAt(Instant.now().minusSeconds(60));
        info.setExpiresAt(Instant.now().plusSeconds(3600));

        Mockito.when(jwtService.parseRefreshToken(eq("REFRESH_OLD"))).thenReturn(info);
        Mockito.when(tokenBlacklistService.isRefreshBlacklisted(eq("old-jti"))).thenReturn(true);

        String body = "{\n  \"refreshToken\": \"REFRESH_OLD\"\n}";

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1008));
    }
}
