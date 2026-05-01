package com.startup_service.Service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import com.startup_service.DTO.TeamMemberDto;
import com.startup_service.Entity.Startup;
import com.startup_service.Feign.TeamClient;

import feign.FeignException;
import feign.Request;

@ExtendWith(MockitoExtension.class)
class StartupPermissionServiceTest {

    @Mock
    private TeamClient teamClient;

    private StartupPermissionService service;
    private Startup startup;

    @BeforeEach
    void setUp() {
        service = new StartupPermissionService(teamClient);
        startup = new Startup();
        startup.setId(7L);
        startup.setFounderEmail("founder@test.com");
    }

    @Test
    void founderAndAdminCanManageStartup() {
        authenticate("founder@test.com", "ROLE_FOUNDER");
        assertTrue(service.canManageStartup(startup));

        authenticate("admin@test.com", "ROLE_ADMIN");
        assertTrue(service.canManageStartup(startup));
        assertTrue(service.isAdmin());
    }

    @Test
    void acceptedTeamMemberWithManagerPermissionCanManage() {
        authenticate("member@test.com", "ROLE_MEMBER");
        TeamMemberDto member = new TeamMemberDto();
        member.setUserEmail("member@test.com");
        member.setStatus("ACCEPTED");
        member.setPermissionLevel("owner");
        when(teamClient.getStartupTeam(7L, "startup-service@internal", "ROLE_ADMIN"))
                .thenReturn(List.of(member));

        assertTrue(service.canManageStartup(startup));
        service.requireStartupManager(startup);
    }

    @Test
    void rejectedOrReadonlyMemberCannotManage() {
        authenticate("member@test.com", "ROLE_MEMBER");
        TeamMemberDto member = new TeamMemberDto();
        member.setUserEmail("member@test.com");
        member.setStatus("PENDING");
        member.setPermissionLevel("OWNER");
        when(teamClient.getStartupTeam(7L, "startup-service@internal", "ROLE_ADMIN"))
                .thenReturn(List.of(member));

        assertFalse(service.canManageStartup(startup));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.requireStartupManager(startup));
        assertTrue(exception.getStatusCode().isSameCodeAs(HttpStatus.FORBIDDEN));
    }

    @Test
    void missingTeamReturnsFalseAndUpstreamErrorBecomesBadGateway() {
        authenticate("member@test.com", "ROLE_MEMBER");
        when(teamClient.getStartupTeam(7L, "startup-service@internal", "ROLE_ADMIN"))
                .thenThrow(notFound());
        assertFalse(service.canManageStartup(startup));

        reset(teamClient);
        when(teamClient.getStartupTeam(7L, "startup-service@internal", "ROLE_ADMIN"))
                .thenThrow(new FeignException.InternalServerError("down", request(), null, Map.of()));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.canManageStartup(startup));
        assertTrue(exception.getStatusCode().isSameCodeAs(HttpStatus.BAD_GATEWAY));
    }

    @Test
    void currentUserEmailReadsSecurityContext() {
        authenticate("user@test.com", "ROLE_USER");
        assertFalse(service.isAdmin());
        assertTrue(service.currentUserEmail().equals("user@test.com"));
    }

    private void authenticate(String email, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority(role))));
    }

    private FeignException.NotFound notFound() {
        return new FeignException.NotFound("not found", request(), null, Map.of());
    }

    private Request request() {
        return Request.create(
                Request.HttpMethod.GET,
                "/team/startup/7",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                null);
    }
}
