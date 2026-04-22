package com.team_service.Controller;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

import com.team_service.DTO.InviteMemberRequest;
import com.team_service.DTO.TeamMemberResponseDTO;
import com.team_service.DTO.UpdateInviteStatusRequest;
import com.team_service.Entity.TeamMember;
import com.team_service.Service.TeamService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService service;

    @PostMapping("/invite")
    public ResponseEntity<TeamMemberResponseDTO> inviteMember(@RequestBody @Valid InviteMemberRequest request) {
        TeamMember member = service.inviteMember(request);
        return ResponseEntity
                .created(URI.create("/team/invite/" + member.getId()))
                .body(toDto(member));
    }

    @PutMapping("/invite/{id}/status")
    public ResponseEntity<TeamMemberResponseDTO> updateInviteStatus(
            @PathVariable Long id, 
            @RequestBody @Valid UpdateInviteStatusRequest request) {
        return ResponseEntity.ok(toDto(service.updateInviteStatus(id, request.getStatus())));
    }

    @GetMapping("/startup/{startupId}")
    public ResponseEntity<List<TeamMemberResponseDTO>> getStartupTeam(@PathVariable Long startupId) {
        return ResponseEntity.ok(service.getStartupTeam(startupId).stream().map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/me")
    public ResponseEntity<List<TeamMemberResponseDTO>> getMyInvites() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(service.getMyInvites(email).stream().map(this::toDto).collect(Collectors.toList()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeMember(@PathVariable Long id) {
        service.removeMember(id);
        return ResponseEntity.noContent().build();
    }

    private TeamMemberResponseDTO toDto(TeamMember member) {
        TeamMemberResponseDTO dto = new TeamMemberResponseDTO();
        dto.setId(member.getId());
        dto.setStartupId(member.getStartupId());
        dto.setUserEmail(member.getUserEmail());
        dto.setRole(member.getRole());
        dto.setStatus(member.getStatus());
        dto.setCreatedAt(member.getCreatedAt());
        return dto;
    }
}
