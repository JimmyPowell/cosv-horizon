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
import tech.cspioneer.backend.entity.Notification;
import tech.cspioneer.backend.service.NotificationService;
import tech.cspioneer.backend.enums.NotificationType;
import tech.cspioneer.backend.enums.NotificationStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.security.test.context.support.WithMockUser(roles = "USER")
class NotificationControllerTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean NotificationService notificationService;

    @Test
    void list_success() throws Exception {
        Notification n = new Notification();
        n.setUuid("n-uuid"); n.setType(NotificationType.SYSTEM_NOTICE); n.setContent("hi"); n.setIsRead(false); n.setCreateTime(LocalDateTime.now()); n.setStatus(NotificationStatus.ACTIVE);
        Mockito.when(notificationService.list(eq("u-uuid"), isNull(), isNull(), isNull(), anyInt(), anyInt())).thenReturn(List.of(n));
        Mockito.when(notificationService.count(eq("u-uuid"), isNull(), isNull(), isNull())).thenReturn(1L);
        mockMvc.perform(get("/notifications").principal(() -> "u-uuid").param("withTotal", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items[0].uuid").value("n-uuid"))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void markRead_success() throws Exception {
        String body = "{\n  \"uuid\": \"n-uuid\"\n}";
        mockMvc.perform(post("/notifications/read").principal(() -> "u-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        Mockito.verify(notificationService).markRead(eq("u-uuid"), eq("n-uuid"));
    }
}
