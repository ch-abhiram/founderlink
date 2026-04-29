package com.startup_service.Service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.startup_service.DTO.TeamMemberDto;
import com.startup_service.Entity.Startup;
import com.startup_service.Feign.TeamClient;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StartupPermissionService {

    private static final List<String> MANAGER_PERMISSION_LEVELS = List.of("OWNER", "ADMIN");
    private static final String INTERNAL_ACTOR_EMAIL = "startup-service@internal";
    private static final String INTERNAL_ACTOR_ROLE = "ROLE_ADMIN";

    private final TeamClient teamClient;

    public void requireStartupManager(Startup startup) {
        if (!canManageStartup(startup)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to manage this startup");
        }
    }

    public boolean canManageStartup(Startup startup) {
        String currentUser = currentUserEmail();
        if (startup.getFounderEmail().equals(currentUser) || isAdmin()) {
            return true;
        }

        return fetchTeamMembers(startup.getId()).stream()
                .filter(member -> currentUser.equalsIgnoreCase(member.getUserEmail()))
                .filter(member -> "ACCEPTED".equalsIgnoreCase(member.getStatus()))
                .map(TeamMemberDto::getPermissionLevel)
                .filter(level -> level != null && MANAGER_PERMISSION_LEVELS.contains(level.toUpperCase()))
                .findFirst()
                .isPresent();
    }

    public String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private List<TeamMemberDto> fetchTeamMembers(Long startupId) {
        try {
            return teamClient.getStartupTeam(startupId, INTERNAL_ACTOR_EMAIL, INTERNAL_ACTOR_ROLE);
        } catch (FeignException.NotFound e) {
            return List.of();
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to load startup team permissions");
        }
    }
}
