package com.team_service.Service;

import com.team_service.Config.RabbitConfig;
import com.team_service.DTO.InviteMemberRequest;
import com.team_service.DTO.StartupDto;
import com.team_service.DTO.UserDto;
import com.team_service.Entity.TeamMember;
import com.team_service.Feign.StartupClient;
import com.team_service.Feign.UserClient;
import com.team_service.Repository.TeamMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamMemberRepository repository;
    @Mock
    private StartupClient startupClient;
    @Mock
    private UserClient userClient;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private TeamService teamService;

    private InviteMemberRequest inviteRequest;
    private StartupDto startupDto;
    private UserDto userDto;
    private TeamMember member;

    @BeforeEach
    void setUp() {
        inviteRequest = new InviteMemberRequest();
        inviteRequest.setStartupId(1L);
        inviteRequest.setUserEmail("user@test.com");
        inviteRequest.setRole("EMPLOYEE");
        inviteRequest.setEquityPercentage(15.0);
        inviteRequest.setPermissionLevel("ADMIN");

        startupDto = new StartupDto();
        startupDto.setId(1L);
        startupDto.setName("Test Startup");
        startupDto.setFounderEmail("founder@test.com");

        userDto = new UserDto();
        userDto.setId(2L);
        userDto.setEmail("user@test.com");
        userDto.setName("Test User");

        member = new TeamMember();
        member.setId(10L);
        member.setStartupId(1L);
        member.setUserEmail("user@test.com");
        member.setRole("EMPLOYEE");
        member.setStatus("PENDING");
        member.setEquityPercentage(15.0);
        member.setPermissionLevel("ADMIN");
    }

    private void setupSecurityContext(String email) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                email, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testInviteMember_Success() {
        setupSecurityContext("founder@test.com");
        
        when(startupClient.getStartup(1L)).thenReturn(startupDto);
        when(userClient.getUserByEmail("user@test.com")).thenReturn(userDto);
        when(repository.findByStartupIdAndUserEmail(1L, "user@test.com")).thenReturn(Optional.empty());
        when(repository.save(any(TeamMember.class))).thenAnswer(i -> {
            TeamMember m = i.getArgument(0);
            m.setId(10L);
            return m;
        });

        TeamMember result = teamService.inviteMember(inviteRequest);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("PENDING", result.getStatus());
        assertEquals(15.0, result.getEquityPercentage());
        assertEquals("ADMIN", result.getPermissionLevel());
        verify(rabbitTemplate, times(1)).convertAndSend(eq(RabbitConfig.EXCHANGE), eq(RabbitConfig.ROUTING_KEY_INVITE), any(Map.class));
    }

    @Test
    void testInviteMember_Forbidden() {
        setupSecurityContext("other@test.com");
        
        when(startupClient.getStartup(1L)).thenReturn(startupDto);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            teamService.inviteMember(inviteRequest);
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void testUpdateInviteStatus_Success() {
        setupSecurityContext("user@test.com");
        
        when(repository.findById(10L)).thenReturn(Optional.of(member));
        when(startupClient.getStartup(1L)).thenReturn(startupDto);
        when(repository.save(any(TeamMember.class))).thenAnswer(i -> i.getArgument(0));

        TeamMember result = teamService.updateInviteStatus(10L, "ACCEPTED");

        assertEquals("ACCEPTED", result.getStatus());
        ArgumentCaptor<Map<String, Object>> eventCaptor = ArgumentCaptor.forClass(Map.class);
        verify(rabbitTemplate, times(1)).convertAndSend(eq(RabbitConfig.EXCHANGE), eq(RabbitConfig.ROUTING_KEY_STATUS), eventCaptor.capture());
        assertEquals("founder@test.com", eventCaptor.getValue().get("founderEmail"));
        assertEquals("Test Startup", eventCaptor.getValue().get("startupName"));
    }

    @Test
    void testGetStartupTeam() {
        when(repository.findByStartupId(1L)).thenReturn(List.of(member));

        List<TeamMember> result = teamService.getStartupTeam(1L);

        assertEquals(1, result.size());
    }

    @Test
    void testGetMyInvites() {
        when(repository.findByUserEmail("user@test.com")).thenReturn(List.of(member));

        List<TeamMember> result = teamService.getMyInvites("user@test.com");

        assertEquals(1, result.size());
    }

    @Test
    void testRemoveMemberFounderAllowed() {
        setupSecurityContext("founder@test.com");
        when(repository.findById(10L)).thenReturn(Optional.of(member));
        when(startupClient.getStartup(1L)).thenReturn(startupDto);

        teamService.removeMember(10L);

        verify(repository).delete(member);
    }
}
