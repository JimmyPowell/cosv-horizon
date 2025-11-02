package tech.cspioneer.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.cspioneer.backend.common.ApiException;
import tech.cspioneer.backend.dto.OrgMemberView;
import tech.cspioneer.backend.dto.OrgWithRole;
import tech.cspioneer.backend.entity.LnkUserOrganization;
import tech.cspioneer.backend.entity.Notification;
import tech.cspioneer.backend.entity.Organization;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.mapper.LnkUserOrganizationMapper;
import tech.cspioneer.backend.mapper.NotificationMapper;
import tech.cspioneer.backend.mapper.OrganizationMapper;
import tech.cspioneer.backend.mapper.UserMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTests {

    @Mock OrganizationMapper organizationMapper;
    @Mock LnkUserOrganizationMapper lnkMapper;
    @Mock UserMapper userMapper;
    @Mock NotificationMapper notificationMapper;

    @InjectMocks OrganizationService service;

    User actor;
    Organization org;

    @BeforeEach
    void setup() {
        actor = new User();
        actor.setId(10L);
        actor.setUuid("actor-uuid");
        actor.setStatus(tech.cspioneer.backend.enums.UserStatus.ACTIVE);

        org = new Organization();
        org.setId(1L);
        org.setUuid("org-uuid");
        org.setName("Acme");
        org.setStatus(tech.cspioneer.backend.enums.OrganizationStatus.ACTIVE);
    }

    @Test
    void createOrganization_success() {
        when(organizationMapper.findByName(eq("Acme"))).thenReturn(null);
        when(userMapper.findByUuid(eq("actor-uuid"))).thenReturn(actor);
        doAnswer(inv -> { Organization o = inv.getArgument(0); o.setId(1L); return 1; })
                .when(organizationMapper).insert(any(Organization.class));

        Organization created = service.createOrganization("actor-uuid", "Acme", null, null);
        assertNotNull(created.getUuid());
        verify(lnkMapper).insert(argThat(l -> l.getOrganizationId().equals(1L) && l.getUserId().equals(10L) && tech.cspioneer.backend.enums.OrganizationRole.ADMIN.equals(l.getRole())));
    }

    @Test
    void updateBasic_nameConflict_throws() {
        when(organizationMapper.findByUuid(eq("org-uuid"))).thenReturn(org);
        when(userMapper.findByUuid(eq("actor-uuid"))).thenReturn(actor);
        when(lnkMapper.findByOrgIdAndUserId(eq(1L), eq(10L))).thenReturn(link("ADMIN"));
        Organization other = new Organization(); other.setUuid("other"); other.setId(2L); other.setName("New");
        when(organizationMapper.findByName(eq("New"))).thenReturn(other);
        ApiException ex = assertThrows(ApiException.class, () -> service.updateBasic("actor-uuid", "org-uuid", "New", null, null, null, null, null, null));
        assertEquals(1006, ex.getCode());
    }

    @Test
    void listMembers_requiresMembership() {
        when(organizationMapper.findByUuid(eq("org-uuid"))).thenReturn(org);
        when(userMapper.findByUuid(eq("actor-uuid"))).thenReturn(actor);
        when(lnkMapper.findByOrgIdAndUserId(eq(1L), eq(10L))).thenReturn(null);
        ApiException ex = assertThrows(ApiException.class, () -> service.listMembers("actor-uuid", "org-uuid"));
        assertEquals(403, ex.getCode());
    }

    @Test
    void addMember_direct_success() {
        when(organizationMapper.findByUuid(eq("org-uuid"))).thenReturn(org);
        when(userMapper.findByUuid(eq("actor-uuid"))).thenReturn(actor);
        when(lnkMapper.findByOrgIdAndUserId(eq(1L), eq(10L))).thenReturn(link("ADMIN"));
        User target = new User(); target.setId(20L); target.setUuid("target"); target.setStatus(tech.cspioneer.backend.enums.UserStatus.ACTIVE);
        when(userMapper.findByEmailOrName(eq("bob"))).thenReturn(target);
        when(lnkMapper.findByOrgIdAndUserId(eq(1L), eq(20L))).thenReturn(null);

        service.addMember("actor-uuid", "org-uuid", "bob", "MEMBER");
        verify(lnkMapper).insert(argThat(l -> l.getOrganizationId().equals(1L) && l.getUserId().equals(20L) && tech.cspioneer.backend.enums.OrganizationRole.MEMBER.equals(l.getRole())));
    }

    @Test
    void sendInvite_dedup_throws() {
        when(organizationMapper.findByUuid(eq("org-uuid"))).thenReturn(org);
        when(userMapper.findByUuid(eq("actor-uuid"))).thenReturn(actor);
        when(lnkMapper.findByOrgIdAndUserId(eq(1L), eq(10L))).thenReturn(link("ADMIN"));
        User target = new User(); target.setId(20L); target.setUuid("target"); target.setStatus(tech.cspioneer.backend.enums.UserStatus.ACTIVE);
        when(userMapper.findByEmailOrName(eq("bob"))).thenReturn(target);
        when(lnkMapper.findByOrgIdAndUserId(eq(1L), eq(20L))).thenReturn(null);
        when(notificationMapper.findActiveOrgInviteId(eq(1L), eq(20L))).thenReturn(99L);
        ApiException ex = assertThrows(ApiException.class, () -> service.sendInvite("actor-uuid", "org-uuid", "bob"));
        assertEquals(1006, ex.getCode());
    }

    @Test
    void acceptInvitation_createsMembership_andMarksAccepted() {
        User invitee = new User(); invitee.setId(30L); invitee.setUuid("invitee"); invitee.setStatus(tech.cspioneer.backend.enums.UserStatus.ACTIVE);
        when(userMapper.findByUuid(eq("invitee"))).thenReturn(invitee);
        Notification n = new Notification();
        n.setUuid("inv-uuid"); n.setType(tech.cspioneer.backend.enums.NotificationType.ORGANIZATION_INVITE); n.setStatus(tech.cspioneer.backend.enums.NotificationStatus.ACTIVE); n.setUserId(30L); n.setTargetId(1L);
        when(notificationMapper.findByUuid(eq("inv-uuid"))).thenReturn(n);
        when(organizationMapper.findById(eq(1L))).thenReturn(org);
        when(lnkMapper.findByOrgIdAndUserId(eq(1L), eq(30L))).thenReturn(null);

        service.acceptInvitation("invitee", "inv-uuid");
        verify(lnkMapper).insert(argThat(l -> l.getOrganizationId().equals(1L) && l.getUserId().equals(30L) && tech.cspioneer.backend.enums.OrganizationRole.MEMBER.equals(l.getRole())));
        verify(notificationMapper).updateStatus(eq("inv-uuid"), eq(tech.cspioneer.backend.enums.NotificationStatus.ACCEPTED), eq(true));
    }

    @Test
    void rejectInvitation_marksRejected() {
        User invitee = new User(); invitee.setId(30L); invitee.setUuid("invitee"); invitee.setStatus(tech.cspioneer.backend.enums.UserStatus.ACTIVE);
        when(userMapper.findByUuid(eq("invitee"))).thenReturn(invitee);
        Notification n = new Notification();
        n.setUuid("inv-uuid"); n.setType(tech.cspioneer.backend.enums.NotificationType.ORGANIZATION_INVITE); n.setStatus(tech.cspioneer.backend.enums.NotificationStatus.ACTIVE); n.setUserId(30L); n.setTargetId(1L);
        when(notificationMapper.findByUuid(eq("inv-uuid"))).thenReturn(n);

        service.rejectInvitation("invitee", "inv-uuid");
        verify(notificationMapper).updateStatus(eq("inv-uuid"), eq(tech.cspioneer.backend.enums.NotificationStatus.REJECTED), eq(true));
    }

    @Test
    void changeMemberRole_notFound_throws404() {
        when(organizationMapper.findByUuid(eq("org-uuid"))).thenReturn(org);
        when(userMapper.findByUuid(eq("actor-uuid"))).thenReturn(actor);
        when(lnkMapper.findByOrgIdAndUserId(eq(1L), eq(10L))).thenReturn(link("ADMIN"));
        User member = new User(); member.setId(40L); member.setUuid("member"); member.setStatus(tech.cspioneer.backend.enums.UserStatus.ACTIVE);
        when(userMapper.findByUuid(eq("member"))).thenReturn(member);
        when(lnkMapper.updateRole(eq(1L), eq(40L), eq(tech.cspioneer.backend.enums.OrganizationRole.VIEWER))).thenReturn(0);
        ApiException ex = assertThrows(ApiException.class, () -> service.changeMemberRole("actor-uuid", "org-uuid", "member", "VIEWER"));
        assertEquals(404, ex.getCode());
    }

    @Test
    void removeMember_notFound_throws404() {
        when(organizationMapper.findByUuid(eq("org-uuid"))).thenReturn(org);
        when(userMapper.findByUuid(eq("actor-uuid"))).thenReturn(actor);
        when(lnkMapper.findByOrgIdAndUserId(eq(1L), eq(10L))).thenReturn(link("ADMIN"));
        User member = new User(); member.setId(40L); member.setUuid("member"); member.setStatus(tech.cspioneer.backend.enums.UserStatus.ACTIVE);
        when(userMapper.findByUuid(eq("member"))).thenReturn(member);
        when(lnkMapper.delete(eq(1L), eq(40L))).thenReturn(0);
        ApiException ex = assertThrows(ApiException.class, () -> service.removeMember("actor-uuid", "org-uuid", "member"));
        assertEquals(404, ex.getCode());
    }

    private LnkUserOrganization link(String role) {
        LnkUserOrganization l = new LnkUserOrganization();
        l.setId(1L); l.setUuid("l"); l.setOrganizationId(1L); l.setUserId(10L); l.setRole(tech.cspioneer.backend.enums.OrganizationRole.valueOf(role));
        return l;
    }
}
