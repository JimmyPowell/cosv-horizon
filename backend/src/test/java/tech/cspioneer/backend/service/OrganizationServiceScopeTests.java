package tech.cspioneer.backend.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tech.cspioneer.backend.common.ApiException;
import tech.cspioneer.backend.entity.Organization;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.mapper.*;
import tech.cspioneer.backend.security.ApiKeyContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceScopeTests {

    @Mock OrganizationMapper organizationMapper;
    @Mock LnkUserOrganizationMapper lnkMapper;
    @Mock UserMapper userMapper;
    @Mock NotificationMapper notificationMapper;

    @InjectMocks OrganizationService service;

    @BeforeEach
    void setupAuth() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user-uuid", null);
        ApiKeyContext ctx = new ApiKeyContext();
        ctx.setSubjectType("ORG");
        ctx.setOrgUuid("org-A");
        auth.setDetails(ctx);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clear() { SecurityContextHolder.clearContext(); }

    @Test
    void updateBasic_crossOrg_forbidden() {
        Organization org = new Organization(); org.setId(1L); org.setUuid("org-B");
        when(organizationMapper.findByUuid(eq("org-B"))).thenReturn(org);
        User u = new User(); u.setId(2L); u.setUuid("user-uuid");
        when(userMapper.findByUuid(eq("user-uuid"))).thenReturn(u);
        when(lnkMapper.findByOrgIdAndUserId(eq(1L), eq(2L))).thenReturn(null);
        ApiException ex = assertThrows(ApiException.class, () -> service.updateBasic("user-uuid", "org-B", null, null, null, null, null, null, null));
        assertEquals(1012, ex.getCode());
    }
}

