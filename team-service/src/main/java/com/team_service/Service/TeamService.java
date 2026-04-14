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
import com.team_service.Entity.TeamMember;
import com.team_service.Feign.StartupClient;
import com.team_service.Feign.UserClient;
import com.team_service.Repository.TeamMemberRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamService {

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

        if (!startup.getFounderEmail().equals(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only founder can invite members");
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

        member.setStatus(status.toUpperCase());
        TeamMember saved = repository.save(member);

        Map<String, Object> event = new HashMap<>();
        event.put("inviteId", saved.getId());
        event.put("startupId", saved.getStartupId());
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

        if (!startup.getFounderEmail().equals(currentUser) && !member.getUserEmail().equals(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only founder or the member themselves can remove");
        }

        repository.delete(member);
    }
}
