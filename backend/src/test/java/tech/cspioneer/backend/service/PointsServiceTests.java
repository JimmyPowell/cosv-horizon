package tech.cspioneer.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.cspioneer.backend.entity.Organization;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.mapper.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointsServiceTests {
    @Mock UserMapper userMapper;
    @Mock OrganizationMapper organizationMapper;
    @Mock UserPointsLedgerMapper userPointsLedgerMapper;
    @Mock OrgPointsLedgerMapper orgPointsLedgerMapper;

    @InjectMocks PointsService pointsService;

    @Test
    void addUserPoints_idempotent_onlyOnce() {
        User u = new User(); u.setId(1L); u.setUuid("u-uuid"); u.setStatus(tech.cspioneer.backend.enums.UserStatus.ACTIVE);
        when(userMapper.findByUuid(eq("u-uuid"))).thenReturn(u);
        when(userPointsLedgerMapper.existsByIdem(eq(1L), eq("idem-1"))).thenReturn(null).thenReturn(1);

        pointsService.addUserPoints("u-uuid", 10, "REGISTER", "USER", "u-uuid", "idem-1");
        pointsService.addUserPoints("u-uuid", 10, "REGISTER", "USER", "u-uuid", "idem-1");

        verify(userPointsLedgerMapper, times(1)).insert(any());
        verify(userMapper, times(1)).incrementRating(eq(1L), eq(10));
    }

    @Test
    void addOrgPoints_incrementsRating() {
        Organization org = new Organization(); org.setId(2L); org.setUuid("org-uuid");
        when(organizationMapper.findByUuid(eq("org-uuid"))).thenReturn(org);
        when(orgPointsLedgerMapper.existsByIdem(eq(2L), any())).thenReturn(null);

        pointsService.addOrgPoints("org-uuid", 5, "VULN_ACCEPTED", "VULN", "v-uuid", "idem-2");
        verify(orgPointsLedgerMapper, times(1)).insert(any());
        verify(organizationMapper, times(1)).incrementRating(eq(2L), eq(5));
    }
}
