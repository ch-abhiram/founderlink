package com.team_service.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.team_service.DTO.InviteMemberRequest;
import com.team_service.DTO.UpdateInviteStatusRequest;
import com.team_service.Entity.TeamMember;
import com.team_service.Service.TeamService;

@ExtendWith(MockitoExtension.class)
class TeamControllerTest {

    @Mock
    private TeamService service;

    private TeamController controller;
    private TeamMember member;

    @BeforeEach
    void setUp() {
        controller = new TeamController(service);
        member = member();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@test.com", null));
    }

    @Test
    void inviteMemberReturnsCreatedDto() {
        InviteMemberRequest request = new InviteMemberRequest();
        when(service.inviteMember(request)).thenReturn(member);

        var response = controller.inviteMember(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("/team/invite/5", response.getHeaders().getLocation().toString());
        assertEquals("CTO", response.getBody().getRole());
    }

    @Test
    void updateAndListEndpointsMapMembers() {
        UpdateInviteStatusRequest request = new UpdateInviteStatusRequest();
        request.setStatus("ACCEPTED");
        member.setStatus("ACCEPTED");
        when(service.updateInviteStatus(5L, "ACCEPTED")).thenReturn(member);
        when(service.getStartupTeam(7L)).thenReturn(List.of(member));
        when(service.getMyInvites("user@test.com")).thenReturn(List.of(member));

        assertEquals("ACCEPTED", controller.updateInviteStatus(5L, request).getBody().getStatus());
        assertEquals(7L, controller.getStartupTeam(7L).getBody().get(0).getStartupId());
        assertEquals("user@test.com", controller.getMyInvites().getBody().get(0).getUserEmail());
    }

    @Test
    void removeMemberReturnsNoContent() {
        assertEquals(HttpStatus.NO_CONTENT, controller.removeMember(5L).getStatusCode());
        verify(service).removeMember(5L);
    }

    private TeamMember member() {
        TeamMember value = new TeamMember();
        value.setId(5L);
        value.setStartupId(7L);
        value.setUserEmail("user@test.com");
        value.setRole("CTO");
        value.setStatus("PENDING");
        value.setEquityPercentage(4.5);
        value.setPermissionLevel("MANAGER");
        value.setCreatedAt(LocalDateTime.now());
        return value;
    }
}
