package com.team_service.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.team_service.Config.RabbitConfig;
import com.team_service.DTO.InviteMemberRequest;
import com.team_service.DTO.StartupDto;
import com.team_service.DTO.UpdateRoleRequest;
import com.team_service.Entity.TeamMember;
import com.team_service.Feign.StartupClient;
import com.team_service.Feign.UserClient;
import com.team_service.Repository.TeamMemberRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamService {

    private static final List<String> ALLOWED_STATUSES = List.of("PENDING", "ACCEPTED", "REJECTED");
    private static final List<String> ALLOWED_PERMISSION_LEVELS = List.of("OWNER", "ADMIN", "MEMBER");
    private static final List<String> MANAGER_PERMISSION_LEVELS = List.of("OWNER", "ADMIN");
    private static final String INTERNAL_ACTOR_EMAIL = "team-service@internal";
    private static final String INTERNAL_ACTOR_ROLE = "ROLE_ADMIN";

    private final TeamMemberRepository repository;
    private final StartupClient startupClient;
    private final UserClient userClient;
    private final RabbitTemplate rabbitTemplate;

    public TeamMember inviteMember(InviteMemberRequest request) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        StartupDto startup;
        try {
            startup = startupClient.getStartup(request.getStartupId());
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup not found");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to load startup details");
        }

        if (!canManageTeam(startup, currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the founder or an accepted admin-level team member can invite members");
        }

        try {
            userClient.getUserByEmail(request.getUserEmail());
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User to invite not found");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to load invited user details");
        }

        Optional<TeamMember> existing = repository.findByStartupIdAndUserEmail(request.getStartupId(), request.getUserEmail());
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already invited or a member");
        }

        TeamMember member = new TeamMember();
        member.setStartupId(request.getStartupId());
        member.setUserEmail(request.getUserEmail());
        member.setRole(request.getRole());
        member.setStatus("PENDING");
        member.setEquityPercentage(request.getEquityPercentage() != null ? request.getEquityPercentage() : 0.0);
        member.setPermissionLevel(normalizePermissionLevel(request.getPermissionLevel()));

        TeamMember saved = repository.save(member);

        Map<String, Object> event = new HashMap<>();
        event.put("inviteId", saved.getId());
        event.put("startupId", saved.getStartupId());
        event.put("startupName", startup.getName());
        event.put("userEmail", saved.getUserEmail());
        event.put("role", saved.getRole());

        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY_INVITE, event);

        return saved;
    }

    public TeamMember updateInviteStatus(Long id, String status) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        TeamMember member = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invite not found"));

        if (!member.getUserEmail().equals(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own invites");
        }

        String normalizedStatus = normalizeStatus(status);
        if ("ACCEPTED".equals(normalizedStatus) && "COFOUNDER".equalsIgnoreCase(member.getRole())) {
            promoteAcceptedCoFounder(member.getUserEmail());
        }

        member.setStatus(normalizedStatus);
        TeamMember saved = repository.save(member);

        StartupDto startup;
        try {
            startup = startupClient.getStartup(saved.getStartupId());
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup not found");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to load startup details");
        }

        Map<String, Object> event = new HashMap<>();
        event.put("inviteId", saved.getId());
        event.put("startupId", saved.getStartupId());
        event.put("startupName", startup.getName());
        event.put("founderEmail", startup.getFounderEmail());
        event.put("userEmail", saved.getUserEmail());
        event.put("status", saved.getStatus());

        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY_STATUS, event);

        return saved;
    }

    public List<TeamMember> getStartupTeam(Long startupId) {
        return repository.findByStartupId(startupId);
    }

    public List<TeamMember> getMyInvites(String email) {
        return repository.findByUserEmail(email);
    }
    
    public void removeMember(Long id) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        TeamMember member = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        StartupDto startup;
        try {
            startup = startupClient.getStartup(member.getStartupId());
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup not found");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to load startup details");
        }

        if (!member.getUserEmail().equals(currentUser) && !canManageTeam(startup, currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the founder, an accepted admin-level team member, or the member themselves can remove");
        }

        repository.delete(member);
    }

    private String normalizeStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid invite status");
        }
        return normalized;
    }

    private String normalizePermissionLevel(String permissionLevel) {
        if (permissionLevel == null || permissionLevel.isBlank()) {
            return "MEMBER";
        }
        String normalized = permissionLevel.trim().toUpperCase();
        if (!ALLOWED_PERMISSION_LEVELS.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid permission level");
        }
        return normalized;
    }

    private boolean canManageTeam(StartupDto startup, String currentUser) {
        if (startup.getFounderEmail().equals(currentUser)) {
            return true;
        }

        return repository.findByStartupIdAndUserEmail(startup.getId(), currentUser)
                .filter(member -> "ACCEPTED".equals(member.getStatus()))
                .map(TeamMember::getPermissionLevel)
                .map(String::toUpperCase)
                .filter(MANAGER_PERMISSION_LEVELS::contains)
                .isPresent();
    }

    private void promoteAcceptedCoFounder(String userEmail) {
        try {
            userClient.updateUserRole(
                    userEmail,
                    new UpdateRoleRequest("ROLE_COFOUNDER"),
                    INTERNAL_ACTOR_EMAIL,
                    INTERNAL_ACTOR_ROLE
            );
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invited user not found for role promotion");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to promote co-founder role");
        }
    }
}
